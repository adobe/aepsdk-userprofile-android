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

package com.adobe.marketing.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import com.adobe.marketing.mobile.userprofile.UserProfileExtension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PublicAPITests {

    @SuppressWarnings("ConstantConditions")
    @Before
    public void setup() {}

    @Test
    public void test_publicExtensionConstant() {
        assertEquals(UserProfileExtension.class, UserProfile.EXTENSION);
    }

    @Test
    public void test_updateUserAttributes() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            Map<String, Object> profileMap =
                    new HashMap<String, Object>() {
                        {
                            put("Key1", "Value1");
                            put("Key2", "Value2");
                        }
                    };
            UserProfile.updateUserAttributes(profileMap);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("UserProfileUpdate", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.userProfile", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestProfile", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("userprofileupdatekey"));
            assertEquals(profileMap, eventData.get("userprofileupdatekey"));
        }
    }

    @Test
    public void test_updateUserAttributes_withNullMap() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            UserProfile.updateUserAttributes(null);
            mobileCoreMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void test_removeUserAttributes() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            List<String> keys = Arrays.asList("key1", "key2");
            UserProfile.removeUserAttributes(keys);
            mobileCoreMockedStatic.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("RemoveUserProfile", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.userProfile", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestReset", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("userprofileremovekeys"));
            assertEquals(keys, eventData.get("userprofileremovekeys"));
        }
    }

    @Test
    public void test_removeUserAttributes_withNullKey() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            UserProfile.removeUserAttributes(null);
            mobileCoreMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void test_getUserAttributes() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            List<String> keys = Arrays.asList("key1", "key2");
            UserProfile.getUserAttributes(
                    keys,
                    new AdobeCallbackWithError<Map<String, Object>>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(Map<String, Object> stringObjectMap) {}
                    });
            mobileCoreMockedStatic.verify(
                    () ->
                            MobileCore.dispatchEventWithResponseCallback(
                                    eventCaptor.capture(), anyLong(), any()));
            Event dispatchedEvent = eventCaptor.getValue();
            assertNotNull(dispatchedEvent);
            assertEquals("getUserAttributes", dispatchedEvent.getName());
            assertEquals("com.adobe.eventType.userProfile", dispatchedEvent.getType());
            assertEquals("com.adobe.eventSource.requestProfile", dispatchedEvent.getSource());
            Map<String, Object> eventData = dispatchedEvent.getEventData();
            assertTrue(eventData.containsKey("userprofilegetattributes"));
            assertEquals(keys, eventData.get("userprofilegetattributes"));
        }
    }

    @Test
    public void test_getUserAttributes_withoutCallback() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            List<String> keys = Arrays.asList("key1", "key2");
            UserProfile.getUserAttributes(keys, null);
            mobileCoreMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void test_getUserAttributes_withNullKey() {
        try (MockedStatic<MobileCore> mobileCoreMockedStatic =
                Mockito.mockStatic(MobileCore.class)) {
            mobileCoreMockedStatic.reset();
            UserProfile.getUserAttributes(
                    null,
                    new AdobeCallbackWithError<Map<String, Object>>() {
                        @Override
                        public void fail(AdobeError adobeError) {}

                        @Override
                        public void call(Map<String, Object> stringObjectMap) {
                            assertNotNull(stringObjectMap);
                            assertTrue(stringObjectMap.isEmpty());
                        }
                    });
            mobileCoreMockedStatic.verifyNoInteractions();
        }
    }
}
