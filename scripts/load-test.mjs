#!/usr/bin/env node
/**
 * HTTP load generator for ProInsight authentication flows.
 *
 * It registers N independent users, rotates their refresh tokens and logs
 * them out. Each request uses a distinct X-Forwarded-For hop so the per-IP
 * login/register limiter does not turn the test into a 429 benchmark.
 *
 * Usage:
 *   node scripts/load-test.mjs --users 100 --concurrency 10
 *   node scripts/load-test.mjs --users 500 --concurrency 20 --phases register,refresh
 *
 * The application must be started with SECURITY_TRUST_PROXY_HEADERS=true for
 * the X-Forwarded-For header to be honored. Never run this against production.
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
const totalUsers = positiveInt(args.get('users'), 100, 'users');
const concurrency = positiveInt(args.get('concurrency'), 10, 'concurrency');
const timeoutMs = positiveInt(args.get('timeout-ms'), 60000, 'timeout-ms');
const outputPath = resolve(args.get('output') ?? 'target/load-test-summary.json');
const password = args.get('password') ?? 'LoadTest@12345';
const runId = args.get('run-id') ?? Date.now().toString();
const phases = new Set(
    (args.get('phases') ?? 'register,refresh,logout')
        .split(',')
        .map((phase) => phase.trim())
        .filter(Boolean)
);

const knownPhases = new Set(['register', 'refresh', 'logout']);
for (const phase of phases) {
    if (!knownPhases.has(phase)) {
        throw new Error(`Unknown phase '${phase}'. Use register,refresh,logout.`);
    }
}

const phaseResults = new Map();
for (const phase of phases) {
    phaseResults.set(phase, { latencies: [], statuses: new Map(), errors: [] });
}

let requestSequence = 0;
let completedUsers = 0;
const progressEvery = Math.max(1, Math.floor(totalUsers / 20));

function positiveInt(value, fallback, name) {
    const parsed = Number(value ?? fallback);
    if (!Number.isInteger(parsed) || parsed <= 0) {
        throw new Error(`--${name} must be a positive integer`);
    }
    return parsed;
}

function nextIp() {
    // TEST-NET-3 (203.0.113.0/24). The last hop is unique per request, so the
    // application's trusted-proxy resolver uses it for rate limiting.
    const host = (requestSequence++ % 253) + 1;
    return `10.0.0.1, 203.0.113.${host}`;
}

function userPayload(index) {
    const suffix = String(index).padStart(5, '0');
    return {
        email: `load.${runId}.${suffix}@example.com`,
        password,
        userName: `load_${runId}_${suffix}`,
        cref: `LOAD-${runId}-${suffix}`,
        cpf: `${runId.slice(-6)}${suffix}`.slice(0, 11),
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

async function post(phase, path, body, token) {
    const startedAt = performance.now();
    try {
        const headers = {
            'Content-Type': 'application/json',
            'X-Forwarded-For': nextIp(),
        };
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
        const response = await fetch(`${baseUrl}${path}`, {
            method: 'POST',
            headers,
            body: body === undefined ? undefined : JSON.stringify(body),
            signal: AbortSignal.timeout(timeoutMs),
        });
        const text = await response.text();
        let json = null;
        if (text) {
            try {
                json = JSON.parse(text);
            } catch {
                // Keep the status and latency even if the body is not JSON.
            }
        }
        record(phase, response.status, performance.now() - startedAt, null);
        return { status: response.status, json };
    } catch (error) {
        const message = `${error.name}: ${error.message}`;
        record(phase, 0, performance.now() - startedAt, message);
        return { status: 0, json: null, error: message };
    }
}

async function exerciseUser(index) {
    const payload = userPayload(index);
    const registration = await post('register', '/api/v1/auth/register', payload);
    if (registration.status !== 201 || !registration.json?.refreshToken) {
        return;
    }

    let accessToken = registration.json.token;
    let refreshToken = registration.json.refreshToken;

    if (phases.has('refresh')) {
        const refreshed = await post('refresh', '/api/v1/auth/refresh', { refreshToken });
        if (refreshed.status === 200 && refreshed.json?.refreshToken) {
            accessToken = refreshed.json.token;
            refreshToken = refreshed.json.refreshToken;
        }
    }

    if (phases.has('logout')) {
        await post('logout', '/api/v1/auth/logout', undefined, accessToken);
    }
}

async function runPool() {
    let nextIndex = 0;
    const workerCount = Math.min(concurrency, totalUsers);
    const workers = Array.from({ length: workerCount }, async () => {
        while (true) {
            const index = nextIndex++;
            if (index >= totalUsers) return;
            await exerciseUser(index);
            completedUsers++;
            if (completedUsers % progressEvery === 0 || completedUsers === totalUsers) {
                console.error(`progress ${completedUsers}/${totalUsers}`);
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

function serializableResult(phase) {
    const result = phaseResults.get(phase);
    return {
        phase,
        ...latencySummary(result.latencies),
        statuses: Object.fromEntries([...result.statuses.entries()].sort((a, b) => a[0] - b[0])),
        errors: result.errors,
    };
}

const startedAt = performance.now();
await runPool();
const durationMs = performance.now() - startedAt;

const phasesSummary = [...phases].map(serializableResult);
const requestCount = phasesSummary.reduce((sum, phase) => sum + phase.count, 0);
const summary = {
    generatedAt: new Date().toISOString(),
    environment: {
        baseUrl,
        users: totalUsers,
        concurrency,
        phases: [...phases],
        runId,
        node: process.version,
        timeoutMs,
    },
    durationMs: round(durationMs),
    requests: requestCount,
    requestsPerSecond: round(durationMs > 0 ? (requestCount / durationMs) * 1000 : 0),
    phases: phasesSummary,
    success:
        requestCount > 0 &&
        phasesSummary.every((phase) =>
            Object.entries(phase.statuses).every(([status]) => Number(status) >= 200 && Number(status) < 300)
        ),
};

await mkdir(dirname(outputPath), { recursive: true });
await writeFile(outputPath, `${JSON.stringify(summary, null, 2)}\n`, 'utf8');
console.error(`summary ${outputPath}`);
console.log(JSON.stringify({
    requests: summary.requests,
    requestsPerSecond: summary.requestsPerSecond,
    durationMs: summary.durationMs,
    success: summary.success,
    phases: summary.phases.map((phase) => ({
        phase: phase.phase,
        count: phase.count,
        p95Ms: phase.p95Ms,
        statuses: phase.statuses,
    })),
}));
