#!/bin/bash

# Test script for file upload feature

BASE_URL="http://localhost:9090/ai-test"

echo "=========================================="
echo "File Upload Feature - Test Suite"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

# ==================== TEST 1: Create Test Log File ====================
echo -e "${BLUE}Creating test log file...${NC}"
cat > test-error.log << 'EOF'
2025-10-18 10:15:23 ERROR [UserService] Database connection failed: Connection timeout
2025-10-18 10:15:24 ERROR [UserService] Retry attempt 1 failed
2025-10-18 10:15:25 ERROR [UserService] Retry attempt 2 failed
2025-10-18 10:15:30 ERROR [OrderService] Null pointer exception at line 45
2025-10-18 10:15:31 WARN  [MemoryMonitor] High memory usage: 85%
2025-10-18 10:15:35 ERROR [PaymentService] Payment gateway timeout
2025-10-18 10:15:40 INFO  [UserService] Database connection restored
2025-10-18 10:15:45 WARN  [CacheService] Cache size exceeding threshold
EOF

echo "✅ Created test-error.log"
echo ""

# ==================== TEST 2: Upload Single File ====================
echo -e "${BLUE}TEST 1: Upload Single Log File${NC}"
echo "Uploading test-error.log..."
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@test-error.log" \
  -F "task=Analyze this log file and identify the main issues. List them in order of severity." \
  -F "sessionId=test-upload-1"

echo -e "\n\n"

# ==================== TEST 3: Upload Java File ====================
echo -e "${BLUE}TEST 2: Upload Java File${NC}"
echo "Uploading SqlTool.java..."
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@src/main/java/com/devassist/tools/SqlTool.java" \
  -F "task=Review this code and suggest improvements" \
  -F "sessionId=test-upload-2"

echo -e "\n\n"

# ==================== TEST 4: Create Multiple Test Files ====================
echo -e "${BLUE}Creating multiple test files...${NC}"

cat > test-file1.log << 'EOF'
2025-10-18 ERROR: Connection timeout in service A
2025-10-18 ERROR: Retry failed in service A
EOF

cat > test-file2.log << 'EOF'
2025-10-18 ERROR: Connection timeout in service B
2025-10-18 WARN: High latency detected
EOF

cat > test-file3.log << 'EOF'
2025-10-18 ERROR: Database unavailable
2025-10-18 ERROR: Query timeout
EOF

echo "✅ Created test-file1.log, test-file2.log, test-file3.log"
echo ""

# ==================== TEST 5: Upload Multiple Files ====================
echo -e "${BLUE}TEST 3: Upload Multiple Files${NC}"
echo "Uploading 3 log files..."
echo ""

curl -X POST "${BASE_URL}/upload/multiple" \
  -F "files=@test-file1.log" \
  -F "files=@test-file2.log" \
  -F "files=@test-file3.log" \
  -F "task=Compare these log files and find common patterns" \
  -F "sessionId=test-upload-3"

echo -e "\n\n"

# ==================== TEST 6: Upload Properties File ====================
echo -e "${BLUE}TEST 4: Upload Configuration File${NC}"
echo "Uploading application.properties..."
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@src/main/resources/application.properties" \
  -F "task=Review this configuration and check for any issues or improvements" \
  -F "sessionId=test-upload-4"

echo -e "\n\n"

# ==================== Cleanup ====================
echo -e "${GREEN}Cleaning up test files...${NC}"
rm -f test-error.log test-file1.log test-file2.log test-file3.log
echo "✅ Cleanup complete"
echo ""

echo -e "${GREEN}=========================================="
echo "All tests completed!"
echo -e "==========================================${NC}"

