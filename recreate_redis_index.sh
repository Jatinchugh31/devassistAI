#!/usr/bin/env bash
set -euo pipefail

# container name
REDIS_CONTAINER=${REDIS_CONTAINER:-redis-trip-service}
INDEX_NAME=${INDEX_NAME:-devassist-index}
PREFIX=${PREFIX:-"devassist:embedding:"}

echo "Checking Redis modules..."
docker exec -i "$REDIS_CONTAINER" redis-cli --raw MODULE LIST || true

echo "Dropping index (if exists) ..."
# FT.DROPINDEX <index> DD  - deletes index and underlying documents if you want; omit DD to keep docs
docker exec -i "$REDIS_CONTAINER" redis-cli --raw <<'REDIS'
FT.DROPINDEX devassist-index DD
REDIS || true

echo "Creating index (FLAT vector) ..."
# Use a single heredoc so redis-cli gets the whole command together.
# Note: quote JSON paths and prefix, and use TAG SEPARATOR where appropriate.
docker exec -i "$REDIS_CONTAINER" redis-cli --raw <<'REDIS'
FT.CREATE ${INDEX_NAME} ON JSON PREFIX 1 "${PREFIX}" SCHEMA \
  "$.content" AS content TEXT WEIGHT 1.0 \
  "$.embedding" AS embedding VECTOR FLAT 6 TYPE FLOAT32 DIM 1536 DISTANCE_METRIC COSINE \
  "$.filePath" AS filePath TAG SEPARATOR "," \
  "$.className" AS className TAG SEPARATOR "," \
  "$.methodName" AS methodName TAG SEPARATOR "," \
  "$.codeType" AS codeType TAG SEPARATOR "," \
  "$.roles" AS roles TAG SEPARATOR "," \
  "$.tags" AS tags TAG SEPARATOR "," \
  "$.isController" AS isController TAG SEPARATOR "," \
  "$.isService" AS isService TAG SEPARATOR "," \
  "$.isRepository" AS isRepository TAG SEPARATOR "," \
  "$.project" AS project TAG SEPARATOR ","
REDIS

echo "Done creating index. Verify with FT.INFO ..."
docker exec -i "$REDIS_CONTAINER" redis-cli --raw "FT.INFO ${INDEX_NAME}" || true

echo "List tag values for tags:"
docker exec -i "$REDIS_CONTAINER" redis-cli --raw "FT.TAGVALS ${INDEX_NAME} tags" || true

echo "✅ Finished"
