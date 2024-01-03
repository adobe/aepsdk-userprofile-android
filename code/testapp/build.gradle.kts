plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = BuildConstants.ProjectConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = "com.adobe.mobile.marketing.aep.testapp"
        minSdk = BuildConstants.ProjectConfig.MIN_SDK_VERSION
        targetSdk = BuildConstants.ProjectConfig.TARGET_SDK_VERSION
        versionCode = BuildConstants.ProjectConfig.VERSION_CODE
        versionName = BuildConstants.ProjectConfig.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.RELEASE) {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = BuildConstants.ProjectConfig.JAVA_SOURCE_COMPATIBILITY
        targetCompatibility = BuildConstants.ProjectConfig.JAVA_TARGET_COMPATIBILITY
    }

    kotlinOptions {
        jvmTarget = BuildConstants.ProjectConfig.KOTLIN_JVM_TARGET
        languageVersion = BuildConstants.ProjectConfig.KOTLIN_LANGUAGE_VERSION
        apiVersion = BuildConstants.ProjectConfig.KOTLIN_API_VERSION
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = BuildConstants.ProjectConfig.COMPOSE_VERSION
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        namespace = "com.adobe.mobile.marketing.aep.testapp"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${BuildConstants.ProjectConfig.KOTLIN_VERSION}")
    implementation("androidx.compose.ui:ui:$BuildConstants.COMPOSE_VERSION")
    implementation("androidx.compose.material:material:${BuildConstants.ProjectConfig.COMPOSE_VERSION}")
    implementation("androidx.compose.ui:ui-tooling-preview:${BuildConstants.ProjectConfig.COMPOSE_VERSION}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation(project(":userprofile"))
    implementation("com.adobe.marketing.mobile:core:2.0.1")
    implementation("com.adobe.marketing.mobile:signal:2.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${BuildConstants.ProjectConfig.COMPOSE_VERSION}")
    debugImplementation("androidx.compose.ui:ui-tooling:${BuildConstants.ProjectConfig.COMPOSE_VERSION}")
}