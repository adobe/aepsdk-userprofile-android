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

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JSONUtilsTests {
    @Test
    public void test_basicTypes() throws JSONException {
        String json = "{\"key1\":\"value\",\"key2\":1,\"key3\":1.2,\"key4\":true}";
        JSONObject jsonObject = new JSONObject(json);
        Map<String, Object> map = JSONUtils.convertJsonObjectToNestedMap(jsonObject);
        assertEquals("value", map.get("key1"));
        assertEquals(1, map.get("key2"));
        assertEquals(1.2, map.get("key3"));
        assertEquals(true, map.get("key4"));
    }

    @Test
    public void test_mapAsValue() throws JSONException {
        String json = "{\"key1\":\"value\",\"key2\":{\"key1\":\"value\",\"key2\":1,\"key3\":1.2,\"key4\":true}}";
        JSONObject jsonObject = new JSONObject(json);
        Map<String, Object> map = JSONUtils.convertJsonObjectToNestedMap(jsonObject);
        assertEquals("value", map.get("key1"));
        assertEquals(new HashMap<String, Object>() {
            {
                put("key1", "value");
                put("key2", 1);
                put("key3", 1.2);
                put("key4", true);
            }
        }, map.get("key2"));
    }

}
