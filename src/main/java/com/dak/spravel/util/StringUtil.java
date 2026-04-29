package com.dak.spravel.util;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * String manipulation utilities including snake case conversion and JSON masking.
 */
@Component
public class StringUtil {
    public static String toSnakeCase(String s) {
        return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static JSONObject maskingJson(JSONObject obj) {
        maskKey(obj, "companyKey");
        maskKey(obj, "password");
        maskKey(obj, "token");
        maskKey(obj, "access_token");
        maskKey(obj, "refresh_token");

        return obj;
    }

    public static void maskKey(JSONObject obj, String key) {
        if (obj.has(key)) {
            obj.put(key, "*****");
        }

        if (obj.has("data")) {
            Object objects = obj.get("data");
            if (objects instanceof JSONObject) {
                JSONObject dataObject = obj.getJSONObject("data");
                if (dataObject.has(key)) {
                    dataObject.put(key, "*****");
                }
            }
        }
    }

    public static boolean hasFourDots(String ipAddress) {
        int dotCount = 0;

        for (char c : ipAddress.toCharArray())  {
            if (c == '.') {
                dotCount++;
            }
        }

        return dotCount == 3;
    }
}

