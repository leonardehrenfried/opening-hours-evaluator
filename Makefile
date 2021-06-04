release:
	./gradlew clean test jar publish closeAndReleaseRepository
	git push --tags
