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
