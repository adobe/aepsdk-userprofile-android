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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionErrorCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@RunWith(MockitoJUnitRunner.Silent.class)
public class UserProfileExtensionTests {

    @Mock
    private ExtensionApi extensionApiMock;

    private UserProfileExtension userProfileExtension;

    @Before
    public void setup() {
        reset(extensionApiMock);
        this.userProfileExtension = new UserProfileExtension(extensionApiMock);
    }

    @Test
    public void test_readyForEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData() was called.
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();

        }
    }

    @Test
    public void test_handleProfileUpdateEvent() {
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key", "value");
            }
        };
        Map<String, Object> eventDataMap = new HashMap<String, Object>() {
            {
                put("userprofileupdatekey", data);
            }
        };
        //
        Event updateProfileEvent = new Event.Builder(
                "UserProfileUpdate",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile").setEventData(eventDataMap).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileUpdateEvent(updateProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(updateProfileEvent, data);
        }
    }

    @Test
    public void test_handleProfileUpdateEvent_withNullValue() {
        Map<String, Object> profileMap = new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        };
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", null);
            }
        };
        Map<String, Object> eventDataMap = new HashMap<String, Object>() {
            {
                put("userprofileupdatekey", data);
            }
        };
        //
        Event updateProfileEvent = new Event.Builder(
                "UserProfileUpdate",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile").setEventData(eventDataMap).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(profileMap);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileUpdateEvent(updateProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(updateProfileEvent, profileMap);
        }
    }

    @Test
    public void test_handleProfileUpdateEvent_emptyMap() {
        Map<String, Object> profileMap = new HashMap<String, Object>() {
        };
        Map<String, Object> eventDataMap = new HashMap<String, Object>() {
            {
                put("userprofileupdatekey", profileMap);
            }
        };
        Event updateProfileEvent = new Event.Builder(
                "UserProfileUpdate",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile").setEventData(eventDataMap).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.handleProfileUpdateEvent(updateProfileEvent);
            assertEquals(0, profileDataMocks.constructed().size());
        }
    }

    @Test
    public void test_handleProfileGetAttributesEvent() {
        List<String> keys = Arrays.asList("key1", "key2");
        Event getProfileEvent = new Event.Builder(
                "getUserAttributes",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("userprofilegetattributes", keys);
                    }
                }).build();

        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(
                ProfileData.class,
                (mock, context) -> {
                    when(mock.get(anyString())).thenAnswer(invocation -> {
                        if (invocation.getArgument(0).equals("key1")) {
                            return "value1";
                        } else {
                            return "value2";
                        }
                    });

                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            doNothing().when(extensionApiMock).dispatch(eventCaptor.capture());
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileGetAttributesEvent(getProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(2)).get(anyString());
            // 3. an Event was dispatched with the loaded profile data.
            Event responseEvent = eventCaptor.getValue();
            assertNotNull(responseEvent);
            assertEquals("UserProfile Response Event", responseEvent.getName());
            assertEquals("com.adobe.eventSource.responseProfile", responseEvent.getSource());
            assertEquals("com.adobe.eventType.userProfile", responseEvent.getType());
            assertEquals(data, responseEvent.getEventData().get("userprofilegetattributes"));
        }
    }

    @Test
    public void test_handleProfileGetAttributesEvent_keyFoundPartially() {
        List<String> keys = Arrays.asList("key1", "key2");
        Event getProfileEvent = new Event.Builder(
                "getUserAttributes",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("userprofilegetattributes", keys);
                    }
                }).build();

        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(
                ProfileData.class,
                (mock, context) -> {
                    when(mock.get(anyString())).thenAnswer(invocation -> {
                        if (invocation.getArgument(0).equals("key1")) {
                            return "value1";
                        } else {
                            return null;
                        }
                    });

                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            doNothing().when(extensionApiMock).dispatch(eventCaptor.capture());
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileGetAttributesEvent(getProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(2)).get(anyString());
            // 3. an Event was dispatched with the loaded profile data.
            Event responseEvent = eventCaptor.getValue();
            assertNotNull(responseEvent);
            assertEquals("UserProfile Response Event", responseEvent.getName());
            assertEquals("com.adobe.eventSource.responseProfile", responseEvent.getSource());
            assertEquals("com.adobe.eventType.userProfile", responseEvent.getType());
            assertEquals(data, responseEvent.getEventData().get("userprofilegetattributes"));
        }
    }

    @Test
    public void test_handleProfileGetAttributesEvent_keyNotExists() {
        List<String> keys = Arrays.asList("key1", "key2");
        Event getProfileEvent = new Event.Builder(
                "getUserAttributes",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("userprofilegetattributes", keys);
                    }
                }).build();
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(
                ProfileData.class,
                (mock, context) -> {
                    when(mock.get(anyString())).thenAnswer(invocation -> null);

                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            doNothing().when(extensionApiMock).dispatch(eventCaptor.capture());
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileGetAttributesEvent(getProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(2)).get(anyString());
            // 3. an Event was dispatched with the loaded profile data.
            Event responseEvent = eventCaptor.getValue();
            assertNotNull(responseEvent);
            assertEquals("UserProfile Response Event", responseEvent.getName());
            assertEquals("com.adobe.eventSource.responseProfile", responseEvent.getSource());
            assertEquals("com.adobe.eventType.userProfile", responseEvent.getType());
            assertEquals(new HashMap<String, Object>(), responseEvent.getEventData().get("userprofilegetattributes"));
        }
    }

    @Test
    public void test_handleProfileGetAttributesEvent_emptyList() {
        List<String> keys = new ArrayList<>();
        Event getProfileEvent = new Event.Builder(
                "getUserAttributes",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestProfile")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("userprofilegetattributes", keys);
                    }
                }).build();
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class)) {
            userProfileExtension.handleProfileGetAttributesEvent(getProfileEvent);
            verifyNoInteractions(extensionApiMock);
            assertEquals(0, profileDataMocks.constructed().size());
        }
    }

    @Test
    public void test_handleProfileDeleteEvent() {
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key", "value");
            }
        };
        List<String> keys = Arrays.asList("key1", "key2");
        Map<String, Object> eventDataMap = new HashMap<String, Object>() {
            {
                put("userprofileremovekeys", keys);
            }
        };
        //
        Event removeProfileEvent = new Event.Builder(
                "RemoveUserProfile",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestReset").setEventData(eventDataMap).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleProfileDeleteEvent(removeProfileEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).delete(listCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(keys, listCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(removeProfileEvent, data);
        }
    }

    @Test
    public void test_handleProfileDeleteEvent_emptyList() {
        List<String> keys = new ArrayList<>();
        Map<String, Object> eventDataMap = new HashMap<String, Object>() {
            {
                put("userprofileremovekeys", keys);
            }
        };
        //
        Event removeProfileEvent = new Event.Builder(
                "RemoveUserProfile",
                "com.adobe.eventType.userProfile",
                "com.adobe.eventSource.requestReset").setEventData(eventDataMap).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class)) {
            userProfileExtension.handleProfileDeleteEvent(removeProfileEvent);
            verifyNoInteractions(extensionApiMock);
            assertEquals(0, profileDataMocks.constructed().size());
        }
    }

    @Test
    public void test_handleRulesEvent_write() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "key");
                                        put("value", "value");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key", "value");
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_write_iam_viewed() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "a.viewed");
                                        put("value", "zzzzzzzzzz");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("a.viewed", new HashMap<String, Object>() {
                    {
                        put("zzzzzzzzzz", 1);
                    }
                });
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_write_iam_triggered() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "a.triggered");
                                        put("value", "aaaaaaaaaa");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("a.triggered", new HashMap<String, Object>() {
                    {
                        put("aaaaaaaaaa", 1);
                    }
                });
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_write_iam_clicked() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "a.clicked");
                                        put("value", "hhhhhhhhhh");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("a.clicked", new HashMap<String, Object>() {
                    {
                        put("hhhhhhhhhh", 1);
                    }
                });
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_write_iam_clickedMultiple() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "a.clicked");
                                        put("value", "hhhhhhhhhh");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("a.clicked", new HashMap<String, Object>() {
                    {
                        put("hhhhhhhhhh", 3);
                    }
                });
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                    when(mock.getMap()).thenReturn(data);
                    when(mock.getMap("a.clicked")).thenAnswer(invocation -> new HashMap<String, Object>() {
                        {
                            put("hhhhhhhhhh", 2);
                        }
                    });
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(data, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_writeWithNullValue() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "write");
                                        put("key", "key1");
                                        put("value", null);
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key1", "value1");
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<Map> updateProfileMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).updateOrDelete(updateProfileMapCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            assertEquals(new HashMap<String, Object>() {
                {
                    put("key1", null);
                }
            }, updateProfileMapCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_delete() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "csp");
                                put("id", "xxx");
                                put("detail", new HashMap<String, Object>() {
                                    {
                                        put("operation", "delete");
                                        put("key", "key1");
                                    }
                                });
                            }
                        });
                    }
                }).build();
        Map<String, Object> data = new HashMap<String, Object>() {
            {
                put("key", "value");
            }
        };
        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class,
                (mock, context) -> {
                    when(mock.getMap()).thenReturn(data);
                    when(mock.loadPersistenceData()).thenReturn(true);
                    when(mock.persist()).thenReturn(true);
                })) {
            userProfileExtension.readyForEvent(null);
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            // verify loading the stored data from the shared preference.
            // 1. initialized a PersistentProfileData instance.
            assertEquals(1, profileDataMocks.constructed().size());
            // 2. loadPersistenceData()/updateOrDelete()/persist() were called.
            ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(profileDataMocks.constructed().get(0), times(1)).loadPersistenceData();
            verify(profileDataMocks.constructed().get(0), times(1)).delete(listCaptor.capture());
            verify(profileDataMocks.constructed().get(0), times(1)).persist();
            verify(profileDataMocks.constructed().get(0), times(1)).getMap();
            List<String> keys = Collections.singletonList("key1");
            assertEquals(keys, listCaptor.getValue());
            // 3. a shared state for UserProfile extension was created and an Event was dispatched with the loaded profile data.
            verifySharedSateAndDispatchedEvent(ruleConsequenceEvent, data);
        }
    }

    @Test
    public void test_handleRulesEvent_otherConsequence() {
        Event ruleConsequenceEvent = new Event.Builder(
                "Consequence Rule",
                "com.adobe.eventType.rulesEngine",
                "com.adobe.eventSource.responseContent")
                .setEventData(new HashMap<String, Object>() {
                    {
                        put("triggeredconsequence", new HashMap<String, Object>() {
                            {
                                put("type", "x");
                            }
                        });
                    }
                }).build();

        try (MockedConstruction<ProfileData> profileDataMocks = mockConstruction(ProfileData.class)) {
            userProfileExtension.handleRulesEvent(ruleConsequenceEvent);
            verifyNoInteractions(extensionApiMock);
            assertEquals(0, profileDataMocks.constructed().size());
        }
    }

    private void verifySharedSateAndDispatchedEvent(Event triggerEvent, Map<String, Object> eventData) {
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApiMock, times(1)).createSharedState(mapCaptor.capture(), eventCaptor.capture());
        ArgumentCaptor<Event> eventCaptor2 = ArgumentCaptor.forClass(Event.class);
        verify(extensionApiMock, times(1)).dispatch(eventCaptor2.capture());
        Object profileData = mapCaptor.getValue().get("userprofiledata");
        assertTrue(profileData instanceof Map);
        assertEquals(eventData, ((Map<?, ?>) profileData));
        assertEquals(triggerEvent, eventCaptor.getValue());
        Event dispatchedEvent = eventCaptor2.getValue();
        assertNotNull(dispatchedEvent);
        assertEquals("UserProfile Response Event", dispatchedEvent.getName());
        assertEquals("com.adobe.eventSource.responseProfile", dispatchedEvent.getSource());
        assertEquals("com.adobe.eventType.userProfile", dispatchedEvent.getType());
        assertEquals(eventData, ((Map<?, ?>) profileData));
    }
}
