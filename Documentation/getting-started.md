# Getting Started with UserProfile SDK

## Before starting

UserProfile extension has a dependency on [AEP Core SDK](https://github.com/adobe/aepsdk-core-android#readme) which must be installed to use the extension.

## Add UserProfile extension to your app

1. Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app. Add a dependency on UserProfile and Core to your mobile application. To ensure consistent builds, it is best to explicitly specify the dependency version and update them manually.

### Kotlin

```kotlin
implementation(platform("com.adobe.marketing.mobile:sdk-bom:3.+"))
implementation("com.adobe.marketing.mobile:core")
implementation("com.adobe.marketing.mobile:userprofile")
```

### Groovy

```groovy
implementation platform('com.adobe.marketing.mobile:sdk-bom:3.+')
implementation 'com.adobe.marketing.mobile:core'
implementation 'com.adobe.marketing.mobile:userprofile'
```

2. Import MobileCore and UserProfile extensions:

   ### Java

   ```java
   import com.adobe.marketing.mobile.MobileCore;
   import com.adobe.marketing.mobile.UserProfile;
   ```

   ### Kotlin

   ```kotlin
   import com.adobe.marketing.mobile.MobileCore
   import com.adobe.marketing.mobile.UserProfile
   ```

3. Import the UserProfile library into your project and register it with `MobileCore`

   ### Java

   ```java
   public class MainApp extends Application {
        private static final String APP_ID = "YOUR_APP_ID";

        @Override
        public void onCreate() {
            super.onCreate();

            MobileCore.setApplication(this);
            MobileCore.setLogLevel(LoggingMode.VERBOSE);
            MobileCore.configureWithAppID(APP_ID);

            List<Class<? extends Extension>> extensions = Arrays.asList(
                    UserProfile.EXTENSION,...);
            MobileCore.registerExtensions(extensions, o -> {
                Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
            });
        }
    }
   ```

   ### Kotlin

   ```kotlin
   class MyApp : Application() {

       override fun onCreate() {
           super.onCreate()
           MobileCore.setApplication(this)
           MobileCore.setLogLevel(LoggingMode.VERBOSE)
           MobileCore.configureWithAppID("YOUR_APP_ID")

           val extensions = listOf(UserProfile.EXTENSION, ...)
           MobileCore.registerExtensions(extensions) {
               Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
           }
       }
   }
   ```

## Next Steps

Get familiar with the various APIs offered by the AEP SDK by checking out the [UserProfile API reference](./api-reference.md).
