import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const createBackupDuration = new Trend('backup_create_duration');

const nodes = [
    {"id":"132390923902390","name":"cluster-manager:8080","address":"cluster-manager:8080","status":"active","mode":"cluster_manager","isManaged":true},
    {"id":"132390923902390","name":"node1-self-register:8081","address":"node1-self-register:8081","status":"active","mode":"node","isManaged":true},
    {"id":"132390923902390","name":"node2-self-register:8082","address":"node2-self-register:8082","status":"active","mode":"node","isManaged":true},
    {"id":"132390923902390","name":"node3-self-register:8083","address":"node3-self-register:8083","status":"active","mode":"node","isManaged":true},
    {"id":"132390923902390","name":"node4-self-register:8084","address":"node4-self-register:8084","status":"active","mode":"node","isManaged":true},
    {"id":"132390923902390","name":"node5-self-register:8085","address":"node5-self-register:8085","status":"active","mode":"node","isManaged":true},
    {"id":"132390923902390","name":"node6-self-register:8086","address":"node6-self-register:8086","status":"active","mode":"node","isManaged":true}
];

export const options = {
    stages: [
        { duration: '5s', target: 1 },
        { duration: '10s', target: 20 },
        { duration: '10s', target: 20 },
        { duration: '20s', target: 100 },
        { duration: '10s', target: 150 },
        { duration: '5s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<5000'], // 95% of requests should be below 5s
        errors: ['rate<0.5'],              // Error rate should be below 50%
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://cluster-manager:8080';

function randomNode() {
    return nodes[Math.floor(Math.random() * nodes.length)];
}

export default function () {

    const payload = JSON.stringify({
        clientId: 1,
        taskId: Math.floor(1),
        sizeBytes: Math.floor(200),
        nodeDTO: randomNode(),
    });
    // GET /api/v1/cm/backups - List all backups
    const res = http.post(
        `${BASE_URL}/api/v1/cm/backups`,
        payload,
        {
            headers: { 'Content-Type': 'application/json' },
            timeout: '30s',
        }
    );

    const success = check(res, {
        'backup status is 201': (r) => r.status === 201,
        'backup response time < 5000ms': (r) => r.timings.duration < 5000,
    });

    errorRate.add(!success);
    createBackupDuration.add(res.timings.duration);

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
