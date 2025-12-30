#!/bin/bash

# =============================================================================
# Chat Server - Multi-Architecture Docker Image Build & Push Script
# Supports: linux/amd64, linux/arm64
# =============================================================================

set -e

# Configuration
IMAGE_NAME="ddingsh9/chatting-server"
VERSION="${1:-latest}"
PLATFORMS="linux/amd64,linux/arm64"
BUILDER_NAME="chatting-multiarch-builder"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check Docker login
check_docker_login() {
    log_info "Checking Docker Hub login status..."
    if ! docker info 2>/dev/null | grep -q "Username"; then
        log_warn "Not logged in to Docker Hub. Please login first."
        docker login
    fi
}

# Setup buildx builder
setup_buildx() {
    log_info "Setting up Docker Buildx..."

    # Check if builder exists
    if docker buildx inspect ${BUILDER_NAME} > /dev/null 2>&1; then
        log_info "Using existing builder: ${BUILDER_NAME}"
        docker buildx use ${BUILDER_NAME}
    else
        log_info "Creating new builder: ${BUILDER_NAME}"
        docker buildx create --name ${BUILDER_NAME} --use --platform ${PLATFORMS}
    fi

    # Bootstrap the builder
    docker buildx inspect --bootstrap
}

# Build application
build_application() {
    log_info "Building Spring Boot application..."

    cd "$(dirname "$0")/../ChatDDing-service"

    # Clean and build with Gradle
    ./gradlew clean bootJar -x test

    if [ $? -ne 0 ]; then
        log_error "Gradle build failed!"
        exit 1
    fi

    log_info "Application build completed successfully"
    cd ..
}

# Build and push multi-arch image
build_and_push_image() {
    log_info "Building multi-architecture Docker image..."
    log_info "Image: ${IMAGE_NAME}:${VERSION}"
    log_info "Platforms: ${PLATFORMS}"

    cd "$(dirname "$0")/.."

    # Build and push
    docker buildx build \
        --platform ${PLATFORMS} \
        --tag ${IMAGE_NAME}:${VERSION} \
        --tag ${IMAGE_NAME}:latest \
        --push \
        --file Dockerfile \
        .

    if [ $? -ne 0 ]; then
        log_error "Docker build failed!"
        exit 1
    fi

    log_info "Image pushed successfully!"
}

# Verify image
verify_image() {
    log_info "Verifying pushed image..."

    docker buildx imagetools inspect ${IMAGE_NAME}:${VERSION}

    log_info "Verification completed!"
}

# Main
main() {
    echo "=============================================="
    echo " Chat Server Multi-Arch Build & Push"
    echo "=============================================="
    echo ""

    log_info "Version: ${VERSION}"
    log_info "Target platforms: ${PLATFORMS}"
    echo ""

    check_docker_login
    setup_buildx
    build_application
    build_and_push_image
    verify_image

    echo ""
    echo "=============================================="
    log_info "Build and push completed successfully!"
    echo "=============================================="
    echo ""
    echo "Image: ${IMAGE_NAME}:${VERSION}"
    echo "Platforms: ${PLATFORMS}"
    echo ""
    echo "To pull the image:"
    echo "  docker pull ${IMAGE_NAME}:${VERSION}"
    echo ""
}

main "$@"
