package com.yuuna.schat.util;

import com.google.gson.JsonObject;

public class AppConstants {

    public static final String SCHAT = "SCHAT";

    public static final String TAG_SIGN = "isSign";
    public static final String TAG_KEY = "key";
    public static final String TAG_NAME = "name";

    public static final String TAG_ACC = "account";

    public static String payload(String requestType, JsonObject data) {
        JsonObject payload = new JsonObject();
        payload.addProperty("request", requestType);
        payload.add("data", data);
        return payload.toString();
    }
}
