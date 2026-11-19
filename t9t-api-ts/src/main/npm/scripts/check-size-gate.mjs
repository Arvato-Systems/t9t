import { build } from 'esbuild';
import { readdir } from 'node:fs/promises';
import { join, resolve } from 'node:path';

const dist = resolve('../../../target/npm-dist');
const bundles = join(dist, 'fesm2022');
const files = (await readdir(bundles)).filter((file) => file.endsWith('.mjs'));
const primary = files.find((file) => file === 'arvato-systems-jacs-t9t-api.mjs');
const secondary = files.filter((file) => file !== primary);

if (!primary || secondary.length === 0) {
  throw new Error('Expected the primary entry point and at least one secondary entry point in target/npm-dist');
}

for (const bundle of [primary, secondary[0]]) {
  const input = join(bundles, bundle);
  const result = await build({
    entryPoints: [input],
    bundle: true,
    minify: true,
    write: false,
    metafile: true,
    external: ['@angular/*', 'rxjs', 'tslib'],
  });
  const outputBytes = result.outputFiles.reduce((size, output) => size + output.contents.byteLength, 0);
  if (outputBytes > 20 * 1024) {
    throw new Error(`${bundle} bundle is ${outputBytes} bytes; the limit is 20480 bytes`);
  }

  const secondaryInputs = Object.keys(result.metafile.inputs)
    .filter((path) => files.some((candidate) => path.endsWith(candidate)) && !path.endsWith(bundle));
  if (secondaryInputs.length > 0) {
    throw new Error(`${bundle} pulls in another entry point: ${secondaryInputs.join(', ')}`);
  }
  console.log(`${bundle}: ${outputBytes} bytes minified`);
}