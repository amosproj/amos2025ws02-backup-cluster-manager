import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const backupDuration = new Trend('backup_request_duration');

// Test configuration with 5 stages
export const options = {
    stages: [
        { duration: '5s', target: 1 },        // Stage 1: Warm-up with 1 VU
        { duration: '20s', target: 100 },   // Stage 2: Ramp up to 100 concurrent users - can't go higher than 100, postgres concurrency limit
        { duration: '5s', target: 0 },        // Cool-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<5000'], // 95% of requests should be below 5s
        errors: ['rate<0.5'],              // Error rate should be below 50%
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://cluster-manager:8080';

export default function () {
    // GET /api/v1/cm/backups - List all backups
    const listResponse = http.get(`${BASE_URL}/api/v1/cm/backups`, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '30s',
    });

    const listSuccess = check(listResponse, {
        'list backups status is 200': (r) => r.status === 200,
        'list backups response time < 5000ms': (r) => r.timings.duration < 5000,
    });

    errorRate.add(!listSuccess);
    backupDuration.add(listResponse.timings.duration);

    sleep(0.1); // Small pause between requests
}

export function handleSummary(data) {
    const summary = {
        total_requests: data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0,
        avg_duration_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values.avg : 0,
        p95_duration_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values['p(95)'] : 0,
        max_duration_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values.max : 0,
        min_duration_ms: data.metrics.http_req_duration ? data.metrics.http_req_duration.values.min : 0,
        fail_rate: data.metrics.errors ? data.metrics.errors.values.rate : 0,
        vus_max: data.metrics.vus_max ? data.metrics.vus_max.values.max : 0,
    };

    return {
        'stdout': JSON.stringify(summary, null, 2)
    };
}
