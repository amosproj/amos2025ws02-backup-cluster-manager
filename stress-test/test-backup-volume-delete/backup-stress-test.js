import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/latest/dist/bundle.js';


// Custom metrics
const errorRate = new Rate('errors');
const deleteBackupDuration = new Trend('backup_delete_duration');

const ITERATION_MAX_VUS = __ENV.ITERATION_MAX_VUS || 100;

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
        { duration: '20s', target: ITERATION_MAX_VUS },
        { duration: '20s', target: ITERATION_MAX_VUS },
        { duration: '5s', target: 1 },
    ],
    thresholds: {
        'http_req_duration{type:delete_backup}': ['p(95)<5000'], // Only care about delete speed
        'errors{type:delete_backup}': ['rate<0.5'],             // Strict on delete fails
    }
};

const BASE_URL = __ENV.BASE_URL || 'http://cluster-manager:8080';

function randomNode() {
    return nodes[Math.floor(Math.random() * nodes.length)];
}

export default function () {
    // 1. Create a new backup
    const node = randomNode();
    const payload = JSON.stringify({
        clientId: 1,
        taskId: Math.floor(1),
        sizeBytes: Math.floor(200),
        nodeDTO: node,
    });

    const createRes = http.post(
        `${BASE_URL}/api/v1/cm/backups`,
        payload,
        {
            headers: { 'Content-Type': 'application/json' },
            timeout: '30s',
            tags: { type: 'create_backup' },
        }
    );

    const createSuccess = check(createRes, {
        'create backup status is 201': (r) => r.status === 201,
    }, { type: 'create_backup' });

    if (!createSuccess) {
        errorRate.add(1, { type: 'create_backup' });
        console.log('Failed to create backup, skipping deletion');
        sleep(1);
        return;
    }

    // Extract the created backup details
    const createdBackup = createRes.json();
    const backupId = createdBackup.id;
    const nodeAddress = createdBackup.nodeDTO.address;

    // 2. Delete the backup we just created
    const deleteRes = http.del(
        `${BASE_URL}/api/v1/cm/backups/${backupId}?nodeAddress=${encodeURIComponent(nodeAddress)}`,
        null,
        {
            timeout: '30s',
            tags: { type: 'delete_backup' },
        }
    );

    const deleteSuccess = check(deleteRes, {
        'delete backup status is 204': (r) => r.status === 204,
        'delete response time < 5000ms': (r) => r.timings.duration < 5000,
    }, { type: 'delete_backup' });

    errorRate.add(!deleteSuccess, { type: 'delete_backup' });
    deleteBackupDuration.add(deleteRes.timings.duration, { type: 'delete_backup' });

    sleep(0.1); // Small pause between requests
}

export function handleSummary(data) {
    // Extract metrics specifically for delete_backup type if available
    const deleteErrors = data.metrics['errors{type:delete_backup}']
        ? data.metrics['errors{type:delete_backup}'].values.rate
        : 0;

    const summary = {
        total_deletes: data.metrics.backup_delete_duration.values.count,
        avg_delete_duration_ms: data.metrics.backup_delete_duration.values.avg,
        p95_delete_duration_ms: data.metrics.backup_delete_duration ? data.metrics.backup_delete_duration.values['p(95)'] : 0,
        max_delete_duration_ms: data.metrics.backup_delete_duration ? data.metrics.backup_delete_duration.values.max : 0,
        min_delete_duration_ms: data.metrics.backup_delete_duration ? data.metrics.backup_delete_duration.values.min : 0,
        fail_rate_deletes_only: deleteErrors,
        vus_max: data.metrics.vus_max ? data.metrics.vus_max.values.max : 0,
    };

    return {
        'stdout': JSON.stringify(summary, null, 2),
        'summary.html': htmlReport(data)

    };
}
