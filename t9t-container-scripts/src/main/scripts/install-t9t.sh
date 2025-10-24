#!/bin/bash
set -e

# This script contains the reusable install logic for t9t-based projects.
# It expects the following variables to be set before sourcing/using:
#   ENV, PROJECT_VERSION, DOCKER_FILE
#
# Usage example (in your project-specific install.sh):
#   source ./install-t9t.sh
#   install-t9t

install-t9t() {
    ENV=${ENV:-.env}
    DOCKER_FILE=${DOCKER_FILE:-"docker-compose.yml"}
    PROJECT_VERSION=${PROJECT_VERSION:-latest}

    export $(grep -v '^#' "${ENV}" | xargs)

    echo "Create Docker network t9t-net if not exists"
    docker network inspect t9t-net >/dev/null 2>&1 || docker network create t9t-net

    echo "Run local registry"
    run-local-registry

    echo "Build images and push to registry"
    mvn -T 1C -Pcontainerize install

    echo "Update local container if using local registry"
    docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" pull

    echo "Reset all"
    docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" down -v
    docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" up -d db

    echo "Waiting for database to be ready..."
    for i in {1..30}; do
        if docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" exec -T db pg_isready -U "${POSTGRES_USER:-postgres}" > /dev/null 2>&1; then
            echo "Database is up!"
            break
        fi
        echo "Database not ready yet, retrying ($i/30)..."
        sleep 2
    done

    echo "Run all DB setups from .env..."
    while IFS='=' read -r VAR VALUE; do
        if [[ $VAR =~ ^setup([a-zA-Z0-9_]+)$ ]]; then
            NAME="${BASH_REMATCH[1]}"
            CONTAINER="$VALUE"
            DBNAME="${NAME,,}"
            echo "Run db setup for $CONTAINER (DB: $DBNAME)"
            run-setup --container="$CONTAINER" --db-url="jdbc:postgresql://host.docker.internal:5432/$DBNAME" --env="${ENV}" --project-version="${PROJECT_VERSION}"
        fi
    done < <(grep '^setup' "${ENV}")

    echo "Run app"
    docker compose -f "${DOCKER_FILE}" up -d

    echo "Waiting for health checks of all containers with health check definition..."
    # Get all services with healthcheck from docker compose
    ALL_SERVICES=$(docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" ps --services)
    
    for SERVICE in $ALL_SERVICES; do
        CONTAINER=$(docker compose --env-file "${ENV}" -f "${DOCKER_FILE}" ps -q "$SERVICE" 2>/dev/null)
        if [ -z "$CONTAINER" ]; then
            continue
        fi
        
        # Check if container has healthcheck configured
        HAS_HEALTHCHECK=$(docker inspect --format='{{if .State.Health}}true{{else}}false{{end}}' "$CONTAINER" 2>/dev/null || echo "false")
        
        if [ "$HAS_HEALTHCHECK" = "true" ]; then
            echo "Wait for healthcheck of $SERVICE ($CONTAINER) ..."
            for i in {1..30}; do
                STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null || echo "notfound")
                if [ "$STATUS" = "healthy" ]; then
                    echo "$SERVICE is ready (healthy)."
                    break
                elif [ "$STATUS" = "notfound" ]; then
                    echo "$SERVICE not found, retrying ($i/30)..."
                else
                    echo "$SERVICE Status: $STATUS, retrying ($i/30)..."
                fi
                sleep 2
            done
        fi
    done

    echo "Run remote tests"
    echo "If failed because of no connection yet, run 'run-rt' again"
    run-rt "${ENV}"
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
    # Script is sourced, function is available
    :
else
    install-t9t "$@"
fi
