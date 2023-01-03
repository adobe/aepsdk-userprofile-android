unit-test:
		(./code/gradlew -p code/android-userprofile-library testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/android-userprofile-library createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/android-userprofile-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-userprofile-library connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/android-userprofile-library createPhoneDebugAndroidTestCoverageReport)

javadoc:
		(./code/gradlew -p code/android-userprofile-library javadocPublish)

publish:
		(./code/gradlew -p code/android-userprofile-library  publishReleasePublicationToSonatypeRepository)

assemble-phone:
		(./code/gradlew -p code/android-userprofile-library  assemblePhone)

assemble-app:
		(./code/gradlew -p code/testapp  assemble)

identity-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-userprofile-library assemblePhone)
		(./code/gradlew -p code/android-userprofile-library publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

build-release:
		(./code/gradlew -p code/android-userprofile-library assemblePhoneRelease)

ci-publish-staging: build-release
	(./code/gradlew -p code/android-userprofile-library publishReleasePublicationToSonatypeRepository --stacktrace)

