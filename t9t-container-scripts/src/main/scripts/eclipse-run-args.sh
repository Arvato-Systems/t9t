#!/bin/bash
# Script: eclipse-run-args.sh
# Generates Eclipse Run/Debug parameters for all services from the Dockerfile
# Requires: yq (https://github.com/mikefarah/yq) and jq

eclipse-run-args() {
  # Default values
  DOCKERFILE="${DOCKERFILE:-Dockerfile}"

  # Parse named arguments
  for ARG in "$@"; do
    case $ARG in
      --dockerfile=*) DOCKERFILE="${ARG#*=}" ;;
      *) echo "Unknown argument: $ARG" >&2; return 1 ;;
    esac
  done

  if ! command -v yq >/dev/null 2>&1; then
    echo "yq is not installed! Please install yq (https://github.com/mikefarah/yq)"
    return 1
  fi
  if ! command -v jq >/dev/null 2>&1; then
    echo "jq is not installed! Please install jq (e.g., sudo apt install jq)"
    return 1
  fi

  # Extract all stages with ENV
  stages=$(grep -E '^FROM ' "$DOCKERFILE" | awk '{print $NF}')

  for stage in $stages; do
    # Extract ENV blocks for this stage
    # Find line range for the stage
    start=$(grep -n -E "^FROM .+AS $stage" "$DOCKERFILE" | head -n1 | cut -d: -f1)
    if [[ -z "$start" ]]; then continue; fi
    # Determine next FROM or end of file
    next=$(grep -n -E '^FROM ' "$DOCKERFILE" | awk -F: -v s="$start" '$1 > s {print $1; exit}')
    if [[ -z "$next" ]]; then
      next=$(wc -l < "$DOCKERFILE" | tr -d ' ')
      next=$((next+1))
    fi
    # Collect all ENV lines in stage range (including multiple ENV per stage)
    # Extract ENV blocks with backslash continuation
    envblock=$(sed -n "$((start+1)),$((next-1))p" "$DOCKERFILE" | awk '
      /^ENV / {inenv=1; line=substr($0,5); next}
      inenv && /^[ \t]+/ {line=line " " $0; next}
      inenv {print line; inenv=0; line=""}
      END {if (inenv) print line}
    ')
    # Remove backslashes, remove comments, normalize whitespace
    envblock=$(echo "$envblock" | sed 's/\\//g' | sed 's/#[^=]*$//' | tr -s ' ' '\n')
    # Extract all Key=Value pairs (remove leading whitespace)
    envs=$(echo "$envblock" | sed 's/^[ \t]*//' | grep '=' | grep -v '^[ ]*$')

    echo "=============================="
    echo "Service: $stage"
    echo "------------------------------"
    echo "Programm arguments:"
    echo "$envs" | grep -i 'metrics' | awk -F= '{print $2}' | sed 's/"//g'
    echo
    echo "VM arguments:"
    echo "$envs" | grep -vi 'metrics' | while read line; do
      [[ -z "$line" ]] && continue
      key=$(echo "$line" | cut -d= -f1)
      val=$(echo "$line" | cut -d= -f2-)
      dkey=$(echo "$key" | tr '[:upper:]' '[:lower:]' | tr '_' '.')
      echo "-D${dkey}=${val}"
    done
    echo
  done
}

if [[ "${BASH_SOURCE[0]}" != "${0}" ]]; then
  # Script is sourced, function is available
  :
else
  # Script will be executed directly
  eclipse-run-args "$@"
fi
