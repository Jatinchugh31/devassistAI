#!/bin/bash

# Test script for multi-tool orchestration scenarios

BASE_URL="http://localhost:9090/ai-test"

echo "=========================================="
echo "Multi-Tool Orchestration Test Suite"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ==================== TEST 1: SQL + FileSystem ====================
echo -e "${BLUE}TEST 1: SQL + FileSystem Tool Chaining${NC}"
echo "Query: Find errors in SqlTool, then read the SqlTool.java file"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find all ERROR logs from the devassist service related to SqlTool, then read the SqlTool.java file and explain if the errors are related to the code",
    "sessionId": "multi-1"
  }'

echo -e "\n\n"

# ==================== TEST 2: SQL Analysis ====================
echo -e "${BLUE}TEST 2: SQL Query + Analysis${NC}"
echo "Query: Find all DevAssist errors and categorize them"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Query the app_logs table for all ERROR level logs from the devassist service, group them by exception_type, and tell me which errors are most critical",
    "sessionId": "multi-2"
  }'

echo -e "\n\n"

# ==================== TEST 3: FileSystem + SQL ====================
echo -e "${BLUE}TEST 3: FileSystem + SQL (Reverse Order)${NC}"
echo "Query: List tool files, then check if there are errors for each tool"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "List all Java files in src/main/java/com/devassist/tools, then for each tool file, check if there are any ERROR logs in the app_logs table related to that tool",
    "sessionId": "multi-3"
  }'

echo -e "\n\n"

# ==================== TEST 4: SQL + HTTP ====================
echo -e "${BLUE}TEST 4: SQL + HTTP Tool Chaining${NC}"
echo "Query: Find HTTP errors, then check if the URL is reachable"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find ERROR logs related to HttpTool in app_logs, extract the URL from the context, and check if that URL is currently reachable",
    "sessionId": "multi-4"
  }'

echo -e "\n\n"

# ==================== TEST 5: All 3 Tools ====================
echo -e "${BLUE}TEST 5: SQL + FileSystem + HTTP (All Tools!)${NC}"
echo "Query: Complex multi-tool orchestration"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "1) Query app_logs for all devassist service errors, 2) Read the FileSystemTool.java file, 3) Check if https://www.google.com is reachable, then provide a comprehensive report",
    "sessionId": "multi-5"
  }'

echo -e "\n\n"

# ==================== TEST 6: Performance Analysis ====================
echo -e "${BLUE}TEST 6: Performance Analysis with Multi-Tool${NC}"
echo "Query: Find slow queries and analyze the code"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find WARN logs about slow SQL queries in app_logs, then read the SqlTool.java file and suggest optimizations",
    "sessionId": "multi-6"
  }'

echo -e "\n\n"

# ==================== TEST 7: Error Correlation ====================
echo -e "${BLUE}TEST 7: Error Correlation Across Tools${NC}"
echo "Query: Find related errors across different tools"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Query app_logs for all ERROR logs from devassist service in the last hour, group them by tool (SqlTool, FileSystemTool, HttpTool), and identify if there are any patterns or correlations",
    "sessionId": "multi-7"
  }'

echo -e "\n\n"

# ==================== TEST 8: Code Review with Context ====================
echo -e "${BLUE}TEST 8: Code Review with Error Context${NC}"
echo "Query: Review code based on actual errors"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Find the most recent ERROR in app_logs for FileSystemTool, read the FileSystemTool.java file, and explain what might be causing that specific error",
    "sessionId": "multi-8"
  }'

echo -e "\n\n"

# ==================== TEST 9: Metrics and Monitoring ====================
echo -e "${BLUE}TEST 9: Metrics Analysis${NC}"
echo "Query: Analyze LogAdvisor metrics"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Query app_logs for LogAdvisor INFO logs, extract the metrics (duration, tokens, cost), and calculate the average cost per request",
    "sessionId": "multi-9"
  }'

echo -e "\n\n"

# ==================== TEST 10: Comprehensive Health Check ====================
echo -e "${BLUE}TEST 10: Comprehensive System Health Check${NC}"
echo "Query: Full system analysis using all tools"
echo ""

curl -X POST "${BASE_URL}/chat/multi-tool" \
  -H 'Content-Type: application/json' \
  -d '{
    "task": "Perform a comprehensive health check: 1) Query app_logs for all ERROR and WARN logs from devassist service, 2) List all tool files to verify they exist, 3) Check if https://api.github.com is reachable, then provide a system health report with recommendations",
    "sessionId": "multi-10"
  }'

echo -e "\n\n"

echo -e "${GREEN}=========================================="
echo "All multi-tool tests completed!"
echo -e "==========================================${NC}"
echo ""
echo -e "${YELLOW}Key Learnings:${NC}"
echo "✅ AI autonomously decides which tools to use"
echo "✅ AI chains tools in the correct order"
echo "✅ AI combines results from multiple tools"
echo "✅ AI provides comprehensive analysis"

