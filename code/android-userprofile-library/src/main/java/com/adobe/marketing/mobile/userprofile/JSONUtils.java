/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.userprofile;

import androidx.annotation.NonNull;

import com.adobe.marketing.mobile.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class JSONUtils {
    private static final String LOG_TAG = "JSONUtils";

    /**
     * Converts a {@link JSONObject} to a nested {@link Map}
     *
     * @param jsonObject a {@link JSONObject} object
     * @return a nested {@link Map}
     */
    static Map<String, Object> convertJsonObjectToNestedMap(@NonNull final JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    JSONObject nestedJsonObject = (JSONObject) value;
                    map.put(key, convertJsonObjectToNestedMap(nestedJsonObject));
                } else if (value instanceof JSONArray) {
                    Log.error(LOG_TAG, "Profile Data doesn't support Array value.");
                } else {
                    map.put(key, value);
                }
            } catch (Exception e) {
                Log.error(LOG_TAG, "The value of [%s] is not supported: %s", key, e);
            }
        }
        return map;
    }
}
