unit-test:
		(./code/gradlew -p code/android-userprofile-library testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/android-userprofile-library unitTestsCoverageReport)

functional-test:
		(./code/gradlew -p code/android-userprofile-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-userprofile-library connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/android-userprofile-library functionalTestsCoverageReport)

code-coverage:
		(./code/gradlew -p code/android-userprofile-library codeCoverageReport)