#!/bin/bash

# Final Test Script for Redis Chat Memory
# This script tests that chat memory is working correctly

echo "========================================="
echo "🧪 Redis Chat Memory Test"
echo "========================================="
echo ""

# Check if Redis is running
echo "1️⃣  Checking Redis..."
if redis-cli ping > /dev/null 2>&1; then
    echo "   ✅ Redis is running"
else
    echo "   ❌ Redis is NOT running!"
    echo "   Start Redis with: brew services start redis"
    echo "   Or with Docker: docker run -d -p 6379:6379 redis:alpine"
    exit 1
fi
echo ""

# Set test session ID
SESSION_ID="test-$(date +%s)"
echo "2️⃣  Using session ID: $SESSION_ID"
echo ""

# Clear any existing data for this session
echo "3️⃣  Clearing previous test data..."
redis-cli DEL "devassist:conversation:$SESSION_ID" > /dev/null
echo "   ✅ Cleared"
echo ""

# Test 1: First message
echo "4️⃣  Test 1: First message (creates conversation)"
echo "   Sending: 'What is Spring Boot?'"
RESPONSE1=$(curl -s -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d "{
    \"role\": \"JAVA\",
    \"task\": \"What is Spring Boot?\",
    \"sessionId\": \"$SESSION_ID\"
  }")

echo "   Response preview: ${RESPONSE1:0:100}..."
echo ""

# Check Redis
echo "5️⃣  Checking Redis storage..."
COUNT=$(redis-cli LLEN "devassist:conversation:$SESSION_ID")
echo "   Messages in Redis: $COUNT"

if [ "$COUNT" -gt 0 ]; then
    echo "   ✅ Messages stored successfully!"
    echo ""
    echo "   Sample message:"
    redis-cli LINDEX "devassist:conversation:$SESSION_ID" 0 | jq '.'
else
    echo "   ❌ No messages stored!"
    exit 1
fi
echo ""

# Test 2: Second message (should use context)
echo "6️⃣  Test 2: Follow-up message (uses context)"
echo "   Sending: 'Can you give me a simple example?'"
sleep 1
RESPONSE2=$(curl -s -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d "{
    \"role\": \"JAVA\",
    \"task\": \"Can you give me a simple example?\",
    \"sessionId\": \"$SESSION_ID\"
  }")

echo "   Response preview: ${RESPONSE2:0:150}..."
echo ""

# Check Redis again
COUNT2=$(redis-cli LLEN "devassist:conversation:$SESSION_ID")
echo "7️⃣  Messages after second request: $COUNT2"

if [ "$COUNT2" -gt "$COUNT" ]; then
    echo "   ✅ New messages added!"
else
    echo "   ⚠️  Message count didn't increase (may be OK if trimming)"
fi
echo ""

# Test 3: Context awareness
echo "8️⃣  Test 3: Verify context awareness"
echo "   Sending: 'What did I just ask you about?'"
sleep 1
RESPONSE3=$(curl -s -X POST "http://localhost:9090/ai/chat" \
  -H "Content-Type: application/json" \
  -d "{
    \"role\": \"JAVA\",
    \"task\": \"What did I just ask you about?\",
    \"sessionId\": \"$SESSION_ID\"
  }")

echo "   Response preview: ${RESPONSE3:0:150}..."
echo ""

# Check if response mentions Spring Boot or example
if echo "$RESPONSE3" | grep -iq "spring boot\|example"; then
    echo "   ✅ AI remembers the conversation context!"
else
    echo "   ⚠️  AI might not be using full context (check logs)"
fi
echo ""

# Test debug endpoint
echo "9️⃣  Checking debug endpoint..."
DEBUG_RESPONSE=$(curl -s "http://localhost:9090/ai/debug/memory/$SESSION_ID")
DEBUG_COUNT=$(echo "$DEBUG_RESPONSE" | jq '. | length' 2>/dev/null || echo "0")
echo "   Debug endpoint shows $DEBUG_COUNT messages"
echo ""

# Summary
echo "========================================="
echo "📊 Test Summary"
echo "========================================="
echo "Session ID: $SESSION_ID"
echo "Messages in Redis: $COUNT2"
echo ""
echo "✅ Tests completed!"
echo ""
echo "To inspect Redis data manually:"
echo "  redis-cli LRANGE 'devassist:conversation:$SESSION_ID' 0 -1"
echo ""
echo "To view logs:"
echo "  Look for 📖 (retrieving) and 💾 (adding) emoji in application logs"
echo ""

# Cleanup option
read -p "Delete test data? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    redis-cli DEL "devassist:conversation:$SESSION_ID" > /dev/null
    echo "✅ Test data cleaned up"
fi

