buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
    }
    dependencies {
        classpath("com.github.adobe:aepsdk-commons:gp-3.0.0")
    }
}
