# @arvato-systems-jacs/t9t-api

The t9t TypeScript package provides the shared Angular RPC client, response
unwrappers, error handling, and generated API entry points.

Internal npm dependencies use release-bounded snapshot ranges. No package lock
is committed; workspace preparation resolves each internal package's current
`snapshot` dist-tag from the configured scope registry on every build. The Maven
property `npm.registry` defaults to GitHub Packages and can be set to
`http://localhost:4873` for local Verdaccio builds.

Configure the package registry using `.npmrc.template` and a token with
`read:packages` permission. For example, `NODE_AUTH_TOKEN=$(gh auth token)` can
be used for package reads when the GitHub CLI token has that scope. Do not
commit a populated `.npmrc` or store credentials in the package. The npm package
is built by Maven from the hand-written `rpc-core` sources and generated BON
module entry points.

`BonaPortable` and metadata support are owned by the foundational `t9t-api`
entry point. Referenced Bonaparte metadata schemas are generated into this
package; the package also declares a peer dependency on the shared
`@arvato-systems-jacs/bonaparte-typescript` runtime. Assembly includes the moved
`t9t-api-auth` and `t9t-api-media` modules as well as the other generated modules.

Maven stages the hand-written npm inputs and generated entry points under
`t9t-api-ts/target/npm-src`; `mvn clean` removes this workspace. The Angular
package and packed artifact are written under `t9t-api-ts/target/npm-dist` and
`t9t-api-ts/target`, respectively. Build and validate the package with:

```sh
mvn --batch-mode --no-transfer-progress -Pbuild -Dts-api=true \
  -pl t9t-api-ts -am clean verify
```

## Cross-repository TypeScript API build

The generation chain is `bonaparte-dsl` TypeScript generator -> t9t BON API
modules -> `t9t-api-ts` npm package. When changing the generator, install the
matching local BON DSL artifacts before building t9t. In `bonaparte-dsl`, run
the target-platform setup, focused generator tests, and local installs described
in that repository's README section "Developing the TypeScript API generator."
Then use the root Maven version as the plugin override in the t9t reactor:

```sh
cd /path/to/bonaparte-dsl
export BONAPARTE_PLUGIN_VERSION="$(mvn -q -N help:evaluate \
  -Dexpression=project.version -DforceStdout)"

cd /path/to/t9t
mvn --batch-mode --no-transfer-progress -Pbuild -Dts-api=true \
  "-Dbonaparte-plugin.version=$BONAPARTE_PLUGIN_VERSION" \
  -pl t9t-api-ts -am clean verify
```

The override matters because the t9t parent may default to an older published
plugin. The build regenerates the API modules and verifies/packages the SDK;
the npm distribution is staged at `t9t-api-ts/target/npm-dist`, with its
versioned tarball under `t9t-api-ts/target`. To install a published private
snapshot into a consumer, use an npm config based on `.npmrc.template` and a
token with `read:packages`, then run that consumer's normal `npm ci`.

### Publish the SDK snapshot to GitHub Packages

Publishing requires a GitHub token with `write:packages` permission (in addition
to any read permission needed by the build). `gh auth token` is convenient only
when the authenticated CLI token includes the write scope; otherwise supply an
approved write-capable token through `NODE_AUTH_TOKEN`. Use a temporary npm
config so credentials are not written into the repository:

```sh
export NODE_AUTH_TOKEN="$(gh auth token)"
npm_auth_config="$(mktemp)"
trap 'rm -f "$npm_auth_config"' EXIT
printf '%s\n' \
  '@arvato-systems-jacs:registry=https://npm.pkg.github.com' \
  '//npm.pkg.github.com/:_authToken=${NODE_AUTH_TOKEN}' > "$npm_auth_config"

cd /path/to/t9t/t9t-api-ts/target/npm-dist
npm --userconfig "$npm_auth_config" publish \
  --tag snapshot --registry=https://npm.pkg.github.com
```

The temporary config contains only the environment-variable reference; keep
the token in the environment and do not print it, commit it, or add it to an
image. After publication, consumers can install the exact published SDK
version from GitHub Packages using a `read:packages` token. When local changes
to the generator are being tested without remote publication, use the
loopback-only Verdaccio workflow below instead.

## Local package deployment

From the t9t repository root, run:

```sh
bash t9t-api-ts/local-registry/publish-local.sh
```

This starts loopback-only Verdaccio (`http://127.0.0.1:4873`) and an HTTP view of
the local Maven file repository (`http://127.0.0.1:8082/repository/`). It
regenerates API sources, runs the `t9t-api-ts` Maven deploy lifecycle, publishes
the npm package under the `snapshot` tag, and deploys the Maven POM and attached
`npm` classifier without pushing to production registries. The script expects
the TypeScript-capable BON DSL version in `~/.m2`. Its default plugin version is
intended for the script's baseline; when testing a local generator build, set
`BONAPARTE_PLUGIN_VERSION` to the version read from the local `bonaparte-dsl`
root POM before invoking the script.

In the t9t-ui checkout, install the locally published package without changing
`package.json` or `package-lock.json`:

```sh
npm run api:install-local
```

The Verdaccio storage volume persists between Compose restarts. Remove it with
`docker compose -f t9t-api-ts/local-registry/compose.yaml down -v` when a clean
local registry is needed.

When testing a local generator change, pass the local version explicitly:

```sh
BONAPARTE_PLUGIN_VERSION="$BONAPARTE_PLUGIN_VERSION" \
  bash t9t-api-ts/local-registry/publish-local.sh
```