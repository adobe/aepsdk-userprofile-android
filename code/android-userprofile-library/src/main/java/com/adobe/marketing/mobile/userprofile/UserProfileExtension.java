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

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Log;
import com.adobe.marketing.mobile.UserProfile;
import com.adobe.marketing.mobile.internal.utility.StringUtils;
import com.adobe.marketing.mobile.utils.DataReader;
import com.adobe.marketing.mobile.utils.DataReaderException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The UserProfileExtension will be responsible for managing the Client Side Operation Profile.
 *
 * <p>
 * With UserProfileExtension one should be able to store attributes about my user on the client so that it can be used
 * later for targeting and personalizing the messaging to them during online or offline scenarios without needing to
 * connect to a server for optimal performance.
 * <p>
 * The UserProfileExtension will provide a way to react to Public facing APIs to update user profile attributes,
 * share the user profile attributes with the rest of the system through shared states
 * <p>
 * The UserProfileExtension listens for the following {@link Event}s:
 * <ol>
 *   <li>{@code EventType#USERPROFILE} - {@code EventSource#REQUEST_PROFILE}</li>
 *   <li>{@code EventType#RULES_ENGINE} - {@code EventSource#RESPONSE_CONTENT}</li>
 * 	 <li>{@code EventType#HUB} - {@code EventSource#BOOTED}</li>
 * 	 <li>{@code EventType.USERPROFILE} - {@code EventSource#REQUEST_RESET}</li>
 * </ol>
 * <p>
 * The UserProfileExtension dispatches the following {@code Event}s:
 * <ol>
 *   <li>{@code EventType.USERPROFILE} - {@code EventSource.RESPONSE_PROFILE}</li>
 * </ol>
 * <p>
 */
public class UserProfileExtension extends Extension {

    static final String LOG_TAG = "UserProfileExtension";
    private ProfileData profileData;

    protected UserProfileExtension(ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getVersion() {
        return UserProfile.extensionVersion();
    }

    @NonNull
    @Override
    protected String getName() {
        return UserProfileConstants.MODULE_NAME;
    }

    @Override
    protected String getFriendlyName() {
        return UserProfileConstants.FRIENDLY_NAME;
    }

    @Override
    protected void onRegistered() {
        getApi().registerEventListener(
                EventType.USERPROFILE,
                EventSource.REQUEST_PROFILE,
                this::handleProfileRequestEvent
        );
        getApi().registerEventListener(
                EventType.USERPROFILE,
                EventSource.REQUEST_RESET,
                this::handleProfileResetEvent
        );
        getApi().registerEventListener(
                EventType.RULES_ENGINE,
                EventSource.RESPONSE_CONTENT,
                this::handleRulesEvent
        );
        if (loadProfileDataIfNeeded() && !profileData.getMap().isEmpty()) {
            updateSharedStateAndDispatchEvent(null);
        }
    }

    void handleProfileRequestEvent(@NonNull final Event event) {
        if (profileData == null) {
            Log.debug(LOG_TAG, "Unable to work with Persisted profile data.");
            return;
        }
        Map<String, Object> eventData = event.getEventData();

        if (eventData == null) {
            Log.debug(UserProfileExtension.LOG_TAG, "Unexpected Null Value (Event data). Ignoring event");
            return;
        }

        if (eventData.containsKey(UserProfileConstants.EventDataKeys.UserProfile.UPDATE_DATA_KEY)) {
            handleProfileUpdateEvent(event);
        } else if (eventData.containsKey(UserProfileConstants.EventDataKeys.UserProfile.GET_DATA_ATTRIBUTES)) {
            handleProfileGetAttributesEvent(event);
        } else {
            Log.debug(UserProfileExtension.LOG_TAG, "No update/get request key in eventData. Ignoring event");
        }
    }

    void handleProfileResetEvent(@NonNull final Event event) {
        if (profileData == null) {
            Log.debug(LOG_TAG, "Unable to work with Persisted profile data.");
            return;
        }
        Map<String, Object> eventData = event.getEventData();

        if (eventData == null) {
            Log.debug(UserProfileExtension.LOG_TAG,
                    "Unexpected Null Value (event data), discarding the user profile request reset event.");
            return;
        }

        if (!eventData.containsKey(UserProfileConstants.EventDataKeys.UserProfile.REMOVE_DATA_KEYS)) {
            Log.debug(UserProfileExtension.LOG_TAG, "No remove request key in eventData. Ignoring event");
            return;
        }
        handleProfileDeleteEvent(event);
    }

    /**
     * Handler for {@code EventType.USERPROFILE} - {@code EventSource.REQUEST_PROFILE} {code Event}.
     * <p>
     * This {@code Event} is dispatched when "UpdateUserProfileAttribute" public API is called.
     * Extracts the updated profile attributes {@code Map} from the {@code Event}. Attempts to update the profile attributes in persistence and in memory.
     * If succeeded, updates the userprofile shared state and dispatches {@code EventType.USERPROFILE} {@code EventSource.RESPONSE_PROFILE} {@code Event} with updated data.
     *
     * @param event {@link Event}, containing the updated profile attributes
     */
    void handleProfileUpdateEvent(@NonNull final Event event) {
        try {
            Map<String, Object> profileAttributes = DataReader.getTypedMap(
                    Object.class, event.getEventData(),
                    UserProfileConstants.EventDataKeys.UserProfile.UPDATE_DATA_KEY);
            if (profileAttributes.size() > 0) {
                updateProfilesAndDispatchSharedState(profileAttributes, event);
            }
        } catch (Exception e) {
            Log.error(LOG_TAG, "Could not extract the profile update request data from the Event.");
        }
    }

    /**
     * This {@code Event} is dispatched when "getUserAttributes" public API is called.
     * Try to retrieve profile data from memory with provided keys and dispatch {@code EventType.USERPROFILE} {@code EventSource.RESPONSE_PROFILE} {@code Event} with above profile data.
     *
     * @param event {@link Event}, containing keys of profile data which will be retrieved from memory.
     */
    void handleProfileGetAttributesEvent(@NonNull final Event event) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<String> nameList = DataReader.getTypedList(String.class, event.getEventData(),
                    UserProfileConstants.EventDataKeys.UserProfile.GET_DATA_ATTRIBUTES);
            if (nameList != null && nameList.size() > 0) {
                for (String name : nameList) {
                    Object attribute = profileData.get(name);

                    if (attribute != null) {
                        map.put(name, attribute);
                    }
                }
            } else {
                return;
            }
        } catch (DataReaderException e) {
            Log.error(LOG_TAG, "Could not extract the profile request data from the Event - (%s)", e);
            return;
        } catch (Exception e) {
            Log.error(LOG_TAG, "Could not find specific data from persisted profile data - (%s)", e);
            return;
        }

        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(UserProfileConstants.EventDataKeys.UserProfile.GET_DATA_ATTRIBUTES,
                map);
        final Event responseEvent = new Event.Builder(
                "UserProfile Response Event",
                EventType.USERPROFILE,
                EventSource.RESPONSE_PROFILE)
                .setEventData(eventDataMap).inResponseToEvent(event).build();
        getApi().dispatch(responseEvent);
    }

    /**
     * Handler for {@code EventType.USERPROFILE} - {@code EventSource.REQUEST_RESET} {@code Event}.
     * <p>
     * This event is created when "RemoveUserProfileAttribute" public API is called.
     * Extracts the key to be deleted from the {@code Event}. Attempts to delete the profile key from persistence and from memory.
     * If succeeded, updates the userprofile shared state and dispatches {@code EventType.USERPROFILE} {@code EventSource.RESPONSE_PROFILE} {@code Event} with updated data.
     *
     * @param event {@code EventType#USERPROFILE} - {@code EventSource#REQUEST_RESET}  {@link Event}
     */
    void handleProfileDeleteEvent(@NonNull final Event event) {
        try {
            List<String> deleteKeys = DataReader.getTypedList(String.class, event.getEventData(),
                    UserProfileConstants.EventDataKeys.UserProfile.REMOVE_DATA_KEYS);
            if (deleteKeys.size() > 0) {
                deleteProfileAndDispatchSharedState(deleteKeys, event);
            }
        } catch (Exception e) {
            Log.error(LOG_TAG, "Could not extract the profile request data from the Event - (%s)", e);
        }
    }

    /**
     * Handler for {@code EventType.RULES_ENGINE} - {@code EventSource.RESPONSE_CONTENT} {@code Event}.
     * <p>
     * This method is called when a Rule with UserProfileExtension consequence has been triggered.
     * This method extracts the {@link String} operation value to be performed from the consequence detail {@link Map}.
     * <ul>
     *   <li> Calls {@link UserProfileExtension#handleWriteConsequence(Map, Event)} if it is consequence with write operation</li>
     *   <li> Calls {@link UserProfileExtension#handleDeleteConsequence(Map, Event)} if it is consequence with delete operation</li>
     *   <li> Logs and returns if it is consequence with invalid operation</li>
     * </ul>
     *
     * @param event an {@code EventType#RULES_ENGINE} - {@code EventSource#RESPONSE_CONTENT}  {@link Event}
     */
    void handleRulesEvent(@NonNull final Event event) {
        if (profileData == null) {
            Log.debug(LOG_TAG, "Unable to work with Persisted profile data.");
            return;
        }
        try {
            Map<String, Object> triggeredConsequence = DataReader.getTypedMap(Object.class, event.getEventData(),
                    UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED);
            if (triggeredConsequence == null || triggeredConsequence.isEmpty()) {
                return;
            }
            String consequenceType = DataReader.getString(triggeredConsequence,
                    UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_TYPE);
            if (!UserProfileConstants.EventDataKeys.RuleEngine.RULES_CONSEQUENCE_KEY_CSP.equals(consequenceType)) {
                return;
            }
            String consequenceId = DataReader.getString(triggeredConsequence,
                    UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_ID);
            Map<String, Object> consequenceDetail = DataReader.getTypedMap(Object.class, triggeredConsequence,
                    UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_DETAIL);

            if (consequenceDetail == null || consequenceDetail.isEmpty()) {
                Log.debug(UserProfileExtension.LOG_TAG,
                        "Unable to process UserProfileExtension Consequence. Invalid detail provided for consequence id (%s)", consequenceId);
                return;
            }
            Log.debug(UserProfileExtension.LOG_TAG,
                    "Processing UserProfileExtension Consequence with id (%s)", consequenceId);
            String operation = DataReader.getString(consequenceDetail,
                    UserProfileConstants.EventDataKeys.UserProfile.CONSEQUENCE_OPERATION);
            // Consequence performing write operation
            if (UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_OPERATION_WRITE.equals(operation)) {
                handleWriteConsequence(consequenceDetail, event);
            } else if (UserProfileConstants.EventDataKeys.RuleEngine.CONSEQUENCE_OPERATION_DELETE.equals(operation)) {
                handleDeleteConsequence(consequenceDetail, event);
            } else {
                Log.debug(LOG_TAG, "Invalid UserProfileExtension consequence operation");
            }
        } catch (Exception exp) {
            Log.error(UserProfileExtension.LOG_TAG,
                    "Could not extract the consequence information from the rules response event - (%s)",
                    exp);
        }
    }

    /**
     * This method is called to handle write-operation consequence on the UserProfileExtension.
     * <p>
     * This method extracts the {@code String} key and {@code String} value from the consequence details and attempts to update the profile.
     * If the key relates to any of the IAM triggered/viewed or clicked keys. Message aggregate table is created and updated.
     * On successful update, a valid shared state for the given {#sharedStateVersion} will be created and an
     * {@code EventType.USERPROFILE} - {@code EventSource.RESPONSE_PROFILE} {@code Event} is dispatched.
     *
     * @param consequenceDetails a {@link Map} representing the consequence details with write profile key and value
     * @param event              The {@link Event} for which the shared state is being set.
     */
    private void handleWriteConsequence(@NonNull final Map<String, Object> consequenceDetails, @NonNull final Event event) {
        try {
            String writeKey = DataReader.getString(consequenceDetails, UserProfileConstants.EventDataKeys.UserProfile.CONSEQUENCE_KEY);
            Object writeValue = consequenceDetails.get(UserProfileConstants.EventDataKeys.UserProfile.CONSEQUENCE_VALUE);
            if (StringUtils.isNullOrEmpty(writeKey)) {
                Log.debug(LOG_TAG, "Invalid write key from the user profile consequence");
                return;
            }
            Object updatedWriteValue = (writeValue == null) ? null : replaceValueForIAMKey(writeKey, writeValue);
            Map<String, Object> profileAttribute = new HashMap<>();
            profileAttribute.put(writeKey, updatedWriteValue);
            updateProfilesAndDispatchSharedState(profileAttribute, event);
        } catch (Exception e) {
            Log.error(LOG_TAG, "Could not extract the profile update request data from the rule consequence details.");
        }
    }

    /**
     * This method is called to handle delete-operation consequence on the userProfileExtension.
     * <p>
     * This method extracts the {@code String} delete key from the consequence details and attempts to remove the given profile.
     * On successful deletion, a valid shared state for the given {#sharedStateVersion} will be created and an
     * {@code EventType.USERPROFILE} - {@code EventSource.RESPONSE_PROFILE} {@code Event} is dispatched.
     *
     * @param consequenceDetails a {@link Map} representing the consequence details with delete profile key
     * @param event              The {@link Event} for which the shared state is being set.
     */
    private void handleDeleteConsequence(@NonNull final Map<String, Object> consequenceDetails, @NonNull final Event event) {
        try {
            String deleteKey = DataReader.getString(consequenceDetails,
                    UserProfileConstants.EventDataKeys.UserProfile.CONSEQUENCE_KEY);
            if (StringUtils.isNullOrEmpty(deleteKey)) {
                Log.debug(LOG_TAG, "Invalid delete key from the user profile consequence");
                return;
            }
            List<String> profileKeys = new ArrayList<>(1);
            profileKeys.add(deleteKey);
            deleteProfileAndDispatchSharedState(profileKeys, event);
        } catch (Exception e) {
            Log.error(LOG_TAG, "Could not extract the profile update request data from the rule consequence details.");
        }
    }

    /**
     * Called when the UserProfileExtension needs to update the {@code PersistentProfileData} instance with a {@code Map} of profile attributes.
     * <p>
     * This method returns false when
     * <ul>
     *   <li> Unable to instantiate {@code PersistentProfileData} instance because of missing platform service.</li>
     *   <li> Invalid or null key.</li>
     * </ul>
     *
     * @param profileAttribute {@link Map} of profile attributes with key-value pair that needs to be updated
     */
    private void updateProfilesAndDispatchSharedState(@NonNull final Map<String, Object> profileAttribute, @NonNull final Event event) {
        profileData.updateOrDelete(profileAttribute);
        if (profileData.persist()) {
            updateSharedStateAndDispatchEvent(event);
        }

    }

    /**
     * Called when the UserProfileExtension needs to delete an attribute from {@code PersistentProfileData} instance.
     * <p>
     * This method returns false,
     * <ul>
     *   <li> when unable to instantiate {@code PersistentProfileData} instance because of missing platform service.</li>
     *   <li> when on invalid or null key.</li>
     * </ul>
     *
     * @param keys the {@link List<String>} profile keys that needs to be deleted
     */
    private void deleteProfileAndDispatchSharedState(@NonNull final List<String> keys, @NonNull final Event event) {
        profileData.delete(keys);
        if (profileData.persist()) {
            updateSharedStateAndDispatchEvent(event);
        }
    }

    /**
     * This method creates/maintains a message aggregate table, keeping track of the count of message triggered/clicked/viewed
     * for each messageId.
     * <p>
     * Checks if the provided key is equal to either of the below given special keys of an IAM message.
     * <ul>
     *     <li>Triggered - {@code StandardProfileKeys.AggregatedKeys#ADOBE_MESSAGE_TRIGGERED} </li>
     *     <li>Clicked - {@code StandardProfileKeys.AggregatedKeys#ADOBE_MESSAGE_CLICKED} </li>
     *     <li>Viewed - {@code StandardProfileKeys.AggregatedKeys#ADOBE_MESSAGE_VIEWED} </li>
     * </ul>
     * If found, increases the count for the messageID under (Triggered/Clicked/Viewed) and returns the {@code Map} of message aggregate table.
     * If Not found, returns the original {code String} value.
     *
     * @param key   A {@link String} key that may contain special IAM keys
     * @param value A {@code String} value that may contain messageID.
     * @return {@link Object} Returns a {@link Map} of message aggregate table if the key is a special IAM key,
     * else return the given input {@code String} value.
     * @see UserProfileConstants.AggregatedKeys
     */
    private Object replaceValueForIAMKey(@NonNull final String key, @Nullable final Object value) {
        if ((key.equals(UserProfileConstants.AggregatedKeys.ADOBE_MESSAGE_TRIGGERED) ||
                key.equals(UserProfileConstants.AggregatedKeys.ADOBE_MESSAGE_CLICKED) ||
                key.equals(UserProfileConstants.AggregatedKeys.ADOBE_MESSAGE_VIEWED))) {
            //Get the messages triggered/viewed/clicked table
            //update the count of the message triggered
            Map<String, Object> messagesAggregateMap = profileData.getMap(key);
            if (messagesAggregateMap == null) {
                messagesAggregateMap = new HashMap<>();
            }
            String messageId = String.valueOf(value);
            int count = DataReader.optInt(messagesAggregateMap, messageId, 0);
            messagesAggregateMap.put(messageId, ++count);
            return messagesAggregateMap;
        } else {
            return value;
        }
    }

    /**
     * Updates the UserProfileExtension shared state and dispatches an {@code EventType.USERPROFILE} - {@code EventSource.RESPONSE_PROFILE} {@code Event}.
     * <p>
     * Creates an {@code EventData} from {@code #profileData} instance, then updates the shared state and dispatches the
     * event with the prepared {@code EventData}
     *
     * @param event The {@link Event} for which the shared state is being set.
     */
    private void updateSharedStateAndDispatchEvent(@Nullable final Event event) {
        Map<String, Object> eventDataMap = new HashMap<>();
        if (profileData != null) {
            eventDataMap.put(UserProfileConstants.EventDataKeys.UserProfile.USER_PROFILE_DATA_KEY,
                    profileData.getMap());
        }

        getApi().createSharedState(
                eventDataMap,
                event
        );

        final Event responseEvent = new Event.Builder(
                "UserProfile Response Event",
                EventType.USERPROFILE,
                EventSource.RESPONSE_PROFILE).setEventData(eventDataMap).build();
        getApi().dispatch(responseEvent);
    }

    /**
     * Attempts to instantiate the instance of {@code PersistentProfileData} to {@code #profileData} variable.
     * <p>
     * No operation is performed if the {@link #profileData} variable is already instantiated.
     * This method returns false when, unable to instantiate {@code PersistentProfileData} instance because of missing platform service.
     *
     * @return {@link boolean} indicating whether the instantiation of {@link #profileData} variable was successful
     */
    private boolean loadProfileDataIfNeeded() {
        if (profileData == null) {
            try {
                profileData = new ProfileData();
            } catch (MissingPlatformServicesException e) {
                Log.debug(LOG_TAG, "Unable to work with Persisted profile data - (%s)", e);
                return false;
            }
            return profileData.loadPersistenceData();
        }
        return true;
    }

}
