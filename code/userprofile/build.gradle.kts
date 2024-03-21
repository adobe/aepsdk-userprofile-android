plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.userprofile"
    enableSpotless = true
    enableCheckStyle = true
    
    publishing {
        gitRepoName = "aepsdk-userprofile-android"
        addCoreDependency(mavenCoreVersion)
    }
}

dependencies {    
    // Revert this after the release of core
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
}
