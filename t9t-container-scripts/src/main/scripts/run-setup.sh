#!/bin/bash

# Usage example:
# run-setup --container=t9t-setup-main --project-version=1.2.3 --db-url=jdbc:postgresql://host.docker.internal:5432/main --db-user=fortytwo --db-password=changeMe --action=install --registry=localhost:6000
# All parameters are optional and have defaults.

run-setup() {
  set -e
  # Default values
  local ENV=.env
  local CONTAINER="t9t-setup-main"
  local PROJECT_VERSION="latest"
  local DB_URL="jdbc:postgresql://host.docker.internal:5432/main"
  local DB_USER="fortytwo"
  local DB_PASSWORD="changeMe"
  local ACTION="install"
  local REGISTRY="localhost:6000"

  # Parse named arguments
  for ARG in "$@"; do
    case $ARG in
      --env=*) ENV="${ARG#*=}" ;;
      --container=*) CONTAINER="${ARG#*=}" ;;
      --project-version=*) PROJECT_VERSION="${ARG#*=}" ;;
      --db-url=*) DB_URL="${ARG#*=}" ;;
      --db-user=*) DB_USER="${ARG#*=}" ;;
      --db-password=*) DB_PASSWORD="${ARG#*=}" ;;
      --action=*) ACTION="${ARG#*=}" ;;
      --registry=*) REGISTRY="${ARG#*=}" ;;
      *) echo "Unknown argument: $ARG" >&2; exit 1 ;;
    esac
  done

  export $(grep -v '^#' "$ENV" | xargs)

  echo "${REGISTRY}/${CONTAINER}:${PROJECT_VERSION}"
  docker run --pull always --rm \
    -e DB_URL="$DB_URL" \
    -e DB_USER="$DB_USER" \
    -e DB_PASSWORD="$DB_PASSWORD" \
    -e ACTION="$ACTION" \
    ${REGISTRY}/${CONTAINER}:${PROJECT_VERSION}
}

run-setup-migrate() {
  # Wrapper für Migration, setzt --action=migrate
  run-setup "$@" --action=migrate
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  # Script is sourced, functions are available
  :
else
  # Script will be executed directly
  run-setup "$@"
fi
