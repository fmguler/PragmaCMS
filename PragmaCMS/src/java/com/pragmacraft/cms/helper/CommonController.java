/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Common Controller Functions
 * @author Fatih Mehmet Güler
 */
public class CommonController {
    //json enumerations
    public static final String JSON_STATUS_SUCCESS = "0";
    public static final String JSON_STATUS_FAIL = "-1";

    /**
     * Returns common status Json
     * @param status JSON_STATUS_XYZ
     * @param message the message to return
     * @param object the object to return
     * @return json object
     */
    public static String toStatusJson(String status, String message, Object object) {
        Map map = new HashMap();
        map.put("status", status);
        map.put("message", message);
        map.put("object", object);

        Gson gson = getGson();
        String json = gson.toJson(map);
        return json;
    }

    //create gson with custom serializers
    private static Gson getGson() {
        return new GsonBuilder().setDateFormat("dd.MM.yyyy HH:mm").create();
    }
}
