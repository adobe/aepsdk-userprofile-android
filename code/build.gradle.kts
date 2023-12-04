plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
}

extra.apply {
    set("minSdkVersion", 21)
    set("compileSdkVersion", 34)
    set("targetSdkVersion", 34)
    set("versionCode", 1)
    set("versionName", "1.0")

    set("sourceCompatibility", JavaVersion.VERSION_1_8)
    set("targetCompatibility", JavaVersion.VERSION_1_8)

    set("kotlinVersion", "1.7.0")
    set("composeVersion", "1.2.0")
    set("kotlinLanguageVersion", "1.5")
    set("kotlinApiVersion", "1.5")
    set("kotlinJvmTarget", JavaVersion.VERSION_1_8.toString())

    set("signing.gnupg.executable", "gpg")
    set("signing.gnupg.keyName", System.getenv("GPG_KEY_ID"))
    set("signing.gnupg.passphrase", System.getenv("GPG_PASSPHRASE"))
}
