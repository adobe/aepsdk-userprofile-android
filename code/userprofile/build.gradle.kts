plugins {
    id("com.android.library")
    jacoco
    signing
    `maven-publish`
}

android {
    namespace = "com.adobe.marketing.mobile.userprofile"

    compileSdk = 33

    defaultConfig {
        minSdk = 19

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    flavorDimensions.add("target")
    productFlavors {
        create("phone") {
            dimension = "target"
        }
    }

    buildTypes {
        getByName("debug") {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.adobe.marketing.mobile:core:2.0.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.mockito:mockito-inline:4.5.1")
    testImplementation("org.json:json:20180813")
    testImplementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.aar")
            )
        )
    )
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("codeCoverageReport") {
    dependsOn("testPhoneDebugUnitTest", "createPhoneDebugCoverageReport")
    enableReports(enableHtmlReport = true, enableXmlReport = true, enableCsvReport = false)
    addSourceSets()

    executionData.setFrom(
        fileTree(
            mapOf(
                "dir" to "$buildDir",
                "includes" to listOf(
                    "outputs/unit_test_code_coverage/phoneDebugUnitTest/*.exec",
                    "outputs/code_coverage/phoneDebugAndroidTest/connected/*coverage.ec"
                )
            )
        )
    )
}

tasks.register<JacocoReport>("unitTestsCoverageReport") {
    dependsOn("testPhoneDebugUnitTest")
    enableReports(enableHtmlReport = true, enableXmlReport = true, enableCsvReport = false)
    addSourceSets()

    executionData.setFrom(
        fileTree(
            mapOf(
                "dir" to "$buildDir",
                "includes" to listOf(
                    "outputs/unit_test_code_coverage/phoneDebugUnitTest/*.exec"
                )
            )
        )
    )
}

tasks.register<JacocoReport>("functionalTestsCoverageReport") {
    dependsOn("createPhoneDebugCoverageReport")
    enableReports(enableHtmlReport = true, enableXmlReport = true, enableCsvReport = false)
    addSourceSets()

    executionData.setFrom(
        fileTree(
            mapOf(
                "dir" to "$buildDir",
                "includes" to listOf(
                    "outputs/code_coverage/phoneDebugAndroidTest/connected/*coverage.ec"
                )
            )
        )
    )
}

android.libraryVariants.configureEach {
    tasks.withType(Javadoc::class.java).configureEach {
        val mainSourceSet = android.sourceSets.getByName("main")
        val phoneSourceSet = android.sourceSets.getByName("phone")
        val mainSourceDirs = mainSourceSet.java.srcDirs
        val phoneSourceDirs = phoneSourceSet.java.srcDirs

        source = fileTree(mainSourceDirs).plus(fileTree(phoneSourceDirs))
        ext.set(
            "androidJar",
            "${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar"
        )

        doFirst {
            classpath =
                files(javaCompileProvider.get().classpath.files) + files(ext.get("androidJar"))
        }

        exclude("**/BuildConfig.java", "**/R.java")
        options {
            memberLevel = JavadocMemberLevel.PUBLIC
        }
    }
}

tasks.register<Javadoc>("javadoc") {
    options.memberLevel = JavadocMemberLevel.PUBLIC
}

tasks.register<Jar>("javadocPublish") {
    from(tasks.getByName<Javadoc>("javadoc"))
    archiveClassifier.set("javadoc")
}

tasks.named("publish").configure {
    dependsOn(tasks.named("assemblePhone"))
}

// TODO: Cleanup with objects to hold constants and utils
val isJitPackBuild: Boolean = project.hasProperty("jitpack")
val groupIdForPublish =
    if (isJitPackBuild) "com.github.adobe.aepsdk-userprofile-android" else "com.adobe.marketing.mobile"
val isReleaseBuild: Boolean = project.hasProperty("release")
val moduleVersion: String by project
val moduleName: String by project
val mavenRepoName: String by project
val moduleAARName: String by project
val mavenRepoDescription: String by project
val mavenCoreVersion: String by project
val versionToUse: String = if (isReleaseBuild) moduleVersion else "${moduleVersion}-SNAPSHOT"
val isSnapshot: Boolean = versionToUse.endsWith("SNAPSHOT")

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("release") {
            groupId = groupIdForPublish
            artifactId = moduleName
            version = versionToUse

            artifact("$buildDir/outputs/aar/$moduleAARName}")
            artifact(tasks.getByName<Jar>("javadocPublish"))

            pom {
                name.set(mavenRepoName)
                description.set(mavenRepoDescription)
                url.set("https://developer.adobe.com/client-sdks")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("adobe")
                        name.set("adobe")
                        email.set("adobe-mobile-testing@adobe.com")
                    }
                }

                scm {
                    connection.set("scm:git:github.com//adobe/aepsdk-userprofile-android.git")
                    developerConnection.set("scm:git:ssh://github.com//adobe/aepsdk-userprofile-android.git")
                    url.set("https://github.com/adobe/aepsdk-userprofile-android")
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    val coreDependencyNode = dependenciesNode.appendNode("dependency")
                    coreDependencyNode.appendNode("groupId", "com.adobe.marketing.mobile")
                    coreDependencyNode.appendNode("artifactId", "core")
                    coreDependencyNode.appendNode("version", mavenCoreVersion)
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url =
                uri(if (isSnapshot) "https://oss.sonatype.org/content/repositories/snapshots/" else "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

extra["signing.gnupg.executable"] = "gpg"
extra["signing.gnupg.keyName"] = System.getenv("GPG_KEY_ID")
extra["signing.gnupg.passphrase"] = System.getenv("GPG_PASSPHRASE")

signing {
    useGpgCmd()

    setRequired(
        tasks.withType<PublishToMavenRepository>().find {
            gradle.taskGraph.hasTask(it)
        }
    )

    sign(publishing.publications)
}

/**
 * Extension to add the sources to the jacoco report
 */
fun JacocoReport.addSourceSets() {
    val mainSourceSet = android.sourceSets.getByName("main")
    val mainSourceDir = mainSourceSet.java.srcDirs
    sourceDirectories.setFrom(files(mainSourceDir))

    val debugTree = fileTree(
        mapOf(
            "dir" to "$buildDir/intermediates/javac/phoneDebug/classes/com/adobe/marketing/mobile",
            "excludes" to listOf("**/ADB*.class", "**/BuildConfig.class")
        )
    )
    additionalClassDirs.setFrom(files(debugTree))

    val phoneSourceSet = android.sourceSets.getByName("phone")
    val phoneSourceDir = phoneSourceSet.java.srcDirs
    additionalSourceDirs.setFrom(files(phoneSourceDir))
}

/**
 * Extension to enable the reports for the jacoco task
 * @param enableXmlReport enable the xml report
 * @param enableHtmlReport enable the html report
 * @param enableCsvReport enable the csv report
 */
fun JacocoReport.enableReports(
    enableXmlReport: Boolean = false,
    enableHtmlReport: Boolean = false,
    enableCsvReport: Boolean = false
) {
    reports {
        xml.required.set(enableXmlReport)
        html.required.set(enableHtmlReport)
        csv.required.set(enableCsvReport)
    }
}
