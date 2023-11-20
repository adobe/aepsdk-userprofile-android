plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
}

//TODO: This is used in the test app only. Move it to the test app build.gradle.kts
val compose_version by extra("1.2.0")