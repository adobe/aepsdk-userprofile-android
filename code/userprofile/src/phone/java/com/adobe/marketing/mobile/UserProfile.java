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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.userprofile.UserProfileExtension;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.DataReaderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile {
    private static final String LOG_TAG = "UserProfile";
    private static final String EXTENSION_VERSION = "3.0.0";
    private static final String CLASS_NAME = "UserProfile";
    public static final Class<? extends Extension> EXTENSION = UserProfileExtension.class;

    private static final String UPDATE_DATA_KEY = "userprofileupdatekey";
    private static final String GET_DATA_ATTRIBUTES = "userprofilegetattributes";
    private static final String REMOVE_DATA_KEYS = "userprofileremovekeys";

    private UserProfile() {}

    /**
     * Returns the version of the {@link UserProfile} extension
     *
     * @return The version as {@code String}
     */
    @NonNull public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * UserProfile API to set user profile attributes keys and values.
     *
     * <p>If the attribute does not exist, it will be created. If the attribute already exists, then
     * the value will be updated. A null attribute value will remove the attribute.
     *
     * <p>This API will generate a userprofile request event.
     *
     * @param attributeMap HashMap of profile attributes key-value pairs to be set.
     */
    public static void updateUserAttributes(@NonNull final Map<String, Object> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "updateUserAttributes - the given attribute map is null or empty, no event was"
                            + " dispatched");
            return;
        }
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(UPDATE_DATA_KEY, attributeMap);
        Event event =
                new Event.Builder(
                                "UserProfileUpdate",
                                EventType.USERPROFILE,
                                EventSource.REQUEST_PROFILE)
                        .setEventData(eventDataMap)
                        .build();
        MobileCore.dispatchEvent(event);
    }

    /**
     * UserProfile API to set user profile attributes keys and values.
     *
     * <p>If the attribute does not exist, it will be created. If the attribute already exists, then
     * the value will be updated. A null attribute value will remove the attribute.
     *
     * <p>This API will generate a userprofile request event.
     *
     * @param attributeName Attribute key.
     * @param attributeValue Attribute value corresponding to the key. Java primitive types, Maps
     *     and Lists are supported.
     */
    @Deprecated
    public static void updateUserAttribute(
            @NonNull final String attributeName, @Nullable final Object attributeValue) {
        if (attributeName == null || attributeName.isEmpty()) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "updateUserAttributes - attributeName is null or empty, no event was"
                            + " dispatched");
            return;
        }
        Log.trace(
                LOG_TAG,
                CLASS_NAME,
                "Updating user attribute with attribute name: %s",
                attributeName);
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(attributeName, attributeValue);
        updateUserAttributes(attributeMap);
    }

    /**
     * UserProfile API to remove the given attribute name.
     *
     * <p>If the attribute does not exist, this API has no effects. If the attribute exists, then
     * the User Attribute will be removed
     *
     * @param attributeName A {@link String} attribute key which has to be removed.
     */
    @Deprecated
    public static void removeUserAttribute(@NonNull final String attributeName) {
        if (attributeName == null || attributeName.isEmpty()) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "updateUserAttributes - attributeName is null or empty, no event was"
                            + " dispatched");
            return;
        }
        Log.trace(LOG_TAG, CLASS_NAME, "Removing user attribute with key: %s", attributeName);

        List<String> keys = new ArrayList<>(1);
        keys.add(attributeName);
        removeUserAttributes(keys);
    }

    /**
     * UserProfile API to remove attributes.
     *
     * <p>If the attribute does not exist, this API has no effects. If the attribute exists, then
     * the User Attribute will be removed
     *
     * @param attributeNames A List of attribute keys which have to be removed.
     */
    public static void removeUserAttributes(@NonNull final List<String> attributeNames) {
        if (attributeNames == null || attributeNames.isEmpty()) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "removeUserAttributes - the given attribute map is null or empty, no event was"
                            + " dispatched");
            return;
        }
        Log.trace(LOG_TAG, CLASS_NAME, "Removing user attributes");
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(REMOVE_DATA_KEYS, attributeNames);
        Event event =
                new Event.Builder(
                                "RemoveUserProfile",
                                EventType.USERPROFILE,
                                EventSource.REQUEST_RESET)
                        .setEventData(eventDataMap)
                        .build();
        MobileCore.dispatchEvent(event);
    }

    /**
     * UserProfile API to get attributes with provided keys.
     *
     * @param keys Attribute key.
     * @param callback An {@link AdobeCallback} invoked after profile attributes retrieved from
     *     memory
     */
    @SuppressWarnings("rawtypes")
    public static void getUserAttributes(
            @NonNull final List<String> keys,
            @NonNull final AdobeCallback<Map<String, Object>> callback) {

        if (callback == null) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "getUserAttributes - the given AdobeCallback is null, no event was dispatched");
            return;
        }

        if (keys == null || keys.size() == 0) {
            Log.debug(
                    LOG_TAG,
                    CLASS_NAME,
                    "getUserAttributes - the given key map is null or empty, no event was"
                            + " dispatched");
            callback.call(new HashMap<>());
            return;
        }
        Log.trace(LOG_TAG, CLASS_NAME, "Getting user attributes");

        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(GET_DATA_ATTRIBUTES, keys);
        Event event =
                new Event.Builder(
                                "getUserAttributes",
                                EventType.USERPROFILE,
                                EventSource.REQUEST_PROFILE)
                        .setEventData(eventDataMap)
                        .build();

        AdobeCallbackWithError<Map<String, Object>> adobeCallbackWithError =
                new AdobeCallbackWithError<Map<String, Object>>() {
                    final AdobeCallbackWithError userCallbackWithError =
                            callback instanceof AdobeCallbackWithError
                                    ? (AdobeCallbackWithError) callback
                                    : null;

                    @Override
                    public void fail(AdobeError adobeError) {
                        if (userCallbackWithError != null) {
                            userCallbackWithError.fail(adobeError);
                        }
                    }

                    @Override
                    public void call(Map<String, Object> profileMap) {
                        callback.call(profileMap);
                    }
                };
        MobileCore.dispatchEventWithResponseCallback(
                event,
                5000L,
                new AdobeCallbackWithError<Event>() {
                    @Override
                    public void fail(AdobeError adobeError) {
                        adobeCallbackWithError.fail(adobeError);
                    }

                    @Override
                    public void call(Event event) {
                        try {
                            Map<String, Object> profileMap =
                                    DataReader.getTypedMap(
                                            Object.class,
                                            event.getEventData(),
                                            GET_DATA_ATTRIBUTES);
                            callback.call(profileMap);
                        } catch (DataReaderException e) {
                            Log.error(
                                    LOG_TAG,
                                    CLASS_NAME,
                                    "Failed to retrieve user attributes from given user profile"
                                            + " event.");
                            adobeCallbackWithError.fail(AdobeError.UNEXPECTED_ERROR);
                        }
                    }
                });
    }
}
