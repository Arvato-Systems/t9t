#!/bin/bash
run-rt() {
	set -e
	ENV=${1:-.env}
	export $(grep -v '^#' ${ENV} | xargs)

	echo ${registry}/${rt}:${projectVersion}
	docker run -it --pull always \
	    --network t9t-net \
		--rm \
		--env-file "${ENV}" \
		${registry}/${rt}:${projectVersion}
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
	# Script is sourced, function is available
	:
else
	# Script will be executed directly
	run-rt "$@"
fi
