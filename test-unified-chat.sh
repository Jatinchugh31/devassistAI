#!/bin/bash

# Unified Chat Test Script
# Tests the single endpoint that handles RAG, Tools, and Hybrid requests

echo "🚀 Testing Unified Chat System"
echo "=============================="
echo ""

BASE_URL="http://localhost:9090/ai-test/unified"

# Test 1: RAG-only question (code analysis)
echo "📚 Test 1: RAG-only question (What is SqlTool?)"
echo "-----------------------------------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What is SqlTool?",
    "sessionId": "unified-test-1"
  }' | jq '.'
echo ""
echo ""

# Test 2: Tools-only question (SQL execution)
echo "🔧 Test 2: Tools-only question (SQL execution)"
echo "-----------------------------------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Execute SQL: SELECT * FROM app_logs LIMIT 3",
    "sessionId": "unified-test-2"
  }' | jq '.'
echo ""
echo ""

# Test 3: File System Tool
echo "📁 Test 3: File System Tool (List Java files)"
echo "----------------------------------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "List all Java files in the project",
    "sessionId": "unified-test-3"
  }' | jq '.'
echo ""
echo ""

# Test 4: Hybrid question (RAG + Tools)
echo "🔄 Test 4: Hybrid question (RAG + Tools)"
echo "----------------------------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "What is SqlTool and show me its code?",
    "sessionId": "unified-test-4"
  }' | jq '.'
echo ""
echo ""

# Test 5: General chat
echo "💬 Test 5: General chat"
echo "-----------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Hello, how are you?",
    "sessionId": "unified-test-5"
  }' | jq '.'
echo ""
echo ""

# Test 6: Complex hybrid question
echo "🎯 Test 6: Complex hybrid question"
echo "----------------------------------"
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "task": "How many Java files are in this project and show me the SqlTool code?",
    "sessionId": "unified-test-6"
  }' | jq '.'
echo ""
echo ""

# Test 7: Get system statistics
echo "📊 Test 7: System statistics"
echo "---------------------------"
curl -X GET "$BASE_URL/stats" | jq '.'
echo ""
echo ""

echo "✅ Unified Chat System Test Complete!"
echo ""
echo "🎯 Key Benefits:"
echo "- Single endpoint: /ai-test/unified/chat"
echo "- Intelligent routing between RAG, Tools, and Hybrid"
echo "- No need to know which endpoint to use"
echo "- Seamless combination of code analysis and tool execution"
echo ""
echo "📝 Usage Examples:"
echo "- 'What is SqlTool?' → RAG analysis"
echo "- 'Execute SQL: SELECT * FROM app_logs' → SQL Tool"
echo "- 'What is SqlTool and show me its code?' → Hybrid (RAG + Tools)"
echo "- 'Hello, how are you?' → General chat"
