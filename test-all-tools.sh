#!/bin/bash

# Test script for all DevAssist AI tools
# Demonstrates single-tool and multi-tool endpoints

BASE_URL="http://localhost:9090/ai-test"

echo "=========================================="
echo "DevAssist AI - Tool Testing Suite"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ==================== TEST 1: SQL Tool ====================
echo -e "${BLUE}TEST 1: SQL Tool${NC}"
echo "Query: Find 1 error log from app_logs table"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/sql" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "Find 1 error log from app_logs table and explain the root cause",
  "sessionId": "test-sql-1"
}'

echo -e "\n\n"

# ==================== TEST 2: FileSystem Tool ====================
echo -e "${BLUE}TEST 2: FileSystem Tool - List Files${NC}"
echo "Query: List all Java files in src/main/java/com/devassist"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/filesystem" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "List all files in src/main/java/com/devassist directory",
  "sessionId": "test-fs-1"
}'

echo -e "\n\n"

# ==================== TEST 3: FileSystem Tool - Read File ====================
echo -e "${BLUE}TEST 3: FileSystem Tool - Read File${NC}"
echo "Query: Read the main application class"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/filesystem" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "Read the file src/main/java/com/devassist/DevassistApplication.java and explain what it does",
  "sessionId": "test-fs-2"
}'

echo -e "\n\n"

# ==================== TEST 4: HTTP Tool - Check URL ====================
echo -e "${BLUE}TEST 4: HTTP Tool - Check URL${NC}"
echo "Query: Check if Google is reachable"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/http" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "Check if https://www.google.com is reachable",
  "sessionId": "test-http-1"
}'

echo -e "\n\n"

# ==================== TEST 5: HTTP Tool - GET Request ====================
echo -e "${BLUE}TEST 5: HTTP Tool - GET Request${NC}"
echo "Query: Fetch data from JSONPlaceholder API"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/http" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "Make a GET request to https://jsonplaceholder.typicode.com/posts/1 and tell me what the post is about",
  "sessionId": "test-http-2"
}'

echo -e "\n\n"

# ==================== TEST 6: Multi-Tool - SQL + FileSystem ====================
echo -e "${BLUE}TEST 6: Multi-Tool - SQL + FileSystem${NC}"
echo "Query: Find errors in logs and show related code"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/multi-tool" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "Find 1 error from app_logs table, then read the SqlTool.java file and explain if there are any issues",
  "sessionId": "test-multi-1"
}'

echo -e "\n\n"

# ==================== TEST 7: Multi-Tool - FileSystem + HTTP ====================
echo -e "${BLUE}TEST 7: Multi-Tool - FileSystem + HTTP${NC}"
echo "Query: List project structure and check external API"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/multi-tool" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "List all Java files in src/main/java/com/devassist/tools directory, then check if https://api.github.com is reachable",
  "sessionId": "test-multi-2"
}'

echo -e "\n\n"

# ==================== TEST 8: Multi-Tool - All 3 Tools ====================
echo -e "${BLUE}TEST 8: Multi-Tool - SQL + FileSystem + HTTP${NC}"
echo "Query: Complex multi-tool orchestration"
echo ""

curl -X 'POST' \
  "${BASE_URL}/chat/multi-tool" \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "task": "First, check if https://www.google.com is up. Then list all tool files in src/main/java/com/devassist/tools. Finally, query app_logs for any errors.",
  "sessionId": "test-multi-3"
}'

echo -e "\n\n"

echo -e "${GREEN}=========================================="
echo "All tests completed!"
echo -e "==========================================${NC}"

