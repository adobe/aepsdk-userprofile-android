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
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion")
}
