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
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.DataReaderException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

class ProfileData {

    private static final String USER_PROFILE_DATASTORE_NAME = "ADBUserProfile";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String EMPTY_JSON = "{}";
    private static final String CLASS_NAME = "PersistentProfileData";
    private final NamedCollection namedCollection;
    private Map<String, Object> data = new HashMap<>();

    ProfileData() throws MissingPlatformServicesException {
        namedCollection =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(USER_PROFILE_DATASTORE_NAME);
        if (namedCollection == null)
            throw new MissingPlatformServicesException(
                    "Failed to create a NamedCollection service with the collection name"
                            + " [ADBUserProfile]");
    }

    @VisibleForTesting
    ProfileData(final NamedCollection namedCollection) {
        this.namedCollection = namedCollection;
    }

    /**
     * Loads the saved profile json string into the internal Map.
     *
     * @return {@code True} indicating the stored data was retrieved and parsed to a {@code Map}
     *     correctly
     */
    boolean loadPersistenceData() {
        String json = namedCollection.getString(KEY_USER_PROFILE, EMPTY_JSON);
        try {
            if (json == null) return true;
            JSONObject jsonObject = new JSONObject(json);
            this.data = JSONUtils.convertJsonObjectToNestedMap(jsonObject);
            return true;
        } catch (JSONException e) {
            Log.error(
                    UserProfileConstants.LOG_TAG,
                    CLASS_NAME,
                    "Could not load persistent profile data: %s",
                    e);
            return false;
        }
    }

    /**
     * Persist the internal {@code Map} to disk. The disk copy is a json string of the Map.
     *
     * @return {@code True} indicating if saving to disk was successful.
     */
    boolean persist() {
        try {
            if (namedCollection == null) return false;
            String json = new JSONObject(data).toString();
            namedCollection.setString(KEY_USER_PROFILE, json);
            Log.trace(
                    UserProfileConstants.LOG_TAG,
                    CLASS_NAME,
                    "Profile Data is persisted : %s",
                    json);
            return true;
        } catch (Exception e) {
            Log.error(
                    UserProfileConstants.LOG_TAG,
                    CLASS_NAME,
                    "Profile Data is not persisted : %s",
                    e);
            return false;
        }
    }

    /**
     * Deletes the given keys from the internal map.
     *
     * @param keys The {@link String} keys which have to be deleted
     */
    void delete(@NonNull final List<String> keys) {
        for (String key : keys) {
            data.remove(key);
        }
    }

    /**
     * Method to get the profile value for the provided key. Returns null if the key does not exist.
     *
     * @param key A {@link String} profile key
     * @return the {@link Object} value for the given key
     */
    @Nullable Object get(final String key) {
        return data.get(key);
    }

    /**
     * Method to get the profile value for the provided key. Returns null if the key does not exist.
     *
     * @param key A {@link String} profile key
     * @return the {@link Map} value for the given key
     */
    @Nullable Map<String, Object> getMap(final String key) {
        try {
            return DataReader.getTypedMap(Object.class, data, key);
        } catch (DataReaderException e) {
            return null;
        }
    }

    /**
     * Update the internal map with the key and value supplied.
     *
     * <p>These are the update rules:
     *
     * <ul>
     *   <li>If the attribute key did not exist before, it will be created.
     *   <li>If it did exist, it will be updated.
     *   <li>If it did exist, and the attribute value is null, the key will be deleted from the map.
     * </ul>
     *
     * @param profileAttributes A {@code Map} of the profile data to be updated.
     */
    void updateOrDelete(@NonNull final Map<String, Object> profileAttributes) {
        for (Map.Entry<String, Object> entry : profileAttributes.entrySet()) {
            updateOrDelete(entry.getKey(), entry.getValue());
        }
    }

    private void updateOrDelete(@NonNull final String key, @Nullable final Object value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }

    /**
     * Returns a {@code Collections#unmodifiableMap(Map)} copy of the internal Map.
     *
     * @return A copy of the internal {@link Map}
     */
    Map<String, Object> getMap() {
        return Collections.unmodifiableMap(data);
    }
}
