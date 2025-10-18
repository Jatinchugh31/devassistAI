package com.devassist.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes a safe read-only SQL executor to the model via Spring AI function/tool calling.
 *
 * Tool name: "sql_execute"
 * Input params: query (required), maxRows (optional, default 50)
 * Output: { "rowCount": N, "rows": [ {col: val, ...}, ... ], "tookMs": 12 }
 *
 * IMPORTANT: this tool verifies the SQL and enforces READ-ONLY SELECT-only behavior.
 */
@Component
public class SqlTool {

    private final JdbcTemplate jdbc;
    private final SqlSafety sqlSafety;
    private final int defaultMaxRows = 50;

    public SqlTool(JdbcTemplate jdbc, SqlSafety sqlSafety) {
        this.jdbc = jdbc;
        this.sqlSafety = sqlSafety;
    }

    /**
     * Request object for SQL execution
     */
    public record SqlRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The SQL SELECT query to execute. IMPORTANT: When filtering by log level, use uppercase values: 'ERROR', 'WARN', 'INFO' (not lowercase). Example: WHERE level='ERROR'")
        String query,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("Maximum number of rows to return (default: 50)")
        Integer maxRows
    ) {}

    /**
     * Response object for SQL execution
     */
    public record SqlResponse(
        int rowCount,
        List<Map<String, Object>> rows,
        long tookMs
    ) {}

    @Tool(name = "sql_execute", description = "Execute a safe, read-only SQL SELECT query against the database. Only SELECT queries are allowed. Available tables: 'app_logs' (contains application logs with columns: id, created_at, level, service, host, thread, request_id, user_id, logger, exception_type, message, stack_trace, context, resolved, tags). IMPORTANT: The 'level' column uses uppercase values: 'ERROR', 'WARN', 'INFO'. Always use uppercase when filtering by level (e.g., WHERE level='ERROR', not WHERE level='error').")
    public SqlResponse executeSql(SqlRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("SQL query is required");
        }

        String query = request.query();
        int maxRows = request.maxRows() != null ? request.maxRows() : defaultMaxRows;

        // validate
        sqlSafety.ensureSelectOnly(query);

        long start = System.currentTimeMillis();

        // Enforce limit: wrap query in a SELECT ... LIMIT if the DB supports LIMIT
        String limitedQuery = sqlSafety.applyRowLimit(query, maxRows);

        List<Map<String, Object>> rows = jdbc.query(limitedQuery, new ColumnMapRowMapper());

        long took = System.currentTimeMillis() - start;

        return new SqlResponse(rows.size(), rows, took);
    }
}
