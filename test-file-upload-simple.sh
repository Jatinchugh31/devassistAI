#!/bin/bash

# Simple file upload test script

BASE_URL="http://localhost:9090/ai-test"

echo "=========================================="
echo "File Upload Test - Quick Start"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ==================== Create Test Files ====================
echo -e "${BLUE}Creating test files...${NC}"

# Test 1: Simple log file
cat > test-error.log << 'EOF'
2025-10-18 10:15:23 ERROR [UserService] Database connection failed: Connection timeout
2025-10-18 10:15:24 ERROR [UserService] Retry attempt 1 failed
2025-10-18 10:15:25 ERROR [UserService] Retry attempt 2 failed
2025-10-18 10:15:30 ERROR [OrderService] Null pointer exception at line 45
2025-10-18 10:15:31 WARN  [MemoryMonitor] High memory usage: 85%
2025-10-18 10:15:35 ERROR [PaymentService] Payment gateway timeout
2025-10-18 10:15:40 INFO  [UserService] Database connection restored
EOF

echo "✅ Created test-error.log"

# Test 2: Simple text file
cat > test-document.txt << 'EOF'
Project Requirements:
1. Implement user authentication
2. Add payment gateway integration
3. Create admin dashboard
4. Setup email notifications
5. Implement logging system

Priority: HIGH
Deadline: End of month
EOF

echo "✅ Created test-document.txt"

# Test 3: Code file
cat > test-code.java << 'EOF'
public class UserService {
    private Database db;
    
    public User getUser(Long id) {
        // TODO: Add null check
        return db.findById(id);
    }
    
    public void saveUser(User user) {
        // TODO: Add validation
        db.save(user);
    }
}
EOF

echo "✅ Created test-code.java"
echo ""

# ==================== TEST 1: Upload Log File ====================
echo -e "${BLUE}TEST 1: Upload Log File${NC}"
echo "File: test-error.log"
echo "Task: Analyze errors"
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@test-error.log" \
  -F "task=Analyze this log file and identify the main issues"

echo -e "\n\n"

# ==================== TEST 2: Upload Document ====================
echo -e "${BLUE}TEST 2: Upload Document${NC}"
echo "File: test-document.txt"
echo "Task: Summarize requirements"
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@test-document.txt" \
  -F "task=Summarize the key requirements and identify the priority items"

echo -e "\n\n"

# ==================== TEST 3: Upload Code File ====================
echo -e "${BLUE}TEST 3: Upload Code File${NC}"
echo "File: test-code.java"
echo "Task: Code review"
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@test-code.java" \
  -F "task=Review this code and suggest improvements"

echo -e "\n\n"

# ==================== TEST 4: Upload Actual Project File ====================
echo -e "${BLUE}TEST 4: Upload Actual Project File${NC}"
echo "File: SqlTool.java from project"
echo "Task: Code analysis"
echo ""

curl -X POST "${BASE_URL}/upload" \
  -F "file=@src/main/java/com/devassist/tools/SqlTool.java" \
  -F "task=Analyze this code and explain what it does"

echo -e "\n\n"

# ==================== TEST 5: Upload Multiple Files ====================
echo -e "${BLUE}TEST 5: Upload Multiple Files${NC}"
echo "Files: test-error.log, test-document.txt, test-code.java"
echo "Task: Compare and analyze"
echo ""

curl -X POST "${BASE_URL}/upload/multiple" \
  -F "files=@test-error.log" \
  -F "files=@test-document.txt" \
  -F "files=@test-code.java" \
  -F "task=Analyze all these files and provide a summary"

echo -e "\n\n"

# ==================== Cleanup ====================
echo -e "${YELLOW}Cleaning up test files...${NC}"
rm -f test-error.log test-document.txt test-code.java
echo "✅ Cleanup complete"
echo ""

echo -e "${GREEN}=========================================="
echo "All file upload tests completed!"
echo -e "==========================================${NC}"

