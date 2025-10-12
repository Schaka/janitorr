# Makefile for Janitorr - Docker-based development
# All Gradle commands MUST run through Docker as per project requirements

# Docker image for Java/Gradle operations
GRADLE_IMAGE = openjdk:25-jdk-slim
WORKSPACE_MOUNT = -v $(PWD):/workspace -w /workspace

# Default target
.PHONY: help
help:
	@echo "Janitorr Development Commands (Docker-based)"
	@echo "============================================="
	@echo ""
	@echo "Build Commands:"
	@echo "  make build          - Full build (excludes Docker image)"
	@echo "  make compile        - Compile Kotlin sources only"
	@echo "  make test           - Run all tests"
	@echo "  make clean          - Clean build artifacts"
	@echo ""
	@echo "Docker Commands:"
	@echo "  make docker-image   - Build Docker image"
	@echo "  make docker-run     - Run application in Docker"
	@echo ""
	@echo "Development:"
	@echo "  make check          - Run compile + test"
	@echo "  make lint           - Run code linting"
	@echo ""
	@echo "Note: All commands use Docker to ensure consistent environment"

# Build commands
.PHONY: build
build:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew build -x bootBuildImage

.PHONY: compile
compile:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew compileKotlin

.PHONY: test
test:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew test

.PHONY: clean
clean:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew clean

# Docker image commands
.PHONY: docker-image
docker-image:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew bootBuildImage

.PHONY: docker-run
docker-run:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew bootRun

# Development shortcuts
.PHONY: check
check: compile test

.PHONY: lint
lint:
	docker run --rm $(WORKSPACE_MOUNT) $(GRADLE_IMAGE) ./gradlew ktlintCheck

# Quick compilation check (fastest option)
.PHONY: quick
quick: compile

# Full verification before commit
.PHONY: verify
verify: clean build test
	@echo "✅ All checks passed - ready to commit!"

# Show Docker image info
.PHONY: docker-info
docker-info:
	@echo "Using Docker image: $(GRADLE_IMAGE)"
	@echo "Mount: $(WORKSPACE_MOUNT)"
	docker run --rm $(GRADLE_IMAGE) java -version