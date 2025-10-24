#!/bin/bash
push-images() {
  set -e

  if [ "$#" -lt 3 ]; then
    echo "Usage: push-images <defaultPlatforms> <srcImage1> <targetImage1> [<srcImage2> <targetImage2> ...]"
    return 1
  fi

  # After removing the first argument (platform), the rest must be even
  if [ $(( ($# - 1) % 2 )) -ne 0 ]; then
    echo "Error: The number of image arguments must be even (source and target image pairs)."
    return 1
  fi

  local DEFAULT_PLATFORM="$1"
  shift
  local args=("$@")
  local IDX=0
  while [ $IDX -lt ${#args[@]} ]; do
    local SRC_IMAGE="${args[$IDX]}"
    IDX=$((IDX+1))
    local TARGET_IMAGE="${args[$IDX]}"
    IDX=$((IDX+1))

    # Check if DEFAULT_PLATFORM contains comma (multi-arch)
    if [[ "$DEFAULT_PLATFORM" == *","* ]]; then
      echo "$SRC_IMAGE is treated as multi-arch based on DEFAULT_PLATFORM."
      # Split the platforms by comma
      IFS=',' read -ra PLATFORMS <<< "$DEFAULT_PLATFORM"
      # For each platform: pull and push the image to the target with platform-specific tags
      local MANIFEST_ARGS=""
      for platform in "${PLATFORMS[@]}"; do
        platform=$(echo "$platform" | xargs)  # Trim whitespace
        # Create platform-specific tag by replacing / and : with -
        local platform_suffix=$(echo "$platform" | sed 's/[\/:]/-/g')
        local platform_tag="${TARGET_IMAGE}-${platform_suffix}"
        
        echo "Processing platform $platform -> $platform_tag ..."
        docker pull --platform="$platform" "$SRC_IMAGE"
        # Tag the pulled image with platform-specific target name
        docker tag "$SRC_IMAGE" "$platform_tag"
        docker push "$platform_tag"
        echo "Pushed $platform_tag"

        # Add to manifest args
        MANIFEST_ARGS="$MANIFEST_ARGS $platform_tag"
      done
      
      # For local registries, try to create manifest list but handle errors gracefully
      echo "Creating and pushing manifest list for $TARGET_IMAGE ..."
      # Delete manifest if it exists to avoid conflicts
      if docker manifest inspect "$TARGET_IMAGE" >/dev/null 2>&1; then
        echo "Deleting existing manifest for $TARGET_IMAGE ..."
        docker manifest rm "$TARGET_IMAGE" || true
      fi
      
      # Try to create and push manifest - if it fails, just use the last platform image as fallback
      echo "Creating manifest with platform images: $MANIFEST_ARGS"
      if docker manifest create "$TARGET_IMAGE" $MANIFEST_ARGS 2>/dev/null; then
        echo "Manifest created successfully"
        if docker manifest push "$TARGET_IMAGE" 2>/dev/null; then
          echo "Manifest pushed successfully"
        else
          echo "Warning: Failed to push manifest to local registry. Using platform-specific tags only."
        fi
      else
        echo "Warning: Local registry doesn't support manifest lists. Platform-specific images are available:"
        for platform_tag in $MANIFEST_ARGS; do
          echo "  - $platform_tag"
        done
        # As fallback, tag the last platform image with the main target name
        last_platform_tag=$(echo $MANIFEST_ARGS | awk '{print $NF}')
        echo "Using $last_platform_tag as fallback for $TARGET_IMAGE"
        docker tag "$last_platform_tag" "$TARGET_IMAGE"
        docker push "$TARGET_IMAGE"
      fi
    else
      echo "$SRC_IMAGE is treated as single-arch."
      # Use default platform from argument or fallback to linux/arm64
      local PLATFORM_TO_USE="${DEFAULT_PLATFORM:-linux/arm64}"
      echo "Using platform: $PLATFORM_TO_USE"
      docker pull --platform="$PLATFORM_TO_USE" "$SRC_IMAGE"
      # Tag the pulled image with the target registry/name
      docker tag "$SRC_IMAGE" "$TARGET_IMAGE"
      docker push "$TARGET_IMAGE"
    fi
  done

  echo "All images pushed successfully!"
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  # Script will be sourced, function is available
  :
else
  # Script will be executed directly
  push-images "$@"
fi