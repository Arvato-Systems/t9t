import { cpSync, existsSync, mkdirSync, readFileSync, rmSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const sourceRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const targetDirectory = resolve(process.env.MAVEN_TARGET_DIRECTORY ?? join(sourceRoot, '../../../target'));
const workspace = join(targetDirectory, 'npm-src');
const inputs = [
    '.npmrc.template',
    'README.md',
    'ng-package.json',
    'package.json',
    'rpc-core',
    'scripts',
    'test-setup.ts',
    'tsconfig.json',
    'tsconfig.lib.json',
    'tsconfig.lib.prod.json',
    'vitest.config.ts',
];

rmSync(workspace, { recursive: true, force: true });
mkdirSync(workspace, { recursive: true });
for (const input of inputs) {
    const source = join(sourceRoot, input);
    if (!existsSync(source))
        throw new Error(`Missing npm package input: ${source}`);
    cpSync(source, join(workspace, input), { recursive: true });
}

const registryUrl = process.env.NPM_REGISTRY_URL || 'https://npm.pkg.github.com';
const npmCli = process.env.npm_execpath;
if (!npmCli)
    throw new Error('Run workspace preparation through npm so snapshot dependencies can be installed');

const manifest = JSON.parse(readFileSync(join(workspace, 'package.json'), 'utf8'));
const internalPackages = ['dependencies', 'peerDependencies']
    .flatMap(group => Object.entries(manifest[group] ?? {}).map(([name, range]) => ({ group, name, range })))
    .filter(({ name }) => name.startsWith('@arvato-systems-jacs/'));
const registryArgs = ['--registry=https://registry.npmjs.org', `--@arvato-systems-jacs:registry=${registryUrl}`];
const debugEnabled = process.env.DEBUG_NPM_WORKSPACE === '1';

function debug(message) {
    if (debugEnabled)
        console.error(`[prepare-package-workspace] ${message}`);
}

function describeCommand(args) {
    const safeArgs = args.filter(arg => !arg.startsWith('--registry=') && !arg.includes(':registry='));
    return `npm ${safeArgs.join(' ')}`;
}

function describeFailure(command, result) {
    const details = [
        `Command: ${command}`,
        `Working directory: ${workspace}`,
        `Exit status: ${result.status ?? 'not available'}`,
        result.signal && `Signal: ${result.signal}`,
        result.error && `Process error: ${result.error.message}`,
        result.stdout?.trim() && `stdout:\n${result.stdout.trim()}`,
        result.stderr?.trim() && `stderr:\n${result.stderr.trim()}`,
    ].filter(Boolean);
    return details.join('\n');
}

debug(`Workspace: ${workspace}`);

function npmJson(args) {
    const command = describeCommand(args);
    debug(`Running ${command}`);
    const result = spawnSync(process.execPath, [npmCli, ...args, '--json', ...registryArgs], {
        cwd: workspace,
        encoding: 'utf8',
    });
    if (result.error || result.status !== 0)
        throw new Error(`npm query failed:\n${describeFailure(command, result)}`);

    try {
        return JSON.parse(result.stdout);
    } catch (error) {
        throw new Error(`npm query returned invalid JSON:\n${describeFailure(command, result)}`, { cause: error });
    }
}

const packagesToInstall = [];
for (const { name, range } of internalPackages) {
    const available = npmJson(['view', `${name}@${range}`, 'version']);
    const versions = Array.isArray(available) ? available : [available];
    const resolvedVersion = versions.at(-1);
    if (!resolvedVersion)
        throw new Error(`No published version of ${name} satisfies ${range}`);
    packagesToInstall.push(`${name}@${resolvedVersion}`);
    debug(`Resolved ${name}@${range} to ${resolvedVersion}`);
}

const installArgs = [
    npmCli,
    'install',
    '--no-save',
    '--package-lock=false',
    '--ignore-scripts',
    '--no-audit',
    '--no-fund',
    ...packagesToInstall,
    ...registryArgs,
];
const installCommand = describeCommand(installArgs.slice(1));
debug(`Installing ${packagesToInstall.length} internal snapshot package(s)`);
const install = spawnSync(process.execPath, installArgs, { cwd: workspace, stdio: 'inherit' });
if (install.error || install.status !== 0)
    throw new Error(`Could not install latest compatible internal snapshots:\n${describeFailure(installCommand, install)}`);

rmSync(join(workspace, 'package-lock.json'), { force: true });
console.log(`Prepared npm workspace at ${workspace}`);