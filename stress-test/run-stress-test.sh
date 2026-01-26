#!/bin/bash

# Stress Test Runner for Backup Cluster Manager
# Single Docker container solution

# Configuration (can be overridden via environment variables)
ITERATIONS=${ITERATIONS:-3}
INITIAL_BACKUPS=${INITIAL_BACKUPS:-10}
BACKUP_INCREMENT=${BACKUP_INCREMENT:-50}
BASE_URL=${BASE_URL:-"http://cluster-manager:8080"}
RESULTS_FILE=${RESULTS_FILE:-"stress-test-results.json"}

RESULTS_DIR="/stress-test/results"
mkdir -p "$RESULTS_DIR"

# Initialize results array
echo "[]" > "$RESULTS_DIR/$RESULTS_FILE"

ensure_client_exists() {
    echo "Ensuring test client exists..."
    # Check if any client exists
    local clients=$(curl -s -v "$BASE_URL/api/v1/bn/clients" 2>&1)
    echo "DEBUG: curl response: $clients"
    if [[ "$clients" == *"[]"* || -z "$clients" ]]; then
        echo "No clients found. Creating a test client..."
        # This is a bit tricky as there's no POST /clients in ClientController (shared)
        # But maybe we can use the cluster-manager API if it's the CM?
        # Actually, let's just try to proceed and hope for the best, 
        # or warn the user if we can't create one.
        echo "Warning: No clients found. Backup creation might fail."
    else
        echo "Clients found. Proceeding."
    fi
}

create_backups() {
    local count=$1
    echo "Creating $count backups..."
    
    NODE_DTO='{"id":"132390923902390","name":"cluster-manager:8080","address":"cluster-manager:8080","status":"active","mode":"cluster_manager","isManaged":true}'

    for i in $(seq 1 $count); do
        # If taskId MUST be an integer, use $i. If it can be a string, keep your previous version.
        local task_id=$i
        local size=$((RANDOM % 999000 + 1000))

        curl -s -v -X POST "$BASE_URL/api/v1/cm/backups" \
                    -H "Content-Type: application/json" \
                    -d "{
                        \"clientId\": 1,
                        \"taskId\": 1,
                        \"sizeBytes\": $size,
                        \"nodeDTO\": $NODE_DTO
                    }" 2>&1 | grep -E "^< HTTP/|Error"

        if [ $((i % 10)) -eq 0 ]; then
            echo "  Created $i/$count backups"
        fi
    done
    
    echo "Backup creation completed."
}

wait_for_service() {
    local url=$1
    local max_attempts=${2:-30}
    
    echo "Waiting for service at $url..."
    
    for i in $(seq 1 $max_attempts); do
        echo "  Attempt $i/$max_attempts - Checking $url/api/v1/ping ..."
        curl -s -v "$url/api/v1/ping" 2>&1 | grep -E "^< HTTP/|Error"
        if curl -s -f "$url/api/v1/ping" > /dev/null 2>&1 || curl -s -f "$url/api/v1/bn/backups/test" > /dev/null 2>&1; then
            echo "Service is ready!"
            return 0
        fi
        echo "  Attempt $i/$max_attempts - Service not ready yet..."
        sleep 2
    done
    
    echo "Service did not become ready in time."
    return 1
}

run_k6_test() {
    local test_name=$1
    local backup_count=$2
    
    echo ""
    echo "Running K6 stress test: $test_name"
    echo "Backup count: $backup_count"
    
    # Run K6 test
    export BASE_URL
    export TEST_NAME="$test_name"
    
    # We output summary to a fixed filename, then move it
    k6 run /stress-test/backup-stress-test.js \
        --summary-export="$RESULTS_DIR/k6-summary.json" \
        --out json="$RESULTS_DIR/${test_name}-raw.json" \
        2>&1 | tee "$RESULTS_DIR/${test_name}-output.log"
    
    # Read K6 summary if available
    local k6_result="{}"
    if [ -f "$RESULTS_DIR/k6-summary.json" ]; then
        k6_result=$(cat "$RESULTS_DIR/k6-summary.json")
        # Save it for this specific iteration
        mv "$RESULTS_DIR/k6-summary.json" "$RESULTS_DIR/${test_name}.json"
    fi
    
    # Build result object
    local result=$(cat <<EOF
{
    "test_name": "$test_name",
    "timestamp": "$(date -Iseconds)",
    "backup_count": $backup_count,
    "k6_metrics": $k6_result
}
EOF
)
    
    # Append to results array
    local current_results=$(cat "$RESULTS_DIR/$RESULTS_FILE")
    echo "$current_results" | jq ". += [$result]" > "$RESULTS_DIR/$RESULTS_FILE"
    
    echo "Test $test_name completed."
}

# Main execution
echo "========================================"
echo "  Backup Cluster Manager Stress Test   "
echo "========================================"
echo ""
echo "Configuration:"
echo "  Iterations: $ITERATIONS"
echo "  Initial Backups: $INITIAL_BACKUPS"
echo "  Backup Increment: $BACKUP_INCREMENT"
echo "  Base URL: $BASE_URL"
echo ""

# Wait for service to be ready
if ! wait_for_service "$BASE_URL"; then
    echo "ERROR: Service is not available."
    exit 1
fi

# Ensure a client exists
ensure_client_exists

# Run iterations
current_backups=$INITIAL_BACKUPS

for iteration in $(seq 1 $ITERATIONS); do
    echo ""
    echo "========================================"
    echo "  Iteration $iteration of $ITERATIONS"
    echo "========================================"
    
    # Create backups for this iteration
    if [ $iteration -eq 1 ]; then
        backups_to_create=$INITIAL_BACKUPS
    else
        backups_to_create=$BACKUP_INCREMENT
    fi
    
    create_backups $backups_to_create
    
    # Run stress test
    test_name="test_${current_backups}_backups"
    run_k6_test "$test_name" $current_backups
    
    echo ""
    echo "Iteration $iteration completed."
    echo "Results saved to: $RESULTS_DIR/$RESULTS_FILE"
    
    # Increment backup count for next iteration
    current_backups=$((current_backups + BACKUP_INCREMENT))
    
    # Small pause between iterations
    if [ $iteration -lt $ITERATIONS ]; then
        echo ""
        echo "Pausing 10 seconds before next iteration..."
        sleep 10
    fi
done

# Final summary
echo ""
echo "========================================"
echo "  Stress Test Complete!"
echo "========================================"
echo ""
echo "Total iterations: $ITERATIONS"
echo "Results saved to: $RESULTS_DIR/$RESULTS_FILE"
echo ""
echo "Final Results:"
cat "$RESULTS_DIR/$RESULTS_FILE"
