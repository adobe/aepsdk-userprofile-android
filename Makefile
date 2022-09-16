unit-test:
		(./code/gradlew -p code/android-userprofile-library testPhoneDebugUnitTest)

functional-test:
		(./code/gradlew -p code/android-userprofile-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-userprofile-library connectedPhoneDebugAndroidTest)