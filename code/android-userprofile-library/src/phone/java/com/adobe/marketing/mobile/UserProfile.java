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

import com.adobe.marketing.mobile.userprofile.UserProfileConstants;
import com.adobe.marketing.mobile.userprofile.UserProfileExtension;
import com.adobe.marketing.mobile.utils.DataReader;
import com.adobe.marketing.mobile.utils.DataReaderException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserProfile {
    private final static String EXTENSION_VERSION = "2.0.0";
    private static final String TAG = "UserProfile";

    private UserProfile() {
    }

    /**
     * Returns the version of the {@link UserProfile} extension
     *
     * @return The version as {@code String}
     */
    public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Registers the extension with the Mobile SDK. This method should be called only once in your application class.
     */
    @Deprecated
    public static void registerExtension() {
        MobileCore.registerExtension(UserProfileExtension.class, extensionError -> {
            if (extensionError == null) {
                return;
            }
            Log.error(TAG, "There was an error when registering the UserProfile extension: %s",
                    extensionError.getErrorName());
        });
    }

    /**
     * UserProfile API to set user profile attributes keys and values.
     * <p>
     * If the attribute does not exist, it will be created. If the attribute already
     * exists, then the value will be updated. A null attribute value will remove
     * the attribute.
     * <p>
     * This API will generate a userprofile request event.
     *
     * @param attributeMap HashMap of profile attributes key-value pairs to be set.
     */
    public static void updateUserAttributes(@NotNull final Map<String, Object> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            Log.debug(TAG, "updateUserAttributes - the given attribute map is null or empty, no event was dispatched");
        }
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(UserProfileConstants.EventDataKeys.UserProfile.UPDATE_DATA_KEY, attributeMap);
        Event event = new Event.Builder(
                "UserProfileUpdate",
                EventType.USERPROFILE,
                EventSource.REQUEST_PROFILE)
                .setEventData(eventDataMap)
                .build();
        MobileCore.dispatchEvent(event);
    }

    /**
     * UserProfile API to set user profile attributes keys and values.
     * <p>
     * If the attribute does not exist, it will be created. If the attribute already
     * exists, then the value will be updated. A null attribute value will remove
     * the attribute.
     * <p>
     * This API will generate a userprofile request event.
     *
     * @param attributeName  Attribute key.
     * @param attributeValue Attribute value corresponding to the key. Java
     *                       primitive types, Maps and Lists are supported.
     */
    public static void updateUserAttribute(@NotNull final String attributeName, @Nullable final Object attributeValue) {
        if (attributeName == null || attributeName.isEmpty()) {
            Log.debug(TAG, "updateUserAttributes - attributeName is null or empty, no event was dispatched");
        }
        Log.trace(TAG, "Updating user attribute with attribute name: %s", attributeName);
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(attributeName, attributeValue);
        updateUserAttributes(attributeMap);
    }

    /**
     * UserProfile API to remove the given attribute name.
     * <p>
     * If the attribute does not exist, this API has no effects. If the attribute
     * exists, then the User Attribute will be removed
     *
     * @param attributeName A {@link String} attribute key which has to be removed.
     */
    public static void removeUserAttribute(@NotNull final String attributeName) {
        if (attributeName == null || attributeName.isEmpty()) {
            Log.debug(TAG, "updateUserAttributes - attributeName is null or empty, no event was dispatched");
        }
        Log.trace(TAG, "Removing user attribute with key: %s", attributeName);

        if (attributeName == null) {
            Log.debug(TAG, "%s (key), failed to remove user attribute", Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        List<String> keys = new ArrayList<>(1);
        keys.add(attributeName);
        removeUserAttributes(keys);
    }

    /**
     * UserProfile API to remove attributes.
     * <p>
     * If the attribute does not exist, this API has no effects. If the attribute
     * exists, then the User Attribute will be removed
     *
     * @param attributeNames A List of attribute keys which have to be removed.
     */
    public static void removeUserAttributes(@NotNull final List<String> attributeNames) {
        if (attributeNames == null || attributeNames.isEmpty()) {
            Log.debug(TAG, "removeUserAttributes - the given attribute map is null or empty, no event was dispatched");
        }
        Log.trace(TAG, "Removing user attributes");
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(UserProfileConstants.EventDataKeys.UserProfile.REMOVE_DATA_KEYS, attributeNames);
        Event event = new Event.Builder(
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
     * @param keys     Attribute key.
     * @param callback An {@link AdobeCallback} invoked after profile attributes
     *                 retrieved from memory
     */
    @SuppressWarnings("rawtypes")
    public static void getUserAttributes(@NotNull final List<String> keys, @NotNull final AdobeCallback<Map<String, Object>> callback) {

        if (callback == null) {
            Log.debug(TAG, "getUserAttributes - the given AdobeCallback is null, no event was dispatched");
            return;
        }

        if (keys == null || keys.size() == 0) {
            Log.debug(TAG, "getUserAttributes - the given key map is null or empty, no event was dispatched");
            callback.call(new HashMap<>());
            return;
        }
        Log.trace(TAG, "Getting user attributes");

        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(UserProfileConstants.EventDataKeys.UserProfile.GET_DATA_ATTRIBUTES, keys);
        Event event = new Event.Builder(
                "getUserAttributes",
                EventType.USERPROFILE,
                EventSource.REQUEST_PROFILE)
                .setEventData(eventDataMap)
                .build();

        AdobeCallbackWithError<Map<String, Object>> adobeCallbackWithError = new AdobeCallbackWithError<Map<String, Object>>() {
            final AdobeCallbackWithError userCallbackWithError = callback instanceof AdobeCallbackWithError ?
                    (AdobeCallbackWithError) callback : null;

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
        MobileCore.dispatchEventWithResponseCallback(event, 5000L, new AdobeCallbackWithError<Event>() {
            @Override
            public void fail(AdobeError adobeError) {
                adobeCallbackWithError.fail(adobeError);
            }

            @Override
            public void call(Event event) {
                try {
                    Map<String, Object> profileMap = DataReader.getTypedMap(Object.class, event.getEventData(),
                            UserProfileConstants.EventDataKeys.UserProfile.GET_DATA_ATTRIBUTES);
                    callback.call(profileMap);
                } catch (DataReaderException e) {
                    Log.error(TAG, "Failed to retrieve user attributes from given user profile event.");
                    adobeCallbackWithError.fail(AdobeError.UNEXPECTED_ERROR);
                }
            }
        });
    }

}
