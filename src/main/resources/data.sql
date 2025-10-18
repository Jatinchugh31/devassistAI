-- data.sql
-- INFO row
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 18:45:12', 'INFO', 'auth-service', 'auth-1', 'http-nio-8080-exec-1', 'req-1001', 'alice',
           'com.example.auth.LoginController', NULL,
           'User login succeeded for user=alice',
           NULL,
           '{"endpoint":"/api/login","method":"POST","httpStatus":200}',
           'login,auth'
       );

-- ERROR: NPE in order-service (multiline stack trace assembled using CHAR(10))
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 18:52:03', 'ERROR', 'order-service', 'order-2', 'http-nio-8080-exec-14', 'req-1043', 'bob',
           'com.example.order.OrderProcessor', 'java.lang.NullPointerException',
           'Failed to process order: null reference encountered',
           'java.lang.NullPointerException: Cannot read field "items" because "order" is null'
               || CHAR(10) || '\tat com.example.order.OrderProcessor.process(OrderProcessor.java:87)'
               || CHAR(10) || '\tat com.example.order.OrderController.placeOrder(OrderController.java:45)'
               || CHAR(10) || '\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)'
               || CHAR(10) || '\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)',
           '{"orderId":null,"attempt":1,"payloadSize":512}',
           'order,critical'
       );

-- ERROR: DB connection timeout in catalog-service
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 18:55:45', 'ERROR', 'catalog-service', 'catalog-1', 'pool-3-thread-7', 'req-1088', NULL,
           'com.example.catalog.CatalogLoader', 'org.postgresql.util.PSQLException',
           'Failed to load product catalog from DB',
           'org.postgresql.util.PSQLException: The connection attempt timed out.'
               || CHAR(10) || '\tat org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:259)'
               || CHAR(10) || '\tat org.postgresql.core.ConnectionFactory.openConnection(ConnectionFactory.java:49)'
               || CHAR(10) || '\tat org.postgresql.jdbc.PgConnection.<init>(PgConnection.java:223)'
               || CHAR(10) || '\tat com.zaxxer.hikari.pool.PoolBase.newConnection(PoolBase.java:354)'
               || CHAR(10) || '\tat com.zaxxer.hikari.pool.HikariPool.createPoolEntry(HikariPool.java:476)'
               || CHAR(10) || 'Caused by: java.net.SocketTimeoutException: connect timed out',
           '{"db":"products-db","queryTimeoutMs":5000}',
           'database,timeout'
       );

-- WARN row
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 19:02:09', 'WARN', 'search-service', 'search-1', 'http-nio-8080-exec-9', 'req-1110', NULL,
           'com.example.search.SearchIndex', NULL,
           'Search index refresh took 12.6s (threshold 10s)',
           NULL,
           '{"index":"products_v2","durationMs":12600}',
           'performance,search'
       );

-- ERROR: OOM in stream-consumer
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 19:10:21', 'ERROR', 'stream-consumer', 'stream-3', 'kafka-consumer-1', 'req-1150', NULL,
           'com.example.stream.KafkaHandler', 'java.lang.OutOfMemoryError',
           'Worker crashed processing batch 42: java.lang.OutOfMemoryError: Java heap space',
           'java.lang.OutOfMemoryError: Java heap space'
               || CHAR(10) || '\tat java.base/java.util.HashMap.resize(HashMap.java:703)'
               || CHAR(10) || '\tat java.base/java.util.HashMap.putVal(HashMap.java:662)'
               || CHAR(10) || '\tat java.base/java.util.HashMap.put(HashMap.java:607)'
               || CHAR(10) || '\tat com.example.stream.BatchProcessor.aggregate(BatchProcessor.java:152)'
               || CHAR(10) || '\tat com.example.stream.KafkaHandler.onMessage(KafkaHandler.java:78)',
           '{"batchId":42,"approxRecords":120000,"heapUsedMB":2480}',
           'oom,kafka,stream'
       );

-- ERROR: startup config missing property (note doubled single quotes around property name)
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 19:13:02', 'ERROR', 'config-service', 'config-1', 'main', 'req-1184', NULL,
           'com.example.config.ConfigLoader', 'java.lang.IllegalStateException',
           'Failed to start application: missing required property ''payment.gateway.url''',
           'java.lang.IllegalStateException: Required property missing: payment.gateway.url'
               || CHAR(10) || '\tat com.example.config.ConfigLoader.validate(ConfigLoader.java:58)'
               || CHAR(10) || '\tat com.example.config.ConfigLoader.init(ConfigLoader.java:34)'
               || CHAR(10) || '\tat com.example.Application.main(Application.java:18)',
           '{"env":"prod","profile":"default"}',
           'startup,config'
       );

-- INFO billing
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-14 19:20:30', 'INFO', 'billing-service', 'billing-1', 'http-nio-8080-exec-3', 'req-1201', 'carol',
           'com.example.billing.BillingController', NULL,
           'Invoice generated for order=ORD-99876',
           NULL,
           '{"orderId":"ORD-99876","amount":"79.99","currency":"USD"}',
           'billing,info'
       );

-- ERROR: SQL Tool - Invalid query
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:15:45', 'ERROR', 'devassist', 'localhost', 'http-nio-9090-exec-5', 'req-2001', 'developer',
           'com.devassist.tools.SqlTool', 'java.sql.SQLException',
           'SQL execution failed: Invalid column name in query',
           'java.sql.SQLException: Column "invalid_column" not found'
               || CHAR(10) || '\tat com.devassist.tools.SqlTool.executeSql(SqlTool.java:65)'
               || CHAR(10) || '\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)',
           '{"query":"SELECT invalid_column FROM app_logs","maxRows":50}',
           'sql,error'
       );

-- ERROR: FileSystem Tool - File not found
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:20:12', 'ERROR', 'devassist', 'localhost', 'http-nio-9090-exec-7', 'req-2002', 'developer',
           'com.devassist.tools.FileSystemTool', 'java.io.IOException',
           'Error reading file: File does not exist',
           'java.io.IOException: File not found: /invalid/path/file.txt'
               || CHAR(10) || '\tat com.devassist.tools.FileSystemTool.readFile(FileSystemTool.java:78)'
               || CHAR(10) || '\tat com.devassist.tools.FileSystemTool.validateAndResolvePath(FileSystemTool.java:310)',
           '{"filePath":"/invalid/path/file.txt","operation":"read"}',
           'filesystem,error'
       );

-- WARN: High memory usage in DevAssist
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:25:33', 'WARN', 'devassist', 'localhost', 'memory-monitor', 'req-2003', NULL,
           'com.devassist.monitor.MemoryMonitor', NULL,
           'High memory usage detected: 82% of heap used',
           NULL,
           '{"heapUsedMB":1640,"heapMaxMB":2000,"gcCount":45}',
           'performance,memory'
       );

-- ERROR: HTTP Tool - Connection timeout
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:30:55', 'ERROR', 'devassist', 'localhost', 'http-nio-9090-exec-9', 'req-2004', 'developer',
           'com.devassist.tools.HttpTool', 'java.net.SocketTimeoutException',
           'HTTP GET request failed: Connection timeout',
           'java.net.SocketTimeoutException: Read timed out'
               || CHAR(10) || '\tat java.base/java.net.SocketInputStream.socketRead0(Native Method)'
               || CHAR(10) || '\tat com.devassist.tools.HttpTool.httpGet(HttpTool.java:92)',
           '{"url":"https://api.example.com/data","timeoutMs":10000}',
           'http,timeout'
       );

-- ERROR: AI Model - Rate limit exceeded
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:35:20', 'ERROR', 'devassist', 'localhost', 'http-nio-9090-exec-11', 'req-2005', 'developer',
           'com.devassist.service.ChatService', 'org.springframework.ai.retry.NonTransientAiException',
           'AI request failed: Rate limit exceeded',
           'org.springframework.ai.retry.NonTransientAiException: HTTP 429 - Rate limit exceeded'
               || CHAR(10) || '\tat com.devassist.service.ChatService.sendMessage(ChatService.java:45)'
               || CHAR(10) || '\tat com.devassist.controller.ChatController.chat(ChatController.java:52)',
           '{"model":"gpt-4o-mini","tokensUsed":1500,"requestsPerMinute":60}',
           'ai,ratelimit'
       );

-- INFO: Successful multi-tool execution
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:40:15', 'INFO', 'devassist', 'localhost', 'http-nio-9090-exec-13', 'req-2006', 'developer',
           'com.devassist.service.ChatService', NULL,
           'Multi-tool request completed successfully: sql_execute + read_file',
           NULL,
           '{"tools":["sql_execute","read_file"],"duration":2345,"tokensUsed":450}',
           'multi-tool,success'
       );

-- ERROR: Redis connection failed
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:45:30', 'ERROR', 'devassist', 'localhost', 'lettuce-nioEventLoop-4-1', 'req-2007', NULL,
           'com.devassist.repository.RedisChatMemoryRepository', 'io.lettuce.core.RedisConnectionException',
           'Failed to connect to Redis: Connection refused',
           'io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379'
               || CHAR(10) || '\tat io.lettuce.core.RedisConnectionException.create(RedisConnectionException.java:78)'
               || CHAR(10) || '\tat com.devassist.repository.RedisChatMemoryRepository.add(RedisChatMemoryRepository.java:34)',
           '{"host":"localhost","port":6379,"timeout":2000}',
           'redis,connection'
       );

-- WARN: Slow SQL query
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:50:45', 'WARN', 'devassist', 'localhost', 'http-nio-9090-exec-15', 'req-2008', 'developer',
           'com.devassist.tools.SqlTool', NULL,
           'SQL query took longer than expected: 3456ms',
           NULL,
           '{"query":"SELECT * FROM app_logs WHERE level = ''ERROR''","executionTimeMs":3456,"rowsReturned":25}',
           'sql,performance'
       );

-- ERROR: File upload size exceeded
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 10:55:10', 'ERROR', 'devassist', 'localhost', 'http-nio-9090-exec-17', 'req-2009', 'developer',
           'com.devassist.service.FileUploadService', 'java.lang.IllegalArgumentException',
           'File upload rejected: File too large',
           'java.lang.IllegalArgumentException: File too large: 15728640 bytes (max: 10485760 bytes)'
               || CHAR(10) || '\tat com.devassist.service.FileUploadService.validateFile(FileUploadService.java:95)'
               || CHAR(10) || '\tat com.devassist.controller.FileUploadController.uploadAndAnalyze(FileUploadController.java:45)',
           '{"fileName":"large-log.txt","fileSize":15728640,"maxSize":10485760}',
           'upload,validation'
       );

-- INFO: LogAdvisor tracking
INSERT INTO app_logs (created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, tags)
VALUES (
           '2025-10-18 11:00:25', 'INFO', 'devassist', 'localhost', 'http-nio-9090-exec-19', 'req-2010', 'developer',
           'com.devassist.advisor.LogAdvisor', NULL,
           'AI request completed: duration=1234ms, tokens=350, cost=$0.0105',
           NULL,
           '{"duration":1234,"promptTokens":200,"completionTokens":150,"totalTokens":350,"cost":0.0105}',
           'advisor,metrics'
       );
