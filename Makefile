unit-test:
		(./code/gradlew -p code/android-userprofile-library testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/android-userprofile-library createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/android-userprofile-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-userprofile-library connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/android-userprofile-library createPhoneDebugAndroidTestCoverageReport)

code-coverage:
		(./code/gradlew -p code/android-userprofile-library codeCoverageReport)

ci-publish:
	(./code/gradlew -p code/android-userprofile-library  publishReleasePublicationToSonatypeRepository)