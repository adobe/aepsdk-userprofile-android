checkstyle:
		(./code/gradlew -p code/userprofile checkstyle)

checkformat:
		(./code/gradlew -p code/userprofile spotlessCheck)

# Used by build and test CI workflow
lint: checkstyle checkformat

format:
		(./code/gradlew -p code/userprofile spotlessApply)

javadoc:
		(./code/gradlew -p code/userprofile javadocJar)

unit-test:
		(./code/gradlew -p code/userprofile testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/userprofile createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/userprofile uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/userprofile connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/userprofile createPhoneDebugAndroidTestCoverageReport)

assemble-phone:
		(./code/gradlew -p code/userprofile  assemblePhone)

assemble-phone-release:
		(./code/gradlew -p code/userprofile assemblePhoneRelease)

assemble-app:
		(./code/gradlew -p code/testapp  assemble)

ci-publish-maven-local-jitpack: assemble-phone-release
		(./code/gradlew -p code/userprofile publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

ci-publish-staging: assemble-phone-release
		(./code/gradlew -p code/userprofile publishReleasePublicationToSonatypeRepository)

ci-publish: assemble-phone-release
		(./code/gradlew -p code/userprofile  publishReleasePublicationToSonatypeRepository -Prelease)
