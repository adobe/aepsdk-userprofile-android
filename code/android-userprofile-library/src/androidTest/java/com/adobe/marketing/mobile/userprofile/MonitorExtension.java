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

import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;

import java.util.concurrent.atomic.AtomicReference;

public class MonitorExtension extends Extension {
    static AtomicReference<MonitorExtension> MONITOR_EXTENSION_INSTANCE = new AtomicReference<>(null);

    /**
     * Construct the extension and initialize with the {@code ExtensionApi}.
     *
     * @param extensionApi the {@link ExtensionApi} this extension will use
     */
    protected MonitorExtension(ExtensionApi extensionApi) {
        super(extensionApi);
    }

    @Override
    protected String getName() {
        return "Monitor";
    }

    @Override
    protected void onRegistered() {
        MONITOR_EXTENSION_INSTANCE.set(this);
    }

}
