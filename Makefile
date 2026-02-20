dist:
	@export $$(script/env-inject | xargs) && ./gradlew clean patchChangelog buildPlugin signPlugin

publish:
	@export $$(script/env-inject | xargs) && ./gradlew clean publishPlugin

