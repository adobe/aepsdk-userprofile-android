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

final class UserProfileConstants {
    static final String LOG_TAG = "UserProfile";
    static final String FRIENDLY_NAME = "UserProfile";
    static final String MODULE_NAME = "com.adobe.module.userProfile";

    private UserProfileConstants() {
    }

    static final class EventDataKeys {

        private EventDataKeys() {
        }

        static final class RuleEngine {
            static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";
            static final String CONSEQUENCE_JSON_ID = "id";
            static final String CONSEQUENCE_JSON_TYPE = "type";
            static final String CONSEQUENCE_JSON_DETAIL = "detail";
            static final String CONSEQUENCE_OPERATION_WRITE = "write";
            static final String CONSEQUENCE_OPERATION_DELETE = "delete";
            static final String RULES_CONSEQUENCE_KEY_CSP = "csp";

            private RuleEngine() {
            }
        }

        static final class UserProfile {

            /**
             * This is the EventData key for the UserProfile Response event. The value for the key
             * will be a {@link java.util.Map}. Use {@code EventData.getObject(String)} to read the value.
             */
            static final String USER_PROFILE_DATA_KEY = "userprofiledata";

            /**
             * This is the EventData key for the UserProfile Request Profile event. The value expected by the
             * UserProfileExtension for this key is a {@link java.util.Map}.
             */
            static final String UPDATE_DATA_KEY = "userprofileupdatekey";


             static final String GET_DATA_ATTRIBUTES = "userprofilegetattributes";

            /**
             * This is the EventData key for the UserProfile Request Reset event.
             */
            static final String REMOVE_DATA_KEYS = "userprofileremovekeys";


            /**
             * This is the EventData key for the Rules Response content event. A {@link String} value is expected indicating
             * the type of operation (write or delete).
             */
            static final String CONSEQUENCE_OPERATION = "operation";

            /**
             * This is the EventData key for the Rules Response content event. A {@link String} value representing a profile attribute
             * key
             */
            static final String CONSEQUENCE_KEY = "key";

            /**
             * This is the EventData key for the Rules Response content event. A {@link String} value representing token expanded
             * profile attribute value
             */
            static final String CONSEQUENCE_VALUE = "value";

            private UserProfile() {
            }
        }
    }

    /**
     * This class groups the profile keys that are maintained by the User Profile extension
     * as a aggregated count of the number of occurrences.
     */
    static class AggregatedKeys {
        private AggregatedKeys() {
        }

        /**
         * The value is a Map of {messageId:Count} of the number of times a message was triggered.
         */
        static final String ADOBE_MESSAGE_TRIGGERED = "a.triggered";
        /**
         * The value is a Map of {messageId:Count} of the number of times a message was viewed.
         */
        static final String ADOBE_MESSAGE_VIEWED = "a.viewed";
        /**
         * The value is a Map of {messageId:Count} of the number of times a message was clicked.
         */
        static final String ADOBE_MESSAGE_CLICKED = "a.clicked";
    }
}
