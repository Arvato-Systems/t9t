#!/usr/bin/env bash
#
# Copyright (c) 2012 - 2026 Arvato Systems GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
module_dir="$(cd "$script_dir/.." && pwd)"
reactor_root="$(cd "$module_dir/.." && pwd)"
maven_repository="$module_dir/target/local-maven-repository"
npm_registry="http://127.0.0.1:4873"
plugin_version="${BONAPARTE_PLUGIN_VERSION:-6.6.3-SNAPSHOT}"

mkdir -p "$maven_repository"
docker compose -f "$script_dir/compose.yaml" up -d --wait

api_modules="$(awk '
  /<id>build<\/id>/ { in_build_profile = 1; next }
  in_build_profile && /<\/profile>/ { exit }
  in_build_profile && /<module>t9t-.*-(api|apiext)<\/module>/ {
    module = $0
    sub(/.*<module>/, "", module)
    sub(/<\/module>.*/, "", module)
    print module
  }
' "$reactor_root/pom.xml" | paste -sd, -)"

if [[ -z "$api_modules" ]]; then
  echo "Could not find API modules in the active build profile" >&2
  exit 1
fi

mvn -f "$reactor_root/pom.xml" --batch-mode --no-transfer-progress \
  -pl "$api_modules" -am process-sources \
  "-Dbonaparte-plugin.version=$plugin_version"

export NPM_REGISTRY_URL="$npm_registry"
export NODE_AUTH_TOKEN=""
mvn -f "$module_dir/pom.xml" --batch-mode --no-transfer-progress deploy \
  "-DaltDeploymentRepository=local-registry::default::file://$maven_repository"

echo "Published Maven artifacts to http://127.0.0.1:8082/repository/"
echo "Published the snapshot npm tag to $npm_registry"