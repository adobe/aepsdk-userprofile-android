plugins {
    id("com.android.library")
    id("kotlin-android")
    jacoco
    signing
    `maven-publish`
}

val buildUtils = BuildUtils()

android {
    namespace = "com.adobe.marketing.mobile.userprofile"

    compileSdk = rootProject.extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = rootProject.extra["sourceCompatibility"] as JavaVersion
        targetCompatibility = rootProject.extra["targetCompatibility"] as JavaVersion
    }

    kotlinOptions {
        jvmTarget = rootProject.extra["kotlinJvmTarget"] as String
        languageVersion = rootProject.extra["kotlinLanguageVersion"] as String
        apiVersion = rootProject.extra["kotlinApiVersion"] as String
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

// Gradle Tasks
tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("unitTestCoverageReport") {
    dependsOn("testPhoneDebugUnitTest")
    enableReports(enableHtmlReport = true, enableXmlReport = true, enableCsvReport = false)
    addSourceSets()

    executionData.setFrom(
        fileTree(
            mapOf(
                "dir" to "$buildDir",
                "includes" to listOf(BuildUtils.UNIT_TEST_EXECUTION_RESULTS_REGEX)
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
                "includes" to listOf(BuildUtils.FUNCTIONAL_TEST_EXECUTION_RESULTS_REGEX)
            )
        )
    )
}

android.libraryVariants.configureEach {
    tasks.withType(Javadoc::class.java).configureEach {
        val mainSourceSet = android.sourceSets.getByName(BuildUtils.MAIN_SOURCE_SET)
        val phoneSourceSet = android.sourceSets.getByName(BuildUtils.PHONE_SOURCE_SET)
        val mainSourceDirs = mainSourceSet.java.srcDirs.first()
        val phoneSourceDirs = phoneSourceSet.java.srcDirs.first()

        source = fileTree(mainSourceDirs) + fileTree(phoneSourceDirs)

        doFirst {
            classpath =
                files(javaCompileProvider.get().classpath.files) + files(buildUtils.androidJarPath)
        }

        exclude(BuildUtils.BUILD_CONFIG_CLASS, BuildUtils.R_CLASS)
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

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("release") {
            groupId = buildUtils.groupId
            artifactId = buildUtils.moduleName
            version = buildUtils.versionToUse

            artifact("$buildDir/outputs/aar/${buildUtils.moduleAARName}")
            artifact(tasks.getByName<Jar>("javadocPublish"))

            pom {
                name.set(buildUtils.mavenRepoName)
                description.set(buildUtils.mavenRepoDescription)
                url.set(BuildUtils.DEVELOPER_DOC_URL)

                licenses {
                    license {
                        name.set(BuildUtils.LICENSE_NAME)
                        url.set(BuildUtils.LICENSE_URL)
                        distribution.set(BuildUtils.LICENSE_DIST)
                    }
                }

                developers {
                    developer {
                        id.set(BuildUtils.DEVELOPER_ID)
                        name.set(BuildUtils.DEVELOPER_NAME)
                        email.set(BuildUtils.DEVELOPER_EMAIL)
                    }
                }

                scm {
                    connection.set(BuildUtils.SCM_CONNECTION_URL)
                    developerConnection.set(BuildUtils.SCM_CONNECTION_URL)
                    url.set(BuildUtils.SCM_REPO_URL)
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    val coreDependencyNode = dependenciesNode.appendNode("dependency")
                    coreDependencyNode.appendNode("groupId", "com.adobe.marketing.mobile")
                    coreDependencyNode.appendNode("artifactId", "core")
                    coreDependencyNode.appendNode("version", buildUtils.mavenCoreVersion)
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri(buildUtils.getPublishUrl())
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()

    setRequired(
        tasks.withType<PublishToMavenRepository>().find {
            gradle.taskGraph.hasTask(it)
        }
    )

    sign(publishing.publications)
}

class BuildUtils {
    companion object {
        const val SNAPSHOTS_URL = "https://oss.sonatype.org/content/repositories/snapshots/"
        const val RELEASES_URL = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

        const val RELEASE_PROPERTY = "release"
        const val JITPACK_PROPERTY = "jitpack"
        const val SNAPSHOT_SUFFIX = "SNAPSHOT"
        const val MAIN_SOURCE_SET = "main"
        const val PHONE_SOURCE_SET = "phone"
        const val UNIT_TEST_EXECUTION_RESULTS_REGEX =
            "outputs/unit_test_code_coverage/phoneDebugUnitTest/*.exec"
        const val FUNCTIONAL_TEST_EXECUTION_RESULTS_REGEX =
            "outputs/code_coverage/phoneDebugAndroidTest/connected/*coverage.ec"

        const val LICENSE_NAME = "The Apache License, Version 2.0"
        const val LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        const val LICENSE_DIST = "repo"

        const val DEVELOPER_ID = "adobe"
        const val DEVELOPER_NAME = "adobe"
        const val DEVELOPER_EMAIL = "adobe-mobile-testing@adobe.com"
        const val DEVELOPER_DOC_URL = "https://developer.adobe.com/client-sdks"

        const val SCM_CONNECTION_URL = "scm:git:github.com//adobe/aepsdk-userprofile-android.git"
        const val SCM_REPO_URL = "https://github.com/adobe/aepsdk-userprofile-android"

        const val BUILD_CONFIG_CLASS = "**/BuildConfig.java"
        const val R_CLASS = "**/R.java"
        const val ADB_CLASS = "**/ADB*.class"
    }

    /**
     * The group id to use for the build
     */
    val groupId =
        if (isJitPackBuild()) "com.github.adobe.aepsdk-userprofile-android" else "com.adobe.marketing.mobile"

    // project properties via delegation
    val compileSdkVersion: Int by project
    val moduleVersion: String by project
    val moduleName: String by project
    val mavenRepoName: String by project
    val moduleAARName: String by project
    val mavenRepoDescription: String by project
    val mavenCoreVersion: String by project

    /**
     * The version name to use for the build
     */
    val versionToUse: String = if (isReleaseBuild()) moduleVersion else "${moduleVersion}-SNAPSHOT"

    /**
     * The android jar path based on the compile sdk version
     */
    val androidJarPath: String =
        "${android.sdkDirectory}/platforms/android-$compileSdkVersion/android.jar"

    /**
     * Verifies if the current build is a release build
     */
    private fun isReleaseBuild(): Boolean = project.hasProperty(RELEASE_PROPERTY)

    /**
     * Verifies if the current build is a snapshot build
     */
    private fun isSnapshotBuild(): Boolean = versionToUse.endsWith(SNAPSHOT_SUFFIX)

    /**
     * Verifies if the current build is a jitpack build
     */
    private fun isJitPackBuild(): Boolean = project.hasProperty(JITPACK_PROPERTY)

    /**
     * Returns the publish url based on the build type
     */
    fun getPublishUrl(): String = if (isSnapshotBuild()) {
        SNAPSHOTS_URL
    } else {
        RELEASES_URL
    }
}

/**
 * Extension to add the sources to the jacoco report
 */
fun JacocoReport.addSourceSets() {
    val mainSourceSet = android.sourceSets.getByName(BuildUtils.MAIN_SOURCE_SET)
    val mainSourceDir = mainSourceSet.java.srcDirs
    sourceDirectories.setFrom(files(mainSourceDir))

    val debugTree = fileTree(
        mapOf(
            "dir" to "$buildDir/intermediates/javac/phoneDebug/classes/com/adobe/marketing/mobile",
            "excludes" to listOf(BuildUtils.ADB_CLASS, BuildUtils.BUILD_CONFIG_CLASS)
        )
    )

    additionalClassDirs.setFrom(files(debugTree))

    val phoneSourceSet = android.sourceSets.getByName(BuildUtils.PHONE_SOURCE_SET)
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