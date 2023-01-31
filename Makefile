unit-test:
		(./code/gradlew -p code/userprofile testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/userprofile createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/userprofile uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/userprofile connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/userprofile createPhoneDebugAndroidTestCoverageReport)

javadoc:
		(./code/gradlew -p code/userprofile javadocPublish)

publish:
		(./code/gradlew -p code/userprofile  publishReleasePublicationToSonatypeRepository)

assemble-phone:
		(./code/gradlew -p code/userprofile  assemblePhone)

assemble-app:
		(./code/gradlew -p code/testapp  assemble)

userprofile-publish-maven-local-jitpack:
		(./code/gradlew -p code/userprofile assemblePhone)
		(./code/gradlew -p code/userprofile publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

build-release:
		(./code/gradlew -p code/userprofile assemblePhoneRelease)

ci-publish-staging: build-release
	(./code/gradlew -p code/userprofile publishReleasePublicationToSonatypeRepository --stacktrace)

