import org.gradle.api.JavaVersion

/**
 * A collection of constants used throughout the build scripts in this project.
 */
object BuildConstants {

    object Plugins {
        const val ANDROID_GRADLE_PLUGIN_VERSION = "8.2.0"
        const val KOTLIN_GRADLE_PLUGIN_VERSION = "1.7.0"
        const val SPOTLESS_GRADLE_PLUGIN_VERSION = "6.11.0"
    }

    object ProjectConfig {
        const val MIN_SDK_VERSION = 21
        const val COMPILE_SDK_VERSION = 34
        const val TARGET_SDK_VERSION = 34
        const val VERSION_CODE = 1
        const val VERSION_NAME = "1.0"

        val JAVA_SOURCE_COMPATIBILITY: JavaVersion = JavaVersion.VERSION_1_8
        val JAVA_TARGET_COMPATIBILITY: JavaVersion = JavaVersion.VERSION_1_8

        const val KOTLIN_VERSION = "1.7.0"
        const val COMPOSE_VERSION = "1.2.0"
        const val KOTLIN_LANGUAGE_VERSION = "1.5"
        const val KOTLIN_API_VERSION = "1.5"
        const val KOTLIN_JVM_TARGET = "1.8"
    }

    object Formatting {
        const val KTLINT_VERSION = "0.42.1"
        const val GOOGLE_JAVA_FORMAT_VERSION = "1.15.0"
        const val JAVA_TARGETS = "src/*/java/**/*.java"
        const val KOTLIN_TARGETS = "src/*/java/**/*.kt"
        const val LICENSE_HEADER_PATH = "../../config/formatter/adobe.header.txt"
    }

    object Publishing {
        const val SNAPSHOTS_URL = "https://oss.sonatype.org/content/repositories/snapshots/"
        const val RELEASES_URL = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

        const val RELEASE_PROPERTY = "release"
        const val JITPACK_PROPERTY = "jitpack"
        const val SNAPSHOT_SUFFIX = "SNAPSHOT"

        const val LICENSE_NAME = "The Apache License, Version 2.0"
        const val LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        const val LICENSE_DIST = "repo"

        const val DEVELOPER_ID = "adobe"
        const val DEVELOPER_NAME = "adobe"
        const val DEVELOPER_EMAIL = "adobe-mobile-testing@adobe.com"
        const val DEVELOPER_DOC_URL = "https://developer.adobe.com/client-sdks"

        const val SCM_CONNECTION_URL = "scm:git:github.com//adobe/aepsdk-userprofile-android.git"
        const val SCM_REPO_URL = "https://github.com/adobe/aepsdk-userprofile-android"

        const val SIGNING_GNUPG_EXECUTABLE = "gpg"
        val SIGNING_GNUPG_KEY_NAME by lazy { System.getenv("GPG_KEY_ID") }
        val SIGNING_GNUPG_PASSPHRASE by lazy { System.getenv("GPG_PASSPHRASE") }
    }

    object Reporting {
        const val UNIT_TEST_EXECUTION_RESULTS_REGEX =
            "outputs/unit_test_code_coverage/phoneDebugUnitTest/*.exec"
        const val FUNCTIONAL_TEST_EXECUTION_RESULTS_REGEX =
            "outputs/code_coverage/phoneDebugAndroidTest/connected/*coverage.ec"
        const val BUILD_CONFIG_CLASS = "**/BuildConfig.java"
        const val R_CLASS = "**/R.java"
        const val ADB_CLASS = "**/ADB*.class"
    }

    object BuildTypes {
        const val RELEASE = "release"
        const val DEBUG = "debug"
    }

    object SourceSets {
        const val MAIN = "main"
        const val PHONE = "phone"
    }

    object ProductFlavors {
        const val PHONE = "phone"
    }

    object BuildDimensions {
        const val TARGET = "target"
    }
}