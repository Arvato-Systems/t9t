import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const reactorRoot = path.resolve(process.env.T9T_REACTOR_ROOT ?? path.resolve(packageRoot, '../../../../'));
const targetDirectory = path.resolve(process.env.MAVEN_TARGET_DIRECTORY ?? path.resolve(packageRoot, '../../../target'));
const manifestPath = path.join(targetDirectory, 'generated-ts-entrypoints.json');
const stagingRoot = path.join(targetDirectory, 'npm-src');

function safeEntryPoint(name) {
    return /^[A-Za-z0-9._-]+$/.test(name) && name !== '.' && name !== '..';
}

if (existsSync(manifestPath)) {
    const previous = JSON.parse(readFileSync(manifestPath, 'utf8'));
    for (const name of previous) {
        if (!safeEntryPoint(name))
            throw new Error(`Invalid generated entrypoint in manifest: ${name}`);
        rmSync(path.join(packageRoot, name), { recursive: true, force: true });
    }
}

const entryPoints = new Map();
const nestedEntryPoints = new Map();
for (const moduleName of readdirSync(reactorRoot).sort()) {
    if (!moduleName.startsWith('t9t-'))
        continue;
    const moduleRoot = path.join(reactorRoot, moduleName);
    const generatedRoot = path.join(moduleRoot, 'src/generated/ts');
    if (!existsSync(generatedRoot) || moduleName === 't9t-api-ts')
        continue;
    const ownerRoot = path.join(generatedRoot, moduleName);
    const moduleEntryPoint = existsSync(path.join(ownerRoot, 'ng-package.json'))
        ? ownerRoot
        : existsSync(path.join(generatedRoot, 'ng-package.json')) ? generatedRoot : null;
    if (moduleEntryPoint)
        entryPoints.set(moduleName, moduleEntryPoint);

    for (const name of readdirSync(generatedRoot).sort()) {
        if (!safeEntryPoint(name))
            continue;
        const source = path.join(generatedRoot, name);
        if (!existsSync(path.join(source, 'ng-package.json')))
            continue;
        if (name === moduleName && source === moduleEntryPoint)
            continue;
        if (entryPoints.has(name)) {
            console.warn(`Ignoring duplicate generated entrypoint '${name}' in ${source}; using ${entryPoints.get(name)}`);
            continue;
        }
        if (nestedEntryPoints.has(name)) {
            console.warn(`Ignoring duplicate generated entrypoint '${name}' in ${source}; using ${nestedEntryPoints.get(name)}`);
            continue;
        }
        nestedEntryPoints.set(name, source);
    }
}

for (const [name, source] of nestedEntryPoints)
    entryPoints.set(name, source);

mkdirSync(targetDirectory, { recursive: true });
for (const [name, source] of entryPoints) {
    const destination = path.join(stagingRoot, name);
    if (existsSync(destination))
        throw new Error(`Refusing to overwrite non-generated package directory: ${destination}`);
    cpSync(source, destination, {
        recursive: true,
        errorOnExist: true,
        force: false,
        filter: sourcePath => {
            const relative = path.relative(source, sourcePath);
            if (!relative)
                return true;
            const firstSegment = relative.split(path.sep)[0];
            return relative !== firstSegment || !existsSync(path.join(source, firstSegment, 'ng-package.json'));
        },
    });
}

writeFileSync(manifestPath, `${JSON.stringify([...entryPoints.keys()], null, 2)}\n`);
console.log(`Assembled ${entryPoints.size} generated TypeScript entrypoints from ${reactorRoot}`);