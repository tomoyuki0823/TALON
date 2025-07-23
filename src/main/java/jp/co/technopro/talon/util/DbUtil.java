
package jp.co.technopro.talon.util;

import jp.co.technopro.talon.db.DbConfig;
import jp.co.technopro.talon.db.DbConfigLoader;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DbUtil {

    public enum Dialect {
        SQLSERVER, POSTGRES, ORACLE, MYSQL
    }

    public static Connection getConnectionIfNull(Connection conn) throws Exception {
        if (conn != null) return conn;
        return DbConfigLoader.load().getConnection();
    }

    public static List<Map<String, Object>> select(String sql, Object... params) throws Exception {
        try (Connection conn = getConnectionIfNull(null)) {
            return select(conn, sql, params);
        }
    }

    public static List<Map<String, Object>> select(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    public static Map<String, Object> selectOneRowAsMap(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? toMap(rs) : null;
            }
        }
    }

    public static List<Map<String, Object>> selectListAsMap(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                while (rs.next()) {
                    resultList.add(toMap(rs));
                }
                return resultList;
            }
        }
    }

    public static int delete(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    public static int update(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    public static int insertByMap(Connection conn, String table, Map<String, Object> data, List<String> columns, Dialect dialect) throws SQLException {
        String sql = buildInsertSQL(table, columns, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, columns.stream().map(data::get).toArray());
            return ps.executeUpdate();
        }
    }

    public static int updateByMap(Connection conn, String table, Map<String, Object> data, List<String> columns, List<String> whereKeys, Dialect dialect) throws SQLException {
        String sql = buildUpdateSQL(table, columns, whereKeys, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<Object> values = new ArrayList<>();
            for (String col : columns) values.add(data.get(col));
            for (String key : whereKeys) values.add(data.get(key));
            setParams(ps, values.toArray());
            return ps.executeUpdate();
        }
    }

    public static int deleteByMap(Connection conn, String table, Map<String, Object> data, List<String> whereKeys, Dialect dialect) throws SQLException {
        String sql = buildDeleteSQL(table, whereKeys, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, whereKeys.stream().map(data::get).toArray());
            return ps.executeUpdate();
        }
    }

    public static int insertByArray(Connection conn, String table, List<Map<String, Object>> list, List<String> columns, Dialect dialect) throws SQLException {
        String sql = buildInsertSQL(table, columns, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, Object> data : list) {
                setParams(ps, columns.stream().map(data::get).toArray());
                ps.addBatch();
            }
            return Arrays.stream(ps.executeBatch()).sum();
        }
    }

    public static Dialect detectDialect(Connection conn) throws SQLException {
        String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
        if (dbName.contains("postgres")) return Dialect.POSTGRES;
        if (dbName.contains("oracle")) return Dialect.ORACLE;
        if (dbName.contains("mysql")) return Dialect.MYSQL;
        if (dbName.contains("sql server")) return Dialect.SQLSERVER;
        throw new UnsupportedOperationException("Unsupported DB: " + dbName);
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            list.add(toMap(rs));
        }
        return list;
    }

    private static Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            row.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        return row;
    }

    private static String quote(String name, Dialect dialect) {
        switch (dialect) {
            case SQLSERVER:
                return "[" + name + "]";
            case MYSQL:
                return "`" + name + "`";
            case POSTGRES:
            case ORACLE:
            default:
                return name;
        }
    }

    private static String buildInsertSQL(String table, List<String> columns, Dialect dialect) {
        String colStr = columns.stream().map(c -> quote(c, dialect)).collect(Collectors.joining(", "));
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        return "INSERT INTO " + quote(table, dialect) + " (" + colStr + ") VALUES (" + placeholders + ")";
    }

    private static String buildUpdateSQL(String table, List<String> columns, List<String> whereKeys, Dialect dialect) {
        String setPart = columns.stream().map(c -> quote(c, dialect) + " = ?").collect(Collectors.joining(", "));
        String wherePart = whereKeys.stream().map(k -> quote(k, dialect) + " = ?").collect(Collectors.joining(" AND "));
        return "UPDATE " + quote(table, dialect) + " SET " + setPart + " WHERE " + wherePart;
    }

    private static String buildDeleteSQL(String table, List<String> whereKeys, Dialect dialect) {
        String wherePart = whereKeys.stream().map(k -> quote(k, dialect) + " = ?").collect(Collectors.joining(" AND "));
        return "DELETE FROM " + quote(table, dialect) + " WHERE " + wherePart;
    }

    /**
     * 指定したテーブルに対して、複数のカラム条件（AND結合）に基づきレコードが存在するかを確認します。
     * <p>
     * 条件に合致するレコードが1件も存在しない場合に {@code true} を返します。
     * </p>
     *
     * <pre>{@code
     * Map<String, Object> whereMap = Map.of(
     *     "SHORI_TUKI", "202507",
     *     "TK_DVS", "A01"
     * );
     * boolean isEmpty = isTableEmpty(conn, "TKC001", whereMap);
     * }</pre>
     *
     * @param conn      データベース接続（JDBC Connection）
     * @param tableName 対象のテーブル名（例: "TKC001"）
     * @param whereMap  WHERE条件を表すマップ（キー：カラム名、値：バインド値）
     * @return レコードが存在しなければ {@code true}、存在すれば {@code false}
     * @throws RuntimeException SQLの実行またはデータ取得に失敗した場合
     */
    public static boolean isTableEmpty(Connection conn, String tableName, Map<String, Object> whereMap) throws SQLException {
        String whereClause = String.join(" AND ",
                whereMap.keySet().stream()
                        .map(col -> col + " = ?")
                        .collect(Collectors.toList()));

        String sql = String.format("SELECT COUNT(*) AS cnt FROM %s WHERE %s", tableName, whereClause);

        return DbUtil.select(conn, sql, whereMap.values().toArray())
                .stream().findFirst()
                .map(row -> ((Number) row.get("cnt")).intValue() == 0)
                .orElse(true);
    }

    public static List<String> getInsertableColList(Connection conn, String tableName, Map<String, Object> valueMap) throws SQLException {
        List<String> availableCols = new ArrayList<>();
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    if (valueMap.containsKey(col)) {
                        availableCols.add(col);
                    }
                }
            }
        }
        return availableCols;
    }

    public static int insertByMapAutoCols(Connection conn, String tableName, Map<String, Object> valueMap, DbUtil.Dialect dialect) throws SQLException {
        List<String> colList = new ArrayList<>();

        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    if (valueMap.containsKey(colName)) {
                        colList.add(colName);
                    }
                }
            }
        }

        return DbUtil.insertByMap(conn, tableName, valueMap, colList, dialect);
    }




}
