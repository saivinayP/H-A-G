package com.hag.db.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads SQL scripts from {@code .sql} files and resolves {@code ${VAR}} tokens.
 *
 * <h3>Supported source formats</h3>
 * <ol>
 *   <li><b>File path</b> — {@code Recipient} references a {@code .sql} file
 *       under the sql-scripts root (e.g. {@code queries/get_user.sql})</li>
 *   <li><b>Inline SQL</b> — the sub-case {@code :INLINE} signals the SQL
 *       is written directly in the {@code Key} column</li>
 * </ol>
 *
 * <h3>Variable substitution</h3>
 * <pre>
 * SELECT * FROM users WHERE email = '${email}' AND status = '${GLOBAL:status}'
 * </pre>
 */
public final class SqlLoader {

    private static final Pattern VAR_PAT = Pattern.compile("\\$\\{([^}]+)}");

    private SqlLoader() {}

    /**
     * Loads a {@code .sql} file from disk and substitutes {@code ${VAR}} tokens.
     *
     * @param scriptPath  path to the SQL file (relative to sqlScriptsRoot)
     * @param sqlRoot     root directory for SQL scripts
     * @param variables   variable map for substitution
     * @return the resolved SQL string ready for JDBC execution
     */
    public static String loadAndResolve(
            String scriptPath,
            String sqlRoot,
            java.util.Map<String, Object> variables
    ) {
        Path absPath = Path.of(sqlRoot).resolve(scriptPath).normalize();
        String raw;
        try {
            raw = Files.readString(absPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read SQL script: " + absPath, e);
        }
        return substituteVariables(raw, variables);
    }

    /**
     * Resolves {@code ${VAR}} tokens in an inline SQL string.
     *
     * @param sql       raw SQL, possibly containing {@code ${VAR}} tokens
     * @param variables variable map
     * @return resolved SQL string
     */
    public static String resolveInline(String sql, java.util.Map<String, Object> variables) {
        if (sql == null) return "";
        if (variables == null || variables.isEmpty()) return sql;
        return substituteVariables(sql, variables);
    }

    // ── Internal ─────────────────────────────────────────────────────────

    static String substituteVariables(String sql, java.util.Map<String, Object> vars) {
        Matcher m = VAR_PAT.matcher(sql);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String token = m.group(1);
            Object value = resolveToken(token, vars);
            m.appendReplacement(sb, value == null ? "" : Matcher.quoteReplacement(value.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static Object resolveToken(String token, java.util.Map<String, Object> vars) {
        if (vars.containsKey(token)) return vars.get(token);
        // Strip scope prefix: "GLOBAL:email" → "email"
        int colon = token.indexOf(':');
        if (colon >= 0) {
            String bare = token.substring(colon + 1);
            if (vars.containsKey(bare)) return vars.get(bare);
        }
        return null;
    }
}
