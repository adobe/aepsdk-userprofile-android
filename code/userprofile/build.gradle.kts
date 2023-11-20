plugins {
    id("com.android.library")
    jacoco
}

apply {
    from("release.gradle")
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