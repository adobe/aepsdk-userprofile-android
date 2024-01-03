plugins {
    id("com.android.application") version BuildConstants.Plugins.ANDROID_GRADLE_PLUGIN_VERSION apply false
    id("org.jetbrains.kotlin.android") version BuildConstants.Plugins.KOTLIN_GRADLE_PLUGIN_VERSION apply false
    id("com.diffplug.gradle.spotless") version BuildConstants.Plugins.SPOTLESS_GRADLE_PLUGIN_VERSION apply false
}

extra.apply {
    set("signing.gnupg.executable", BuildConstants.Publishing.SIGNING_GNUPG_EXECUTABLE)
    set("signing.gnupg.keyName", BuildConstants.Publishing.SIGNING_GNUPG_KEY_NAME)
    set("signing.gnupg.passphrase", BuildConstants.Publishing.SIGNING_GNUPG_PASSPHRASE)
}
