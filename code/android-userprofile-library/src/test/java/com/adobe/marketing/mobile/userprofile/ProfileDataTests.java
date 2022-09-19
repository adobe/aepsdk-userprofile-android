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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProfileDataTests {

    private ProfileData profileData;
    @Mock
    private NamedCollection namedCollection;

    @Before
    public void setup() {
        reset(namedCollection);
        profileData = new ProfileData(namedCollection);
    }

    @Test
    public void test_loadPersistenceData() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> defaultValueCaptor = ArgumentCaptor.forClass(String.class);
        when(namedCollection.getString(keyCaptor.capture(), defaultValueCaptor.capture())).thenReturn("{\"key1\":\"value1\",\"key2\":\"value2\"}");
        assertTrue(profileData.loadPersistenceData());
        assertEquals("{}", defaultValueCaptor.getValue());
        assertEquals("user_profile", keyCaptor.getValue());
        assertEquals(new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        }, profileData.getMap());
    }

    @Test
    public void test_loadPersistenceData_keyNotExists() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> defaultValueCaptor = ArgumentCaptor.forClass(String.class);
        when(namedCollection.getString(keyCaptor.capture(), defaultValueCaptor.capture())).thenReturn(null);
        assertTrue(profileData.loadPersistenceData());
        assertEquals("{}", defaultValueCaptor.getValue());
        assertEquals("user_profile", keyCaptor.getValue());
        assertTrue(profileData.getMap().isEmpty());
    }

    @Test
    public void test_loadPersistenceData_invalidJson() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> defaultValueCaptor = ArgumentCaptor.forClass(String.class);
        when(namedCollection.getString(keyCaptor.capture(), defaultValueCaptor.capture())).thenReturn("{this-is-not-a-valid-json}");
        assertFalse(profileData.loadPersistenceData());
        assertEquals("{}", defaultValueCaptor.getValue());
        assertEquals("user_profile", keyCaptor.getValue());
        assertTrue(profileData.getMap().isEmpty());
    }

    @Test
    public void test_persist() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(namedCollection).setString(keyCaptor.capture(), jsonCaptor.capture());
        profileData.updateOrDelete(new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        });
        assertTrue(profileData.persist());
        assertEquals("user_profile", keyCaptor.getValue());
        assertEquals("{\"key1\":\"value1\"}", jsonCaptor.getValue());
    }

    @Test
    public void test_persist_withNullService() {
        profileData = new ProfileData(null);
        profileData.updateOrDelete(new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        });
        assertFalse(profileData.persist());
    }

    @Test
    public void test_persist_withServiceError() {
        profileData.updateOrDelete(new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        });
        doThrow(new RuntimeException("")).when(namedCollection).setString(any(), any());
        assertFalse(profileData.persist());
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_getMap_unmodified() {
        Map<String, Object> profile = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        profileData.updateOrDelete(profile);
        Map<String, Object> unmodifiedMap = profileData.getMap();
        assertEquals(profile, unmodifiedMap);
        unmodifiedMap.remove("key1");
    }

    @Test
    public void test_getMap_invalidMap() {
        Map<String, Object> profile = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        profileData.updateOrDelete(profile);
        assertNull(profileData.getMap("key1"));
    }

    @Test
    public void test_updateOrDelete() {
        Map<String, Object> profileMap1 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        profileData.updateOrDelete(profileMap1);
        assertEquals(profileMap1, profileData.getMap());
        Map<String, Object> profileMap2 = new HashMap<String, Object>() {
            {
                put("key3", "value3");
                put("key2", null);
            }
        };
        profileData.updateOrDelete(profileMap2);
        assertEquals(new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key3", "value3");
            }
        }, profileData.getMap());
    }

    @Test
    public void test_delete() {
        Map<String, Object> profileMap1 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        profileData.updateOrDelete(profileMap1);
        assertEquals(profileMap1, profileData.getMap());
        List<String> deletedKeys = new ArrayList<>();
        deletedKeys.add("key2");
        profileData.delete(deletedKeys);
        assertEquals(new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        }, profileData.getMap());
    }

    @Test
    public void test_get() {
        Map<String, Object> profileMap1 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        profileData.updateOrDelete(profileMap1);
        assertEquals(profileMap1, profileData.getMap());
        assertEquals("value1", profileData.get("key1"));
    }

}
