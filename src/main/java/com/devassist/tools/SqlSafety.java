package com.devassist.tools;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlSafety - lightweight SQL validation / sanitization helper for safe read-only execution.
 *
 * - Ensures the SQL is SELECT/CTE/EXPLAIN SELECT only (no INSERT/UPDATE/DELETE/DDL).
 * - Rejects multi-statement queries (no semicolons).
 * - Provides dialect-aware row limiting (LIMIT for most dialects, FETCH/FIRST for SQLServer/Oracle if configured).
 *
 * Note: This is NOT a full SQL parser. It uses heuristics that work for common cases.
 * If you need absolute safety, run queries against a dedicated read-only user/role or use DB-level guards.
 */
@Component
public class SqlSafety {

    public enum SqlDialect {
        DEFAULT,    // uses "LIMIT n" (Postgres/MySQL/H2)
        POSTGRES,
        MYSQL,
        SQLSERVER,
        ORACLE
    }

    // detect forbidden statements (DDL/DML/exec/etc)
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|merge|call|exec|replace)\\b",
            Pattern.CASE_INSENSITIVE);

    // simple check for semicolon (multi-statement)
    private static final Pattern SEMI_PATTERN = Pattern.compile(";");

    // leading comments removal (block and line)
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern LINE_COMMENT = Pattern.compile("--.*?(\\r?\\n|$)");

    private final SqlDialect dialect;

    public SqlSafety() {
        this(SqlDialect.DEFAULT);
    }

    public SqlSafety(SqlDialect dialect) {
        this.dialect = dialect == null ? SqlDialect.DEFAULT : dialect;
    }

    /**
     * Throws IllegalArgumentException if the SQL is not safe (not read-only SELECT or contains forbidden constructs).
     */
    public void ensureSelectOnly(String sql) {
        String cleaned = normalizeAndStripComments(sql);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("SQL is empty after stripping comments");
        }

        // no semicolons allowed (prevent multi-statement)
        Matcher semi = SEMI_PATTERN.matcher(cleaned);
        if (semi.find()) {
            throw new IllegalArgumentException("Multiple statements or semicolon found. Only single SELECT queries are allowed.");
        }

        // disallow dangerous keywords
        Matcher forbidden = FORBIDDEN_PATTERN.matcher(cleaned);
        if (forbidden.find()) {
            String found = forbidden.group(1);
            throw new IllegalArgumentException("Forbidden SQL keyword detected: " + found);
        }

        // allow: SELECT ..., WITH ... , EXPLAIN SELECT ...
        String leading = leadingToken(cleaned).toLowerCase(Locale.ROOT);

        if ("select".equals(leading) || "with".equals(leading) || "explain".equals(leading)) {
            // if explain, ensure next token is select or with/select
            if ("explain".equals(leading)) {
                String after = secondToken(cleaned);
                if (after == null || !(after.equalsIgnoreCase("select") || after.equalsIgnoreCase("with"))) {
                    throw new IllegalArgumentException("EXPLAIN must be followed by SELECT or WITH SELECT");
                }
            }
            // allowed
            return;
        }

        throw new IllegalArgumentException("Only SELECT/WITH/EXPLAIN SELECT queries are allowed.");
    }

    /**
     * Returns true if the SQL appears to be a safe read-only SELECT (does not throw).
     */
    public boolean isSelectOnly(String sql) {
        try {
            ensureSelectOnly(sql);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Apply a row-limit wrapper according to dialect.
     *
     * - If query already contains a LIMIT/FETCH/OFFSET clause (simple heuristic), returns as-is.
     * - Otherwise appends a dialect-specific LIMIT to avoid huge results.
     *
     * Note: This does not attempt to rewrite complex queries; it's a best-effort helper.
     */
    public String applyRowLimit(String sql, int maxRows) {
        if (sql == null) return sql;
        String cleaned = sql.trim();

        // quick guard: if semicolon present, remove trailing ones (we already disallow multi-statement in ensureSelectOnly)
        cleaned = cleaned.replaceAll("\\s*;\\s*$", "");

        // heuristic: if contains "limit" or "fetch" already, leave it
        String lower = cleaned.toLowerCase(Locale.ROOT);
        if (lower.matches("(?s).*\\blimit\\b.*") || lower.matches("(?s).*\\bfetch\\b.*")) {
            return cleaned;
        }

        switch (dialect) {
            case SQLSERVER:
                // SQL Server: use FETCH FIRST ... ROWS ONLY (requires ORDER BY in many cases; we'll add OFFSET 0 to be safe)
                return cleaned + " OFFSET 0 ROWS FETCH NEXT " + maxRows + " ROWS ONLY";
            case ORACLE:
                // Oracle 12c+: FETCH FIRST n ROWS ONLY
                return cleaned + " FETCH FIRST " + maxRows + " ROWS ONLY";
            case MYSQL:
            case POSTGRES:
            case DEFAULT:
            default:
                return cleaned + " LIMIT " + maxRows;
        }
    }

    // ---- helpers ----

    private String normalizeAndStripComments(String sql) {
        if (sql == null) return "";
        // remove block comments
        String withoutBlock = BLOCK_COMMENT.matcher(sql).replaceAll(" ");
        // remove line comments
        String withoutLine = LINE_COMMENT.matcher(withoutBlock).replaceAll(" ");
        // collapse whitespace
        String normalized = withoutLine.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    /**
     * Returns the first token (word) from the cleaned SQL or empty string.
     */
    private String leadingToken(String cleanedSql) {
        if (cleanedSql == null || cleanedSql.isBlank()) return "";
        String s = cleanedSql.trim();
        int space = s.indexOf(' ');
        if (space < 0) return s;
        return s.substring(0, space);
    }

    /**
     * Returns the second token (word) from the cleaned SQL, or null.
     */
    private String secondToken(String cleanedSql) {
        if (cleanedSql == null || cleanedSql.isBlank()) return null;
        String s = cleanedSql.trim();
        String[] parts = s.split("\\s+");
        if (parts.length < 2) return null;
        return parts[1];
    }
}
