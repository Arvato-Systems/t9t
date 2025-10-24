#!/bin/bash
run-local-registry() {
	set -e
	echo Start or create registry
	docker start registry || docker run -d -p 6000:5000 -e REGISTRY_STORAGE_DELETE_ENABLED=true --name registry registry:2
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
	# Script is sourced, function is available
	:
else
	# Script will be executed directly
	run-local-registry "$@"
fi
