PragmaCMS
=========

The easiest way of hosting professional web sites, and keeping them up-to-date!

PragmaCMS is aimed to be a lightweight alternative to mainstream PHP CMS/blogging platforms like Drupal/Joomla/Wordpress. Instead of very complex scenarios, when you need only a simple CMS system to serve static pages using a nice template. 

See it live at;
http://www.pragmacms.com/

Installing
==========
PragmaCMS is a Netbeans project. All dependencies are included. Just open with Netbeans and click build.
You can deploy it to Tomcat/Jetty or other servlet containers. You need to specify pragmacms.home parameter as VM options. (e.g.  -Dpragmacms.home=/path/to/PragmaCMS). This PragmaCMS folder contains database configuration and folders to store templates, etc. You can get this folder from here: https://dl.dropboxusercontent.com/u/5564675/PragmaCMS.zip

Create a blank database in PostgreSQL and update the PragmaCMS.properties at conf folder with the db credentials. The app will populate schema on first load. 

Contact me if you have any questions.
