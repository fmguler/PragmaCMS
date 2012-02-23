/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service.storage;

import com.fmguler.common.service.storage.dao.StorageDao;
import com.fmguler.common.service.storage.domain.StorageObject;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Simple Storage Service based on key - data
 * <p>
 * Keeps track of the keys in the database (StorageObjects).
 * Deletes the orphan (not referenced) keys automatically.
 * Users of the service should write the keys to a database table and
 * tell us the table-field for this feature.
 *
 * @author Fatih Mehmet Güler
 */
public class StorageServiceImpl implements StorageService {
    private final int MAINTENANCE_INTERVAL = 1000 * 60;//* 5;
    private StorageDao storageDao;
    private Timer maintenanceTimer;
    private List referencingTables;
    private String storageFolder;
    private SecureRandom random;
    private Set keysToBeDeleted = new HashSet();
    private Map openFiles = new HashMap();
    private boolean debugMode = false;

    public StorageServiceImpl() {
        random = new SecureRandom();
    }

    /**
     * Initialize the service.
     * Creates and starts maintenance tasks.
     */
    public void init() {
        maintenanceTimer = new Timer(true); //true lets it not to continue after server shutdown, important.
        maintenanceTimer.scheduleAtFixedRate(new TimerTask() {
            /**
             * When the consumers of this service remove a key, they simply
             * delete
             * it from the database. Here we calculate the unreferenced keys
             * from consumers (orphan keys)
             * and take necessary actions on it (currently we simply delete).
             *
             * Note that in order this to work, we should need in which tables
             * and fields references the keys,
             * this should be set to this service implementation with
             * setReferencingTables().
             */
            public void run() {

                //if some messages are deleted, they may also leave orphan contents
                //(contents which are not referenced from any attachment) so delete them as well.
                List orphanObjects = storageDao.findOrphanObjects(referencingTables);

                //we delete the orphan keys and associated data
                Iterator it = orphanObjects.iterator();
                while (it.hasNext()) {
                    StorageObject storageObject = (StorageObject)it.next();
                    removeKey(storageObject.getKey());
                }

            }
        }, 7654, MAINTENANCE_INTERVAL);
    }

    /**
     * Close all open files, remove all undeleted keys.
     * <p>Call when closing down.
     */
    public void destroy() {
        if (debugMode) System.out.println("destroy starts");

        //close all open files (may be they are left open) this also gets them removed if they are on the list to be deleted
        Iterator it = openFiles.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Object value = openFiles.get(key);
            if (value instanceof Closeable) {
                try {
                    ((Closeable)value).close();
                    if (debugMode) System.out.println("closed key: " + key);
                } catch (IOException ex) {
                    if (debugMode) System.out.println("could not close key: " + key + " error: " + ex.getMessage());
                }
            }
        }

        if (debugMode) System.out.println("destroy ends");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    /**
     * Generate a new key to be used for writing/reading.
     * <p>
     * Note that in order to remove unused keys automatically,
     * the generated keys should be stored in a database table,
     * and the referencing table and field names should be specified with
     * setReferencingTables() method.
     * <p>
     * If automatic key deletion is not preferred, set
     * StorageObject.autoRemove=false and remove keys manually with removeKey();
     * (THIS IS NOT IMPLEMENTED CURRENTLY - YAGNI)
     */
    public String generateKey() {
        byte[] keyBytes = new byte[8];
        String key = "";
        do {
            random.nextBytes(keyBytes);
            key = toHex(keyBytes);
        } while (keyExists(key));

        return key;
    }

    /**
     * Return the inputStream of the content;
     * This inputStream must be closed after the reading process has finished.
     * @throws StorageException if the key is not found or invalid (use
     * generateKey() to generate keys)
     */
    public InputStream getInputStream(String key) throws StorageException {
        //impl:
        //FILE -> dosyayı bul, ona fileinputstream döndür. IS close edilince, key silinmişse (silinecekler listesindeyse) dosyayı sil.
        //Amazon s3 -> Web servis'ten okuyan IS döndür.
        //NFS -> NFS'ten okuyan IS döndür.
        try {
            if (key == null || key.length() < 5) throw new StorageException(StorageException.ERROR_INVALID_KEY, null); //must be hexadecimal and greater than 4 digits
            if (!keyExists(key)) throw new StorageException(StorageException.ERROR_KEY_NOT_FOUND, null);

            //if this key is still being written return a piped input stream which reads directly from the SFOS
            //this input stream will block until the SFOS is closed (NOTE: disable here if this causes errors)
            Object openFile = openFiles.get(key);
            if (openFile != null) {
                StorageFileOutputStream sfos = (StorageFileOutputStream)openFile;
                return sfos.getPipedInputStream();
            }

            //return a file input stream
            StorageFileInputStream sfis = new StorageFileInputStream(keyToPath(key, false));
            sfis.setDoxService(this);
            sfis.setKey(key);
            openFiles.put(key, sfis);
            return sfis;
        } catch (FileNotFoundException ex) {
            throw new StorageException(StorageException.ERROR_KEY_NOT_FOUND, "is", ex);
        }
    }

    /**
     * Return the outputStream that can be written to the specified key.
     * This outputStream must be closed after the writing process has finished.
     * <p>
     * While writing to the outputStream, do not call getOutputStream() again.
     * <p>
     * IMPORTANT: Each key can be written only once. Generate a new key, and
     * replace your key reference,
     * if you need to update your data.
     * @throws StorageException if the specified key already exists / write
     * failue
     */
    public OutputStream getOutputStream(String key) throws StorageException {
        //impl:
        //FILE -> dosyaya yazan fileoutputstream döndür. IS close edilince, key silinmişse (silinecekler listesindeyse) dosyayı sil.
        //Amazon s3 -> web servise yazan OS döndür.
        //NFS -> NFS'e yazan OS döndür.
        try {
            if (keyExists(key)) throw new StorageException(StorageException.ERROR_KEY_ALREADY_EXISTS, null);
            StorageFileOutputStream sfos = new StorageFileOutputStream(keyToPath(key, true), this, key); //keyToPath creates folders
            openFiles.put(key, sfos);
            //add the key to the registry
            storageDao.insertStorageObject(new StorageObject(key));
            return sfos;
        } catch (FileNotFoundException ex) {
            throw new StorageException(StorageException.ERROR_WRITE_FAILED, "os", ex);
        }
    }

    /**
     * Return the storage object which contains size and hash of the specified
     * key
     * @param key the storage key
     * @return the storage object with size and hash
     */
    public StorageObject getStorageObject(String key) {
        return storageDao.getStorageObject(key);
    }

    /**
     * Removes the specified key.
     * <p>
     * NOTE: Do not rely on removing key behavior.
     * Check the result if the key is really deleted.
     * If it returns false, it will be deleted later.
     * Do not use the same key to write new data. Generate a new key.
     */
    protected boolean removeKey(String key) {
        //NOTE: I make this method private, since the orphan storage objects will be deleted by itself. 
        //When the need of having keys not stored in database arises, this method can be made public again.
        //Then also, the orphan deletion should ignore keys that are not stored in database, which will be deleted with this method (e.g. put a no-orphan-check boolean field to the StorageObject)
        if (key == null) return false;
        if (!keyExists(key)) {
            storageDao.deleteStorageObject(key); //remove from the registry
            return false; //dont throw exception if the key does not exist
        }
        File file = new File(keyToPath(key, false));
        if (file.delete()) {
            keysToBeDeleted.remove(key); //if already on the list, remove it
            file.getParentFile().delete(); //delete parent dir if empty
            file.getParentFile().getParentFile().delete(); //delete parent dir if empty
            if (debugMode) System.out.println("removed key: " + key);
            storageDao.deleteStorageObject(key); //remove from the registry
            return true;
        } else {
            if (debugMode) System.out.println("could not remove key, adding to list: " + key);
            keysToBeDeleted.add(key); //may be someone is reading it, or writing it, delete later
            return false;
        }
    }

    //--------------------------------------------------------------------------
    //PRIVATE METHODS
    /**
     * callback method to be called when content inputstream is closed
     */
    protected void onInputStreamClosed(String key) {
        //remove it from open files
        openFiles.remove(key);
        //the key is deleted while IS is alive, delete it now.
        if (keysToBeDeleted.contains(key)) removeKey(key);
    }

    /**
     * callback method to be called when content outputstream is closed
     */
    protected void onOutputStreamClosed(String key, int size, String hash) {
        //remove it from open files
        openFiles.remove(key);
        //the key is deleted while OS is alive, delete it now.
        if (keysToBeDeleted.contains(key)) removeKey(key);
        else storageDao.updateStorageObject(new StorageObject(key, size, hash)); //set the size/hash
    }

    //byte array to hex (used in key generation)
    protected static String toHex(byte[] bytes) {
        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += hexChar[(bytes[i] & 0xf0) >> 4];
            result += hexChar[bytes[i] & 0x0f];
        }
        return result;
    }

    //returns the path corresponding to the specified key. 
    private String keyToPath(String key, boolean mkdirs) {
        //NOTE: currently, the folder is: rootFolder / key0key1 /key2key3 / key456789...
        //this can be changed if the second folder exceeds the max no of files that the os can hold.
        String result = storageFolder;
        result += "/";
        result += key.substring(0, 2);
        result += "/";
        result += key.substring(2, 4);
        if (mkdirs) new File(result).mkdirs();
        result += "/";
        result += key.substring(4);

        return result;
    }

    //returns if the specified key already exists
    private boolean keyExists(String key) {
        return new File(keyToPath(key, false)).exists();
    }

    //turns on or off system out println's
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    //--------------------------------------------------------------------------
    //SETTERS
    /**
     * @param storageFolder root folder that the storage data will be kept
     */
    public void setStorageFolder(String storageFolder) {
        this.storageFolder = storageFolder;
    }

    /**
     * Set the tables which references keys generated by this service.
     * @param referencingTables list of maps including tableName and fieldName
     * keys.
     */
    public void setReferencingTables(List referencingTables) {
        this.referencingTables = referencingTables;
    }

    /**
     * @param storageDao the storage dao used to keep track of key registry
     */
    public void setStorageDao(StorageDao storageDao) {
        this.storageDao = storageDao;
    }
}

/**
 * private wrapper around file input stream in order to detect closing
 */
class StorageFileInputStream extends FileInputStream {
    private StorageServiceImpl serviceImpl;
    private String key;

    public StorageFileInputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public void setDoxService(StorageServiceImpl serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void close() throws IOException {
        super.close();
        serviceImpl.onInputStreamClosed(key);
    }
}

/**
 * private wrapper around arbitrary output stream in order to detect closing and
 * calculate, size&hash
 * NOTE: this encapsulates the outputstream instance.
 * If this would extend e.g. fileoutputstream, the hash calculation won't work
 * because the VM calls .close() multiple times.
 */
class StorageFileOutputStream extends OutputStream implements StorageOutputStream {
    private FileOutputStream out;
    private String filePath;
    private StorageServiceImpl serviceImpl;
    private String key;
    private int size;
    private String hash;
    private MessageDigest md;
    //pipes to write to
    private final List pipes = new LinkedList();

    public StorageFileOutputStream(String name, StorageServiceImpl serviceImpl, String key) throws FileNotFoundException {
        this.out = new FileOutputStream(name);
        this.filePath = name;
        this.serviceImpl = serviceImpl;
        this.key = key;
        try {
            this.md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            //not expected. we crash severely this happens)
        }
    }

    //--------------------------------------------------------------------------
    //OUTPUT STREAM METHODS
    public void write(byte[] b) throws IOException {
        out.write(b);
        size += b.length;
        md.update(b);

        //write to pipes
        Iterator it = pipes.iterator();
        while (it.hasNext()) {
            OutputStream pos = (OutputStream)it.next();
            try {
                pos.write(b);
            } catch (IOException ex) {
            }
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        size += len;
        md.update(b, off, len);

        //write to pipes
        Iterator it = pipes.iterator();
        while (it.hasNext()) {
            OutputStream pos = (OutputStream)it.next();
            try {
                pos.write(b, off, len);
            } catch (IOException ex) {
            }
        }
    }

    public void write(int b) throws IOException {
        out.write(b);
        size++;
        md.update((byte)(b & 0xff));

        //write to pipes
        Iterator it = pipes.iterator();
        while (it.hasNext()) {
            OutputStream pos = (OutputStream)it.next();
            try {
                pos.write(b);
            } catch (IOException ex) {
            }
        }
    }

    public void flush() throws IOException {
        out.flush();

        //flush pipes
        Iterator it = pipes.iterator();
        while (it.hasNext()) {
            OutputStream pos = (OutputStream)it.next();
            try {
                pos.flush();
            } catch (IOException ex) {
            }
        }
    }

    public void close() throws IOException {
        out.close();
        hash = StorageServiceImpl.toHex(md.digest());
        serviceImpl.onOutputStreamClosed(key, size, hash);

        //close pipes
        Iterator it = pipes.iterator();
        while (it.hasNext()) {
            OutputStream pos = (OutputStream)it.next();
            try {
                pos.close();
            } catch (IOException ex) {
            }
        }
    }

    //--------------------------------------------------------------------------
    //PIPE RELATED METHODS
    /**
     * @return the piped input stream to read directly from this output stream
     */
    public InputStream getPipedInputStream() {
        synchronized (pipes) {
            //init a new pipe
            PipedOutputStream pos = null;
            InputStream pipedIS = null;
            try {
                pipedIS = new PipedInputStream();
                pos = new PipedOutputStream((PipedInputStream)pipedIS);
            } catch (IOException ex) {
                //nothing to do, cant read.
                pipedIS = new ByteArrayInputStream(new byte[0]);
            }

            //write the existing data up to current size
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filePath);
                byte[] buf = new byte[4096];
                int c;
                while ((c = fis.read(buf)) != -1) {
                    pos.write(buf, 0, c);
                }
            } catch (IOException ex) {
            } finally {
                //fucking java boilerplate code
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                    }
                }
            }

            //add the pos to the list of pipes which this output stream will write to
            pipes.add(pos);

            //return the new pipe
            return pipedIS;
        }
    }

    //-----------------------------------------------------------------------------------
    //METHODS BELOW COULD BE USED BY CONSUMERS OF THE SERVICE BY CASTING RETURNED OUTPUT STREAM TO SFOS
    /**
     * @return the storage key for this content
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the size of the total written bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the hash of the written content
     */
    public String getHash() {
        return hash;
    }
}
