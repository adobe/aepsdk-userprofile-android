import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.adobe.mobile.marketing.userprofile.testapp"

    defaultConfig {
        applicationId = "com.adobe.mobile.marketing.userprofile.testapp"
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
        compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION
        targetSdk = BuildConstants.Versions.TARGET_SDK_VERSION

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
        sourceCompatibility = BuildConstants.Versions.JAVA_SOURCE_COMPATIBILITY
        targetCompatibility = BuildConstants.Versions.JAVA_TARGET_COMPATIBILITY
    }

    kotlinOptions {
        jvmTarget = BuildConstants.Versions.KOTLIN_JVM_TARGET
        languageVersion = BuildConstants.Versions.KOTLIN_LANGUAGE_VERSION
        apiVersion = BuildConstants.Versions.KOTLIN_API_VERSION
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = BuildConstants.Versions.COMPOSE_COMPILER
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${BuildConstants.Versions.KOTLIN}")
    implementation("androidx.compose.ui:ui:${BuildConstants.Versions.COMPOSE}")
    implementation("androidx.compose.material:material:${BuildConstants.Versions.COMPOSE}")
    implementation("androidx.compose.ui:ui-tooling-preview:${BuildConstants.Versions.COMPOSE}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.3.1")

    implementation(project(":userprofile"))
    implementation("com.adobe.marketing.mobile:core:3.0.0")
    implementation("com.adobe.marketing.mobile:signal:3.0.0") 

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${BuildConstants.Versions.COMPOSE}")
    debugImplementation("androidx.compose.ui:ui-tooling:${BuildConstants.Versions.COMPOSE}")
}