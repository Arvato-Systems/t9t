#!/bin/bash
for f in "$(dirname "${BASH_SOURCE[0]}")"/*.sh; do
  [[ "$f" == "${BASH_SOURCE[0]}" ]] && continue
  source "$f"
done
