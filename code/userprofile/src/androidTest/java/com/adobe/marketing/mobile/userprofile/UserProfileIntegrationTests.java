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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.SDKHelper;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.UserProfile;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.DataReaderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileIntegrationTests {

    @Before
    public void setup() throws InterruptedException {
        SDKHelper.resetSDK();
        MobileCore.setApplication(ApplicationProvider.getApplicationContext());
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences sharedPreference = context.getSharedPreferences("ADBUserProfile", 0);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.clear();
        editor.commit();

        final CountDownLatch latch = new CountDownLatch(1);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);
        List<Class<? extends Extension>> extensions = new ArrayList<>();
        extensions.add(UserProfile.EXTENSION);
        extensions.add(MonitorExtension.class);
        MobileCore.registerExtensions(extensions, o -> latch.countDown());
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test(timeout = 10000)
    public void testUpdateUserAttributesWithAllSupportedTypes() throws InterruptedException {
        updateUserAttributesWithAllSupportedTypes();
    }

    private void updateUserAttributesWithAllSupportedTypes() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                        put("k4", true);
                    }
                });

        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(4, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k1", "value");
                                    put("k2", 2.1);
                                    put("k3", 3);
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    private void sleep(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
        }
    }

    @Test(timeout = 10000)
    public void testExtensionRegistrationLoadLocalDataAndCreateSharedState()
            throws InterruptedException {
        updateUserAttributesWithAllSupportedTypes();

        SDKHelper.resetSDK();
        MobileCore.setApplication(ApplicationProvider.getApplicationContext());
        final CountDownLatch latch = new CountDownLatch(1);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);
        List<Class<? extends Extension>> extensions = new ArrayList<>();
        extensions.add(UserProfile.EXTENSION);
        extensions.add(MonitorExtension.class);
        MobileCore.registerExtensions(
                extensions,
                o -> {
                    sleep(100);
                    assertNotNull(MonitorExtension.MONITOR_EXTENSION_INSTANCE.get());
                    SharedStateResult result =
                            MonitorExtension.MONITOR_EXTENSION_INSTANCE
                                    .get()
                                    .getApi()
                                    .getSharedState(
                                            "com.adobe.module.userProfile",
                                            null,
                                            false,
                                            SharedStateResolution.LAST_SET);
                    Map<String, Object> data = result == null ? null : result.getValue();
                    assertNotNull(data);
                    assertFalse(data.isEmpty());
                    try {
                        Map<String, Object> map =
                                DataReader.getTypedMap(Object.class, data, "userprofiledata");
                        assertEquals(4, map.keySet().size());
                    } catch (DataReaderException e) {
                        fail();
                    }
                    latch.countDown();
                });
        latch.await();
    }

    @Test(timeout = 1000)
    public void testUpdateUserAttributesWithNullValue() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                    }
                });
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k3", null);
                        put("k4", true);
                    }
                });

        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(3, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k1", "value");
                                    put("k2", 2.1);
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    @Test(timeout = 1000)
    public void testUpdateUserAttribute() throws InterruptedException {
        UserProfile.updateUserAttribute("key", "value");

        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Collections.singletonList("key"),
                stringObjectMap -> {
                    assertEquals(1, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("key", "value");
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    @Test(timeout = 1000)
    public void testRemoveUserAttribute() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                        put("k4", true);
                    }
                });
        UserProfile.removeUserAttribute("k2");
        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(3, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k1", "value");
                                    put("k3", 3);
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    @Test(timeout = 1000)
    public void testRemoveUserAttributes() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                        put("k4", true);
                    }
                });
        UserProfile.removeUserAttributes(Arrays.asList("k1", "k2", "k3"));
        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(1, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    @Test(timeout = 1000)
    public void testRulesConsequenceEventOperationWrite() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                    }
                });
        Event consequenceEvent =
                new Event.Builder(
                                "consequence event",
                                "com.adobe.eventType.rulesEngine",
                                "com.adobe.eventSource.responseContent")
                        .setEventData(
                                new HashMap<String, Object>() {
                                    {
                                        put(
                                                "triggeredconsequence",
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("type", "csp");
                                                        put("id", "xxx");
                                                        put(
                                                                "detail",
                                                                new HashMap<String, Object>() {
                                                                    {
                                                                        put("operation", "write");
                                                                        put("key", "k4");
                                                                        put("value", true);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                })
                        .build();
        MobileCore.dispatchEvent(consequenceEvent);
        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(4, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k1", "value");
                                    put("k2", 2.1);
                                    put("k3", 3);
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }

    @Test(timeout = 1000)
    public void testRulesConsequenceEventOperationDelete() throws InterruptedException {
        UserProfile.updateUserAttributes(
                new HashMap<String, Object>() {
                    {
                        put("k1", "value");
                        put("k2", 2.1);
                        put("k3", 3);
                        put("k4", true);
                    }
                });
        Event consequenceEvent =
                new Event.Builder(
                                "consequence event",
                                "com.adobe.eventType.rulesEngine",
                                "com.adobe.eventSource.responseContent")
                        .setEventData(
                                new HashMap<String, Object>() {
                                    {
                                        put(
                                                "triggeredconsequence",
                                                new HashMap<String, Object>() {
                                                    {
                                                        put("type", "csp");
                                                        put("id", "xxx");
                                                        put(
                                                                "detail",
                                                                new HashMap<String, Object>() {
                                                                    {
                                                                        put("operation", "delete");
                                                                        put("key", "k3");
                                                                        put("value", "xxxxx");
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                })
                        .build();
        MobileCore.dispatchEvent(consequenceEvent);
        final CountDownLatch getDataLatch = new CountDownLatch(1);
        UserProfile.getUserAttributes(
                Arrays.asList("k1", "k2", "k3", "k4"),
                stringObjectMap -> {
                    assertEquals(3, stringObjectMap.size());
                    assertEquals(
                            new HashMap<String, Object>() {
                                {
                                    put("k1", "value");
                                    put("k2", 2.1);
                                    //                    put("k3", 3);
                                    put("k4", true);
                                }
                            },
                            stringObjectMap);
                    getDataLatch.countDown();
                });
        getDataLatch.await();
    }
}
