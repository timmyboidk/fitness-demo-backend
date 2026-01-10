#!/bin/bash

URL="http://localhost:8080/api/data/collect"
CONCURRENCY=5
REQUESTS=20

echo "Starting load test: $REQUESTS requests with $CONCURRENCY concurrency..."

for ((i=1; i<=REQUESTS; i++)); do
  curl -s -X POST $URL \
    -H "Content-Type: application/json" \
    -d "{\"sessionId\": \"s_$i\", \"items\": [{\"moveId\": \"m_squat\", \"score\": $((RANDOM % 40 + 60))}]}" > /dev/null &
  
  if (( i % CONCURRENCY == 0 )); then
    wait
  fi
done

wait
echo "Load test completed."
