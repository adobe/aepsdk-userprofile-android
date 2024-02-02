plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.userprofile"
    
    publishing {
        gitRepoName = "aepsdk-userprofile-android"
        addCoreDependency(mavenCoreVersion)
    }
}

dependencies {
    // Stop using SNAPSHOT after Core release.
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
}
