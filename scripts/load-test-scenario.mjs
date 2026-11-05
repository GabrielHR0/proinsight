#!/usr/bin/env node
/**
 * Realistic HTTP load scenario for ProInsight.
 *
 * Each virtual user owns a personal academia and a few clients. After a
 * setup phase (register + create clients with an initial IMC), concurrent
 * users repeatedly execute the same flow a personal trainer would:
 *
 *   login -> me -> list clients -> client history -> list protocols
 *         -> pre-evaluation data -> create IMC evaluation -> history again
 *         -> refresh access token -> logout
 *
 * Usage:
 *   node scripts/load-test-scenario.mjs --users 100 --sessions 3 \
 *     --clients-per-user 3 --concurrency 50
 *
 * The application must be started with SECURITY_TRUST_PROXY_HEADERS=true so
 * each request can carry a unique X-Forwarded-For hop. Never run this against
 * production.
 */

import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { performance } from 'node:perf_hooks';

const args = new Map();
for (let i = 2; i < process.argv.length; i++) {
    const arg = process.argv[i];
    if (!arg.startsWith('--')) continue;
    const equals = arg.indexOf('=');
    if (equals !== -1) {
        args.set(arg.slice(2, equals), arg.slice(equals + 1));
    } else {
        const next = process.argv[i + 1];
        if (next && !next.startsWith('--')) {
            args.set(arg.slice(2), next);
            i++;
        } else {
            args.set(arg.slice(2), 'true');
        }
    }
}

const baseUrl = (args.get('base-url') ?? 'http://localhost:8080').replace(/\/+$/, '');
const totalUsers = positiveInt(args.get('users'), 50, 'users');
const sessions = positiveInt(args.get('sessions'), 3, 'sessions');
const clientsPerUser = positiveInt(args.get('clients-per-user'), 3, 'clients-per-user');
const concurrency = positiveInt(args.get('concurrency'), 50, 'concurrency');
const setupConcurrency = positiveInt(args.get('setup-concurrency'), 20, 'setup-concurrency');
const timeoutMs = positiveInt(args.get('timeout-ms'), 120000, 'timeout-ms');
const thinkMs = nonNegativeInt(args.get('think-ms'), 0, 'think-ms');
const outputPath = resolve(args.get('output') ?? 'target/load-scenario.json');
const password = args.get('password') ?? 'LoadTest@12345';
const runId = (args.get('run-id') ?? Date.now().toString()).replace(/[^A-Za-z0-9_-]/g, '');
const protocoloId = args.get('protocolo-id') ?? 'protocolo_imc_oms';

const PHASES = [
    'setup_register',
    'setup_client',
    'login',
    'me',
    'clients_list',
    'history_before',
    'protocols',
    'pre_avaliacao',
    'imc_create',
    'history_after',
    'refresh',
    'logout',
];

const phaseResults = new Map(PHASES.map((phase) => [phase, {
    latencies: [],
    statuses: new Map(),
    errors: [],
}]));
const setupErrors = [];
const measurementErrors = [];
let requestSequence = 0;
let setupCompleted = 0;
let sessionsCompleted = 0;
let usersCompleted = 0;
const setupProgressEvery = Math.max(1, Math.floor(totalUsers / 10));
const sessionProgressEvery = Math.max(1, Math.floor((totalUsers * sessions) / 20));

function positiveInt(value, fallback, name) {
    const parsed = Number(value ?? fallback);
    if (!Number.isInteger(parsed) || parsed <= 0) {
        throw new Error(`--${name} must be a positive integer`);
    }
    return parsed;
}

function nonNegativeInt(value, fallback, name) {
    const parsed = Number(value ?? fallback);
    if (!Number.isInteger(parsed) || parsed < 0) {
        throw new Error(`--${name} must be a non-negative integer`);
    }
    return parsed;
}

function nextIp() {
    // Use a large private range so every request gets a distinct last hop.
    // The application trusts the right-most X-Forwarded-For entry only.
    const n = requestSequence++;
    const a = (n >> 16) & 255;
    const b = (n >> 8) & 255;
    const c = n & 255;
    return `10.0.0.1, 10.${a}.${b}.${c}`;
}

function numericRunSeed() {
    let seed = 0;
    for (const char of runId) {
        seed = (seed * 31 + char.charCodeAt(0)) % 80000000000;
    }
    return seed;
}

const runSeed = numericRunSeed();

function userPayload(index) {
    const suffix = String(index).padStart(5, '0');
    const cpf = String(10000000000 + ((runSeed + index) % 80000000000)).slice(0, 11);
    return {
        email: `load.${runId}.${suffix}@example.com`,
        password,
        userName: `load_${runId}_${suffix}`,
        cref: `LOAD-${runId}-${suffix}`,
        cpf,
    };
}

/**
 * Generates a valid 11-digit CPF from a numeric seed. The client DTO uses
 * Hibernate's @CPF validator, so a random string would make the setup fail.
 */
function validCpf(seed) {
    const base = String(100000000 + (seed % 900000000));
    const digits = base.split('').map(Number);
    const digit = (values, weight) => {
        let sum = 0;
        for (let i = 0; i < values.length; i++) {
            sum += values[i] * (weight - i);
        }
        const result = (sum * 10) % 11;
        return result === 10 ? 0 : result;
    };
    const first = digit(digits, 10);
    const second = digit([...digits, first], 11);
    return `${base}${first}${second}`;
}

function clientPayload(user, index) {
    const n = user.index * clientsPerUser + index;
    return {
        fullName: `Aluno Load ${String(n).padStart(5, '0')}`,
        email: `aluno.${runId}.${String(n).padStart(5, '0')}@example.com`,
        phone: `119${String(n).padStart(8, '0')}`.slice(0, 11),
        cpf: validCpf(n + 1),
        dataNascimento: '1990-05-15',
        sexo: 'MASCULINO',
        academiaId: user.academiaId,
        avaliadorId: user.userId,
        protocoloId,
        pesoGramas: 70000 + (n % 30) * 1000,
        alturaCm: 170 + (n % 15),
        active: true,
    };
}

function record(phase, status, durationMs, error) {
    const result = phaseResults.get(phase);
    result.latencies.push(durationMs);
    result.statuses.set(status, (result.statuses.get(status) ?? 0) + 1);
    if (error && result.errors.length < 5) {
        result.errors.push(error);
    }
}

function isSuccess(status) {
    return status >= 200 && status < 300;
}

async function request(phase, method, path, options = {}) {
    const startedAt = performance.now();
    try {
        const headers = { 'X-Forwarded-For': nextIp() };
        if (options.body !== undefined) {
            headers['Content-Type'] = 'application/json';
        }
        if (options.token) {
            headers.Authorization = `Bearer ${options.token}`;
        }
        if (options.academiaId) {
            headers['X-Academia-Id'] = options.academiaId;
        }
        const response = await fetch(`${baseUrl}${path}`, {
            method,
            headers,
            body: options.body === undefined ? undefined : JSON.stringify(options.body),
            signal: AbortSignal.timeout(timeoutMs),
        });
        const text = await response.text();
        let json = null;
        if (text) {
            try {
                json = JSON.parse(text);
            } catch {
                // Keep status and latency even when the body is not JSON.
            }
        }
        const error = isSuccess(response.status)
            ? null
            : `HTTP ${response.status}: ${text.slice(0, 300)}`;
        record(phase, response.status, performance.now() - startedAt, error);
        return { status: response.status, json, text };
    } catch (error) {
        const message = `${error.name}: ${error.message}`;
        record(phase, 0, performance.now() - startedAt, message);
        return { status: 0, json: null, text: null, error: message };
    }
}

function sleep(ms) {
    return new Promise((resolvePromise) => setTimeout(resolvePromise, ms));
}

async function setupUser(index) {
    const payload = userPayload(index);
    const registration = await request('setup_register', 'POST', '/api/v1/auth/register', {
        body: payload,
    });
    if (registration.status !== 201 || !registration.json?.token) {
        throw new Error(`register failed: ${registration.status}`);
    }

    const academiaId = Object.keys(registration.json.academiaPermissoes ?? {})[0];
    if (!academiaId) {
        throw new Error('registration did not return an academia');
    }

    const user = {
        index,
        email: payload.email,
        userId: registration.json.userId,
        academiaId,
        clients: [],
    };

    for (let clientIndex = 0; clientIndex < clientsPerUser; clientIndex++) {
        const response = await request('setup_client', 'POST', '/api/v1/clientes/com-imc', {
            body: clientPayload(user, clientIndex),
            token: registration.json.token,
            academiaId,
        });
        if (response.status !== 201 || !response.json?.cliente?.id) {
            throw new Error(`client setup failed: ${response.status}`);
        }
        user.clients.push(response.json.cliente.id);
    }

    return user;
}

async function setupUsers() {
    const users = [];
    let nextIndex = 0;
    const workerCount = Math.min(setupConcurrency, totalUsers);
    const workers = Array.from({ length: workerCount }, async () => {
        while (true) {
            const index = nextIndex++;
            if (index >= totalUsers) return;
            try {
                users.push(await setupUser(index));
            } catch (error) {
                if (setupErrors.length < 10) {
                    setupErrors.push(`user ${index}: ${error.message}`);
                }
            } finally {
                setupCompleted++;
                if (setupCompleted % setupProgressEvery === 0 || setupCompleted === totalUsers) {
                    console.error(`setup ${setupCompleted}/${totalUsers}`);
                }
            }
        }
    });
    await Promise.all(workers);
    return users.sort((a, b) => a.index - b.index);
}

async function exerciseSession(user, sessionIndex) {
    const login = await request('login', 'POST', '/api/v1/auth/login', {
        body: { login: user.email, password },
    });
    if (login.status !== 200 || !login.json?.token) {
        throw new Error(`login failed: ${login.status}`);
    }

    let accessToken = login.json.token;
    let refreshToken = login.json.refreshToken;
    const auth = { token: accessToken, academiaId: user.academiaId };

    await request('me', 'GET', '/api/v1/auth/me', auth);
    if (thinkMs > 0) await sleep(thinkMs);

    await request('clients_list', 'GET', '/api/v1/clientes', auth);
    if (thinkMs > 0) await sleep(thinkMs);

    const clientId = user.clients[(user.index + sessionIndex) % user.clients.length];
    await request('history_before', 'GET', `/api/v1/clientes/${clientId}/avaliacoes`, auth);
    if (thinkMs > 0) await sleep(thinkMs);

    await request('protocols', 'GET', '/api/v1/avaliacoes/protocolos', auth);
    if (thinkMs > 0) await sleep(thinkMs);

    await request(
        'pre_avaliacao',
        'GET',
        `/api/v1/avaliacoes/${protocoloId}/dados-pre-avaliacao/${clientId}`,
        auth,
    );
    if (thinkMs > 0) await sleep(thinkMs);

    const imc = await request('imc_create', 'POST', '/api/v1/avaliacoes/imc', {
        ...auth,
        body: {
            cliente_id: clientId,
            protocolo_id: protocoloId,
            avaliador_id: user.userId,
            peso_gramas: 70000 + (user.index % 20) * 1000,
            altura_cm: 170 + (user.index % 15),
        },
    });
    if (imc.status !== 200) {
        throw new Error(`IMC evaluation failed: ${imc.status}`);
    }

    await request('history_after', 'GET', `/api/v1/clientes/${clientId}/avaliacoes`, auth);
    if (thinkMs > 0) await sleep(thinkMs);

    const refreshed = await request('refresh', 'POST', '/api/v1/auth/refresh', {
        body: { refreshToken },
    });
    if (refreshed.status === 200 && refreshed.json?.token) {
        accessToken = refreshed.json.token;
        refreshToken = refreshed.json.refreshToken;
    }

    await request('logout', 'POST', '/api/v1/auth/logout', {
        token: accessToken,
        academiaId: user.academiaId,
    });
}

async function runMeasurement(users) {
    let nextIndex = 0;
    const workerCount = Math.min(concurrency, users.length);
    const workers = Array.from({ length: workerCount }, async () => {
        while (true) {
            const index = nextIndex++;
            if (index >= users.length) return;
            const user = users[index];
            try {
                for (let sessionIndex = 0; sessionIndex < sessions; sessionIndex++) {
                    await exerciseSession(user, sessionIndex);
                    sessionsCompleted++;
                    if (sessionsCompleted % sessionProgressEvery === 0
                            || sessionsCompleted === users.length * sessions) {
                        console.error(`sessions ${sessionsCompleted}/${users.length * sessions}`);
                    }
                }
            } catch (error) {
                if (measurementErrors.length < 10) {
                    measurementErrors.push(`user ${user.index}: ${error.message}`);
                }
            } finally {
                usersCompleted++;
            }
        }
    });
    await Promise.all(workers);
}

function latencySummary(latencies) {
    if (latencies.length === 0) {
        return { count: 0, avgMs: 0, p50Ms: 0, p95Ms: 0, p99Ms: 0, maxMs: 0 };
    }
    const sorted = [...latencies].sort((a, b) => a - b);
    const percentile = (value) => {
        const index = Math.min(sorted.length - 1, Math.max(0, Math.ceil((value / 100) * sorted.length) - 1));
        return sorted[index];
    };
    const total = sorted.reduce((sum, value) => sum + value, 0);
    return {
        count: sorted.length,
        avgMs: round(total / sorted.length),
        p50Ms: round(percentile(50)),
        p95Ms: round(percentile(95)),
        p99Ms: round(percentile(99)),
        maxMs: round(sorted[sorted.length - 1]),
    };
}

function round(value) {
    return Math.round(value * 100) / 100;
}

function serializablePhase(phase) {
    const result = phaseResults.get(phase);
    return {
        phase,
        ...latencySummary(result.latencies),
        statuses: Object.fromEntries([...result.statuses.entries()].sort((a, b) => a[0] - b[0])),
        errors: result.errors,
    };
}

const setupStartedAt = performance.now();
const users = await setupUsers();
const setupDurationMs = performance.now() - setupStartedAt;

const measurementStartedAt = performance.now();
await runMeasurement(users);
const measurementDurationMs = performance.now() - measurementStartedAt;

const phasesSummary = PHASES.map(serializablePhase);
const setupRequests = phasesSummary
    .filter((phase) => phase.phase.startsWith('setup_'))
    .reduce((sum, phase) => sum + phase.count, 0);
const measurementRequests = phasesSummary
    .filter((phase) => !phase.phase.startsWith('setup_'))
    .reduce((sum, phase) => sum + phase.count, 0);

const summary = {
    generatedAt: new Date().toISOString(),
    environment: {
        baseUrl,
        users: totalUsers,
        sessions,
        clientsPerUser,
        concurrency,
        setupConcurrency,
        thinkMs,
        timeoutMs,
        protocoloId,
        runId,
        node: process.version,
    },
    setup: {
        usersReady: users.length,
        usersFailed: totalUsers - users.length,
        durationMs: round(setupDurationMs),
        requests: setupRequests,
        errors: setupErrors,
    },
    measurement: {
        usersCompleted,
        sessionsCompleted,
        sessionsExpected: users.length * sessions,
        durationMs: round(measurementDurationMs),
        requests: measurementRequests,
        requestsPerSecond: round(
            measurementDurationMs > 0 ? (measurementRequests / measurementDurationMs) * 1000 : 0,
        ),
        errors: measurementErrors,
    },
    phases: phasesSummary,
    success:
        users.length === totalUsers
        && usersCompleted === users.length
        && sessionsCompleted === users.length * sessions
        && phasesSummary.every((phase) =>
            Object.entries(phase.statuses).every(([status]) => isSuccess(Number(status)))),
};

await mkdir(dirname(outputPath), { recursive: true });
await writeFile(outputPath, `${JSON.stringify(summary, null, 2)}\n`, 'utf8');
console.error(`summary ${outputPath}`);
console.log(JSON.stringify({
    setup: summary.setup,
    measurement: summary.measurement,
    phases: summary.phases.map((phase) => ({
        phase: phase.phase,
        count: phase.count,
        p50Ms: phase.p50Ms,
        p95Ms: phase.p95Ms,
        p99Ms: phase.p99Ms,
        statuses: phase.statuses,
    })),
    success: summary.success,
}));
