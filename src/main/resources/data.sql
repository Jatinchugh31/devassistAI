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
