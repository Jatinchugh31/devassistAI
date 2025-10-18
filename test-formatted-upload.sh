#!/bin/bash

# Test file upload with formatted response

BASE_URL="http://localhost:9090/ai-test"

echo "=========================================="
echo "File Upload Test - Formatted Response"
echo "=========================================="
echo ""

# Create a test log file
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
echo ""

echo "📤 Uploading file to AI for analysis..."
echo ""

# Upload and analyze (with pretty JSON output)
curl -X POST "${BASE_URL}/upload" \
  -F "file=@test-error.log" \
  -F "task=Analyze this log file and identify the main issues" \
  | python3 -m json.tool

echo ""
echo ""

# Cleanup
rm -f test-error.log
echo "✅ Cleanup complete"

