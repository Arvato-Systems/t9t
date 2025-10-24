#!/bin/bash
containerize() {
  set -e

  ORIG_DIR="$(pwd)"
  cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

  # Ensure return to original directory on exit or error
  trap 'cd "$ORIG_DIR"' EXIT

  # Default values
  REGISTRY="localhost:6000"
  RS="/"
  PREFIX="default-"
  VERSION="latest"
  LEVEL1_BASE_IMAGE_VERSION="latest"
  LEVEL2_BASE_IMAGE_VERSION="latest"
  JAVA_IMAGE="amazoncorretto"
  JAVA_TAG="21-alpine-full"
  JPROFILER_VERSION="15_0_3"
  TARGETS=""
  PLATFORMS=""

  # Parse named arguments
  for ARG in "$@"; do
    case $ARG in
      --registry=*) REGISTRY="${ARG#*=}" ;;
      --rs=*) RS="${ARG#*=}" ;;
      --version=*) VERSION="${ARG#*=}" ;;
      --level1-base-image-version=*) LEVEL1_BASE_IMAGE_VERSION="${ARG#*=}" ;;
      --level2-base-image-version=*) LEVEL2_BASE_IMAGE_VERSION="${ARG#*=}" ;;
      --java-image=*) JAVA_IMAGE="${ARG#*=}" ;;
      --java-tag=*) JAVA_TAG="${ARG#*=}" ;;
      --jprofiler-version=*) JPROFILER_VERSION="${ARG#*=}" ;;
      --targets=*) TARGETS="${ARG#*=}" ;;
      --platforms=*) PLATFORMS="${ARG#*=}" ;;
      --prefix=*) PREFIX="${ARG#*=}" ;;
      *) echo "Unknown argument: $ARG" >&2; exit 1 ;;
    esac
  done

  PLATFORM_ARG=""
  if [[ -n "$PLATFORMS" ]]; then
    PLATFORM_ARG="--platform=$PLATFORMS"
  fi

  for TARGET in $TARGETS; do
    IMAGE="${REGISTRY}${RS}${PREFIX}${TARGET}:${VERSION}"
    echo "Building $IMAGE (target: $TARGET, platforms: $PLATFORMS)..."
    docker buildx build \
      --progress=plain \
      $PLATFORM_ARG \
      -t "$IMAGE" \
      --target "$TARGET" \
      --build-arg "REGISTRY=${REGISTRY}" \
      --build-arg "LEVEL1_BASE_IMAGE_VERSION=${LEVEL1_BASE_IMAGE_VERSION}" \
      --build-arg "LEVEL2_BASE_IMAGE_VERSION=${LEVEL2_BASE_IMAGE_VERSION}" \
      --build-arg "JAVA_IMAGE=${JAVA_IMAGE}" \
      --build-arg "JAVA_TAG=${JAVA_TAG}" \
      --build-arg "JPROFILER_VERSION=${JPROFILER_VERSION}" \
      --push \
      .
  done

  echo "All images built successfully!"
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  # Script is sourced, function is available
  :
else
  # Script will be executed directly
  containerize "$@"
fi