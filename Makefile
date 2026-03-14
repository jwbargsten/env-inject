dist:
	@script/env-inject exec ./gradlew clean patchChangelog buildPlugin signPlugin

publish:
	@script/env-inject exec ./gradlew clean publishPlugin

