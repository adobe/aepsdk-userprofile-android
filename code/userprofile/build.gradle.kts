plugins {
    id("com.android.library")
    id("kotlin-android")
    jacoco
    signing
    `maven-publish`
    id("com.diffplug.spotless")

}

val buildUtils = BuildUtils()

android {
    namespace = "com.adobe.marketing.mobile.userprofile"

    compileSdk = BuildConstants.ProjectConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildConstants.ProjectConfig.MIN_SDK_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions.add(BuildConstants.BuildDimensions.TARGET)
    productFlavors {
        create(BuildConstants.ProductFlavors.PHONE) {
            dimension = BuildConstants.BuildDimensions.TARGET
        }
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.DEBUG) {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }

        getByName(BuildConstants.BuildTypes.RELEASE) {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant(BuildConstants.BuildTypes.RELEASE) {
            withSourcesJar()
            withJavadocJar()
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = BuildConstants.ProjectConfig.JAVA_SOURCE_COMPATIBILITY
        targetCompatibility = BuildConstants.ProjectConfig.JAVA_TARGET_COMPATIBILITY
    }

    kotlinOptions {
        jvmTarget = BuildConstants.ProjectConfig.KOTLIN_JVM_TARGET
        languageVersion = BuildConstants.ProjectConfig.KOTLIN_LANGUAGE_VERSION
        apiVersion = BuildConstants.ProjectConfig.KOTLIN_API_VERSION
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

// ============= Gradle Tasks ============= //
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
                "includes" to listOf(BuildConstants.Reporting.UNIT_TEST_EXECUTION_RESULTS_REGEX)
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
                "includes" to listOf(BuildConstants.Reporting.FUNCTIONAL_TEST_EXECUTION_RESULTS_REGEX)
            )
        )
    )
}

android.libraryVariants.configureEach {
    tasks.withType(Javadoc::class.java).configureEach {
        val mainSourceSet = android.sourceSets.getByName(BuildConstants.SourceSets.MAIN)
        val phoneSourceSet = android.sourceSets.getByName(BuildConstants.SourceSets.PHONE)
        val mainSourceDirs = mainSourceSet.java.srcDirs.first()
        val phoneSourceDirs = phoneSourceSet.java.srcDirs.first()

        source = fileTree(mainSourceDirs) + fileTree(phoneSourceDirs)

        doFirst {
            classpath =
                files(javaCompileProvider.get().classpath.files) + files(buildUtils.androidJarPath)
        }

        exclude(BuildConstants.Reporting.BUILD_CONFIG_CLASS, BuildConstants.Reporting.R_CLASS)
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

// ============= Gradle Extensions ============= //

spotless {
    java {
        toggleOffOn("format:off", "format:on")
        target(BuildConstants.Formatting.JAVA_TARGETS)
        removeUnusedImports()
        googleJavaFormat(BuildConstants.Formatting.GOOGLE_JAVA_FORMAT_VERSION).aosp().reflowLongStrings()
        endWithNewline()
        formatAnnotations()
        licenseHeaderFile(BuildConstants.Formatting.LICENSE_HEADER_PATH)
    }
    kotlin {
        target(BuildConstants.Formatting.KOTLIN_TARGETS)
        ktlint(BuildConstants.Formatting.KTLINT_VERSION)
        endWithNewline()
        licenseHeaderFile(BuildConstants.Formatting.LICENSE_HEADER_PATH)
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>(BuildConstants.BuildTypes.RELEASE) {
            groupId = buildUtils.groupId
            artifactId = buildUtils.moduleName
            version = buildUtils.versionToUse

            artifact("$buildDir/outputs/aar/${buildUtils.moduleAARName}")
            artifact(tasks.getByName<Jar>("javadocPublish"))

            pom {
                name.set(buildUtils.mavenRepoName)
                description.set(buildUtils.mavenRepoDescription)
                url.set(BuildConstants.Publishing.DEVELOPER_DOC_URL)

                licenses {
                    license {
                        name.set(BuildConstants.Publishing.LICENSE_NAME)
                        url.set(BuildConstants.Publishing.LICENSE_URL)
                        distribution.set(BuildConstants.Publishing.LICENSE_DIST)
                    }
                }

                developers {
                    developer {
                        id.set(BuildConstants.Publishing.DEVELOPER_ID)
                        name.set(BuildConstants.Publishing.DEVELOPER_NAME)
                        email.set(BuildConstants.Publishing.DEVELOPER_EMAIL)
                    }
                }

                scm {
                    connection.set(BuildConstants.Publishing.SCM_CONNECTION_URL)
                    developerConnection.set(BuildConstants.Publishing.SCM_CONNECTION_URL)
                    url.set(BuildConstants.Publishing.SCM_REPO_URL)
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


// ============= Helpers ============= //
class BuildUtils {
    /**
     * The group id to use for the build
     */
    val groupId =
        if (isJitPackBuild()) "com.github.adobe.aepsdk-userprofile-android" else "com.adobe.marketing.mobile"

    // project properties via delegation
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
        "${android.sdkDirectory}/platforms/android-${BuildConstants.ProjectConfig.COMPILE_SDK_VERSION}/android.jar"

    /**
     * Verifies if the current build is a release build
     */
    private fun isReleaseBuild(): Boolean = project.hasProperty(BuildConstants.Publishing.RELEASE_PROPERTY)

    /**
     * Verifies if the current build is a snapshot build
     */
    private fun isSnapshotBuild(): Boolean = versionToUse.endsWith(BuildConstants.Publishing.SNAPSHOT_SUFFIX)

    /**
     * Verifies if the current build is a jitpack build
     */
    private fun isJitPackBuild(): Boolean = project.hasProperty(BuildConstants.Publishing.JITPACK_PROPERTY)

    /**
     * Returns the publish url based on the build type
     */
    fun getPublishUrl(): String = if (isSnapshotBuild()) {
        BuildConstants.Publishing.SNAPSHOTS_URL
    } else {
        BuildConstants.Publishing.RELEASES_URL
    }
}

/**
 * Extension to add the sources to the jacoco report
 */
fun JacocoReport.addSourceSets() {
    val mainSourceSet = android.sourceSets.getByName(BuildConstants.SourceSets.MAIN)
    val mainSourceDir = mainSourceSet.java.srcDirs
    sourceDirectories.setFrom(files(mainSourceDir))

    val debugTree = fileTree(
        mapOf(
            "dir" to "$buildDir/intermediates/javac/phoneDebug/classes/com/adobe/marketing/mobile",
            "excludes" to listOf(BuildConstants.Reporting.ADB_CLASS, BuildConstants.Reporting.BUILD_CONFIG_CLASS)
        )
    )

    additionalClassDirs.setFrom(files(debugTree))

    val phoneSourceSet = android.sourceSets.getByName(BuildConstants.SourceSets.PHONE)
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