release:
	./gradlew test jar publish closeAndReleaseRepository
	git push --tags
