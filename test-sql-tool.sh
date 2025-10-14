#!/bin/bash

# Test SQL Tool - DevAssist AI
# This script tests the SQL tool functionality with various queries

BASE_URL="http://localhost:9090"

echo "🧪 Testing SQL Tool..."
echo "===================="
echo ""

# Test 1: Find 1 error log and explain
echo "📋 Test 1: Finding 1 log from app_logs table and asking AI to explain root cause"
echo "---"
echo "Note: The AI should automatically query the 'app_logs' table (plural)"
curl -X 'POST' \
  "${BASE_URL}/ai-test/chat/sql" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "find 1 log from app_logs table and explain me the root cause",
    "sessionId": "test-session-1"
  }'
echo ""
echo ""
echo "===================="
echo ""

# Test 2: Find all ERROR logs
echo "📋 Test 2: Finding all ERROR level logs"
echo "---"
curl -X 'POST' \
  "${BASE_URL}/ai-test/chat/sql" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Show me all ERROR level logs from the app_logs table",
    "sessionId": "test-session-2"
  }'
echo ""
echo ""
echo "===================="
echo ""

# Test 3: Find NullPointerException logs
echo "📋 Test 3: Finding NullPointerException logs and explain"
echo "---"
curl -X 'POST' \
  "${BASE_URL}/ai-test/chat/sql" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find any NullPointerException in the logs and explain what went wrong",
    "sessionId": "test-session-3"
  }'
echo ""
echo ""
echo "===================="
echo ""

# Test 4: Find OutOfMemoryError
echo "📋 Test 4: Finding OutOfMemoryError and providing solution"
echo "---"
curl -X 'POST' \
  "${BASE_URL}/ai-test/chat/sql" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Search for OutOfMemoryError in the logs and suggest how to fix it",
    "sessionId": "test-session-4"
  }'
echo ""
echo ""
echo "===================="
echo "✅ All tests completed!"

