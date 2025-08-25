package jp.co.technopro.talon.util.common;

import jp.co.technopro.talon.db.common.DbConfigLoader;
import jp.co.technopro.talon.dto.common.SqlResult;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.sql.common.SqlLoader;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import jp.co.technopro.talon.sql.common.SqlLoaderFactory;

public class DbUtil {

    private static SqlLoader sqlLoader = SqlLoaderFactory.commonLoader(); // デフォルト値

    /**
     * 外部からSQLローダーを差し替える（テスト・環境ごとの切り替え用）
     *
     * @param loader 差し替える SqlLoader（共通、カンパニー別など）
     */
    public static void setSqlLoader(SqlLoader loader) {
        sqlLoader = loader;
    }

    public static SqlLoader getSqlLoader() {
        return sqlLoader;
    }

    // 例：SQL定義を取得して実行するユーティリティ
    public static String getSql(String id) {
        return sqlLoader.get(id);
    }

    public enum Dialect {
        SQLSERVER, POSTGRES, ORACLE, MYSQL
    }

    /**
     * DB接続を取得します。指定された Connection が null の場合は、
     * 会社コードに応じた DB構成ファイルから新たに接続を取得します。
     *
     * @param conn      明示的に指定された Connection（null可）
     * @param companyCd 会社コード（例: "gojo", "common"）
     * @return DB接続（非null）
     * @throws Exception 接続取得時にエラーが発生した場合
     */
    public static Connection getConnectionIfNull(Connection conn, String companyCd)  {
        if (conn != null) return conn;
        try {
            return DbConfigLoader.load(companyCd).getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SQL IDを指定してSQLを実行し、結果をList<Map>で返却します。
     *
     * <p>
     * conn が null の場合は companyCd に基づいて新しい接続を取得し、自動クローズします。<br>
     * conn が指定されている場合は、その接続はクローズされません（TALON由来想定）。
     * </p>
     *
     * @param conn      DBコネクション（null可）
     * @param sqlId     SQL定義ファイルで定義されたSQLのID
     * @param companyCd 会社コード（connがnullの場合に使用）
     * @param params    プレースホルダにバインドするパラメータ
     * @return SQL実行結果のレコードリスト（各行はMap形式）
     * @throws SQLException SQL実行時の例外
     */
    public static SqlResult selectById(Connection conn, String sqlId, String companyCd, Object... params)
             {

        SqlLoader loader = SqlLoaderFactory.forCompanyWithCommon(companyCd);
        String sql = loader.get(sqlId);

        if (sql == null) {
            throw new IllegalArgumentException("SQL ID が見つかりません: " + sqlId);
        }

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            try {
                if (actualConn == null || actualConn.isClosed()) {
                    actualConn = getConnectionIfNull(null, companyCd);
                    shouldClose = true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            List<Map<String, Object>> resultList = null;
            try {
                resultList = select(actualConn, companyCd, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            SqlResult result = new SqlResult();
            result.setMapListResult(resultList);
            result.setAffectedRows(resultList.size());
            return result;

        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }


    /**
     * プレースホルダ付きSQLとバインド値を使って擬似SQLを生成します（デバッグ用）
     *
     * @param rawSql プレースホルダ（?）付きSQL
     * @param params バインドパラメータ
     * @return バインド済みのSQL風文字列
     */
    private static String buildExecutedSql(String rawSql, Object... params) {
        if (params == null || params.length == 0) return rawSql;

        StringBuilder result = new StringBuilder();
        int paramIndex = 0;

        for (int i = 0; i < rawSql.length(); i++) {
            char c = rawSql.charAt(i);
            if (c == '?' && paramIndex < params.length) {
                Object param = params[paramIndex++];
                String value;
                if (param == null) {
                    value = "NULL";
                } else if (param instanceof String || param instanceof java.sql.Date || param instanceof java.time.LocalDate) {
                    value = "'" + param.toString().replace("'", "''") + "'";
                } else {
                    value = param.toString();
                }
                result.append(value);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 指定されたSQLを、指定のDBコネクションまたは会社コードに応じた接続で実行し、
     * 結果を1行1MapとしてList形式で返却します。
     *
     * <p>各Mapのキーはカラム名（ResultSetのラベル）、値は対応するオブジェクトです。</p>
     *
     * @param conn      DBコネクション（nullの場合は companyCd に応じて接続を取得）
     * @param companyCd 会社コード（例: "gojo", "common"）conn が null の場合に使用
     * @param sql       実行するSQL文（例: "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params    SQL文中のプレースホルダ（"?"）にバインドされるパラメータ（可変長）
     * @return レコード一覧（各行をMap<String, Object>として表現）
     * @throws SQLException SQL構文エラーや接続取得失敗などのDB例外が発生した場合
     */
    public static List<Map<String, Object>> select(Connection conn, String companyCd, String sql, Object... params) throws SQLException {
        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }

            String simulatedSql = buildExecutedSql(sql, params);
            System.out.println("Executing SQL: " + simulatedSql);

            try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
                setParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("SQL実行中にエラーが発生しました", e);
        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 指定したSQLを実行してレコードを削除します。
     * SQL文にはプレースホルダ（"?"）を含めることができ、可変長引数として値をバインドできます。
     * <p>
     * conn が null の場合は companyCd に応じて接続を取得し、実行後に自動クローズされます。
     * </p>
     *
     * @param conn      使用するDBコネクション（nullの場合は自動取得）
     * @param companyCd 会社コード（例: "gojo", "common"）conn が null の場合に使用
     * @param sql       実行するDELETE文（例: "DELETE FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params    SQLのプレースホルダにバインドするパラメータ（可変長）
     * @return 削除されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int delete(Connection conn, String companyCd, String sql, Object... params) throws SQLException {
        boolean closeAfter = false;
        try {
            if (conn == null) {
                conn = getConnectionIfNull(null, companyCd);
                closeAfter = true;
            }

            String simulatedSql = buildExecutedSql(sql, params);
            System.out.println("Executing SQL: " + simulatedSql);

            try (Connection autoClose = closeAfter ? conn : null;
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                setParams(stmt, params);
                return stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new SQLException("DELETE実行中にエラーが発生しました", e);
        }
    }

    /**
     * 指定されたSQLを実行し、更新されたレコード数を返します。
     * UPDATE文・INSERT文・MERGE文 などのDML全般に使用可能です。
     * <p>
     * conn が null の場合、companyCd に応じて接続を取得し、自動クローズします。
     * </p>
     *
     * @param conn      使用するDBコネクション（nullの場合は自動取得）
     * @param companyCd 会社コード（例: "gojo", "common"）conn が null の場合に使用
     * @param sql       実行するSQL文（例: "UPDATE TKC001 SET STATUS = ? WHERE ID = ?"）
     * @param params    SQL文中のプレースホルダ（"?"）にバインドされるパラメータ（可変長）
     * @return 更新されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int update(Connection conn, String companyCd, String sql, Object... params) throws SQLException {
        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }

            String simulatedSql = buildExecutedSql(sql, params);
            System.out.println("Executing SQL: " + simulatedSql);

            try (PreparedStatement stmt = actualConn.prepareStatement(sql)) {
                setParams(stmt, params);
                return stmt.executeUpdate();
            }

        } catch (Exception e) {
            throw new SQLException("UPDATE実行中にエラーが発生しました", e);

        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * DBメタデータからDBの種類を判定し、対応するDialectを返します。
     *
     * @param conn DBコネクション
     * @return 判定されたDialect（SQLSERVER, POSTGRES, ORACLE, MYSQLのいずれか）
     * @throws SQLException                  メタデータ取得時のエラー
     * @throws UnsupportedOperationException 未対応のDB製品名だった場合
     */
    public static Dialect detectDialect(Connection conn) throws SQLException {
        String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
        if (dbName.contains("postgres")) return Dialect.POSTGRES;
        if (dbName.contains("oracle")) return Dialect.ORACLE;
        if (dbName.contains("mysql")) return Dialect.MYSQL;
        if (dbName.contains("sql server")) return Dialect.SQLSERVER;
        throw new UnsupportedOperationException("Unsupported DB: " + dbName);
    }

    /**
     * PreparedStatement に対してパラメータを順にバインドします。
     *
     * @param ps     バインド対象のPreparedStatement
     * @param params プレースホルダにバインドする値（順序通りにセットされます）
     * @throws SQLException バインド時のエラー
     */
    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null || params.length == 0) return;

        int expectedCount = ps.getParameterMetaData().getParameterCount();
        if (params.length != expectedCount) {
            throw new SQLException("プレースホルダ（?）の数とバインドするパラメータ数が一致していません。"
                    + " expected=" + expectedCount + ", actual=" + params.length);
        }

        for (int i = 0; i < params.length; i++) {

            Object param = params[i];
            System.out.println("  -> param[" + (i + 1) + "] = " + param + " (" + (param != null ? param.getClass().getSimpleName() : "null") + ")");
            ps.setObject(i + 1, params[i]);
        }
    }

    /**
     * ResultSetの全行をMap形式に変換してListで返します。
     * 各レコードは {@link #toMap(ResultSet)} によりMap化されます。
     *
     * @param rs 変換対象のResultSet（先頭行以前の状態で渡す必要あり）
     * @return レコードのリスト（各レコードはMap<String, Object>形式）
     * @throws SQLException ResultSet処理中の例外
     */
    private static List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            list.add(toMap(rs));
        }
        return list;
    }


    /**
     * ResultSetの現在行をMap形式に変換します。
     * カラム名（エイリアス含む）をキー、カラム値をバリューとするMapを返します。
     *
     * @param rs 変換対象のResultSet（現在行にポインタがある状態であること）
     * @return 1レコード分のデータを表すMap（キー=カラム名、値=カラム値）
     * @throws SQLException ResultSet処理中の例外
     */
    private static Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            row.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        return row;
    }

    /**
     * テーブル名・カラム名などの識別子をDB方言に応じてクォート（エスケープ）します。
     *
     * @param name    クォート対象の名前（カラム名など）
     * @param dialect DB方言（SQLSERVER, MYSQL, POSTGRES, ORACLEなど）
     * @return クォート後の文字列（例: "[COL]" や "`col`"）
     */
    private static String quote(String name, Dialect dialect) {
        switch (dialect) {
            case SQLSERVER:
                return "[" + name + "]";
            case MYSQL:
                return "`" + name + "`";
            case POSTGRES:
            case ORACLE:
            default:
                return name; // クォート不要 or 呼び出し側で対応
        }
    }

    /**
     * INSERT文を生成します（可読性のため改行を含む）。
     *
     * @param table   テーブル名
     * @param columns 挿入カラムリスト（順序必須）
     * @param dialect DB方言
     * @return INSERT文（改行付き）
     */
    private static String buildInsertSQL(String table, List<String> columns, Dialect dialect) {
        String colStr = columns.stream().map(c -> quote(c, dialect)).collect(Collectors.joining(", "));
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));

        return "INSERT INTO " + quote(table, dialect) + "\n" + "    (" + colStr + ")\n" + "VALUES\n" + "    (" + placeholders + ")";
    }

    /**
     * UPDATE文を生成します（可読性のため改行を含む）。
     *
     * @param table     テーブル名
     * @param columns   更新対象カラムリスト（SET句）
     * @param whereKeys WHERE句カラムリスト
     * @param dialect   DB方言
     * @return UPDATE文（改行付き）
     */
    private static String buildUpdateSQL(String table, List<String> columns, List<String> whereKeys, Dialect dialect) {
        String setClause = columns.stream().map(c -> quote(c, dialect) + " = ?").collect(Collectors.joining(",\n    ")); // ← 改行とインデントをここで追加

        String whereClause = whereKeys.stream().map(k -> quote(k, dialect) + " = ?").collect(Collectors.joining(" AND\n    ")); // ← 改行とインデント

        return "UPDATE " + quote(table, dialect) + "\n" + "SET\n" + "    " + setClause + "\n" + "WHERE\n" + "    " + whereClause;
    }

    /**
     * DELETE文を生成します（可読性のため改行を含む）。
     *
     * @param table     テーブル名
     * @param whereKeys WHERE句カラムリスト
     * @param dialect   DB方言
     * @return DELETE文（改行付き）
     */
    private static String buildDeleteSQL(String table, List<String> whereKeys, Dialect dialect) {
        String whereClause = whereKeys.stream().map(k -> quote(k, dialect) + " = ?").collect(Collectors.joining(" AND\n    "));  // ← 改行とインデントをここで明示

        return "DELETE FROM " + quote(table, dialect) + "\n" + "WHERE\n" + "    " + whereClause;
    }

    /**
     * 指定したテーブルに対して、WHERE条件に合致するレコードが存在しないかを確認します。
     * <p>
     * 条件に一致するレコードが1件も存在しなければ {@code true} を返します。
     * </p>
     *
     * @param conn      データベース接続
     * @param tableName テーブル名（例: "TK_MEMBER"）
     * @param whereMap  WHERE条件（カラム名→値。null指定で IS NULL）
     * @return レコードが存在しなければ true、存在すれば false
     * @throws SQLException SQL実行時の例外
     */
    public static boolean isTableEmpty(Connection conn, String tableName, Map<String, Object> whereMap, String companyCode)  {
        int count = getCount(conn, tableName, whereMap, companyCode);
        return count == 0;
    }

    /**
     * 指定されたテーブルの定義情報を INFORMATION_SCHEMA.COLUMNS などから取得し、
     * valueMap に存在するカラムのみを抽出して返します。
     *
     * <p>主に {@code insertByMap(...)} 用のカラムリスト生成に使用します。</p>
     *
     * @param conn      DBコネクション（null可。null時は companyCd に応じて取得）
     * @param companyCd 会社コード（DB接続取得用）
     * @param tableName 対象テーブル名（例: "TKC001"）
     * @param valueMap  値を保持するMap（キー=カラム名候補）
     * @return テーブルに実在し、かつ valueMap に含まれているカラム名リスト
     * @throws SQLException SQL実行時の例外
     */
    public static List<String> gettableColList(Connection conn, String companyCd, String tableName, Map<String, Object> valueMap) throws SQLException, ClassNotFoundException {

        Connection actualConn = getConnectionIfNull(conn, companyCd);
        List<String> availableCols = new ArrayList<>();
        Dialect dialect = detectDialect(actualConn);

        String sql;
        boolean caseSensitive = false;

        switch (dialect) {
            case SQLSERVER:
                sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'";
                break;
            case POSTGRES:
                sql = "SELECT column_name AS COLUMN_NAME FROM information_schema.columns WHERE table_name = ?";
                caseSensitive = true;
                break;
            case ORACLE:
                sql = "SELECT column_name AS COLUMN_NAME FROM user_tab_columns WHERE table_name = ?";
                tableName = tableName.toUpperCase();
                break;
            case MYSQL:
            default:
                sql = "SELECT column_name AS COLUMN_NAME FROM information_schema.columns WHERE table_name = ?";
                break;
        }

        try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            System.out.println("Executing SQL: " + buildExecutedSql(sql, tableName));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    String keyToCheck;

                    if (caseSensitive) {
                        keyToCheck = col.toLowerCase();
                    } else if (dialect == Dialect.ORACLE) {
                        keyToCheck = col.toUpperCase();
                    } else {
                        keyToCheck = col;
                    }

                    if (valueMap.containsKey(keyToCheck)) {
                        availableCols.add(col);
                    }
                }
            }
        }

        return availableCols;
    }


    /**
     * DB方言に応じてカラム名のキー文字列を正規化します。
     * <p>
     * PostgreSQL: 小文字化<br>
     * Oracle    : 大文字化<br>
     * その他    : 変換なし
     * </p>
     *
     * @param key     カラム名のキー（Mapのキーなど）
     * @param dialect データベース方言（Dialect列挙型）
     * @return 正規化されたカラム名
     */
    private static String normalizeColumnKey(String key, Dialect dialect) {
        if (key == null) return null;

        switch (dialect) {
            case POSTGRES:
                return key.toLowerCase();
            case ORACLE:
                return key.toUpperCase();
            default:
                return key;
        }
    }

    /**
     * Mapの値を元に、指定されたテーブルへ1行INSERTします。
     * conn が null の場合は companyCd で接続を補完します。
     * null を含めるかどうかは includeNulls で制御します。
     *
     * @param conn         DBコネクション（null可）
     * @param companyCd    会社コード（例: "gojo", "common"）conn が null の場合に使用
     * @param tableName    挿入先テーブル名（例: "TKC001"）
     * @param valueMap     挿入する値（キー=カラム名、値=挿入値）
     * @param includeNulls true の場合、null 値も挿入対象に含める
     * @throws SQLException SQL実行時の例外
     */
    public static void insertByMapEx(Connection conn, String companyCd, String tableName,
                                     Map<String, Object> valueMap, boolean includeNulls) throws SQLException {
        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }

            List<String> columns = gettableColList(actualConn, companyCd, tableName, valueMap);

            List<String> insertCols = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            for (String col : columns) {
                Object val = valueMap.get(col);
                if (val != null || includeNulls) {
                    insertCols.add(col);
                    params.add(val);
                }
            }

            String sql = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    tableName,
                    String.join(", ", insertCols),
                    insertCols.stream().map(c -> "?").collect(Collectors.joining(", "))
            );

            String simulatedSql = buildExecutedSql(sql, params.toArray());
            System.out.println("Executing SQL: " + simulatedSql);

            try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
                setParams(ps, params.toArray());
                ps.executeUpdate();
            }

        } catch (Exception e) {
            throw new SQLException("insertByMapExの実行中にエラーが発生しました", e);

        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }


    /**
     * 任意のテーブルに対して、指定された値マップを用いてレコードを更新します。
     * conn が null の場合は companyCd に応じた接続を取得します。
     *
     * @param conn         データベース接続（null可）
     * @param companyCd    会社コード（conn が null の場合に使用）
     * @param tableName    更新対象のテーブル名（例: "TKC001"）
     * @param valueMap     更新する値のマップ（カラム名→値）
     * @param whereMap     WHERE句の条件（カラム名→値）
     * @param includeNulls null 値も更新する場合は true
     * @return 更新された件数
     * @throws SQLException             SQL実行時の例外
     * @throws IllegalArgumentException カラムが空など入力不備時
     */
    public static int updateByMapEx(Connection conn, String companyCd, String tableName,
                                    Map<String, Object> valueMap, Map<String, Object> whereMap,
                                    boolean includeNulls) throws SQLException {

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }

            List<String> columns = gettableColList(actualConn, companyCd, tableName, valueMap);
            if (columns == null || columns.isEmpty()) {
                throw new IllegalArgumentException("更新対象カラムが取得できませんでした: " + tableName);
            }

            List<String> setClauses = new ArrayList<>();
            List<Object> params = new ArrayList<>();

            for (String col : columns) {
                if (valueMap.containsKey(col)) {
                    Object val = valueMap.get(col);
                    if (val != null || includeNulls) {
                        setClauses.add(col + " = ?");
                        params.add(val);
                    }
                }
            }

            if (setClauses.isEmpty()) {
                throw new IllegalArgumentException("更新対象カラムが存在しません: " + tableName);
            }

            List<String> whereClauses = new ArrayList<>();
            for (Map.Entry<String, Object> entry : whereMap.entrySet()) {
                whereClauses.add(entry.getKey() + " = ?");
                params.add(entry.getValue());
            }

            if (whereClauses.isEmpty()) {
                throw new IllegalArgumentException("WHERE条件が未指定です。全件更新は禁止されています: " + tableName);
            }

            String sql = String.format("UPDATE %s SET %s WHERE %s",
                    tableName,
                    String.join(", ", setClauses),
                    String.join(" AND ", whereClauses));

            String simulatedSql = buildExecutedSql(sql, params.toArray());
            System.out.println("Executing SQL: " + simulatedSql);

            try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
                setParams(ps, params.toArray());
                return ps.executeUpdate();
            }

        } catch (Exception e) {
            throw new SQLException("updateByMapEx 実行時にエラーが発生しました", e);

        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 指定テーブルに対して、WHERE条件に合致する最初の1レコードを取得します。
     *
     * @param conn      DBコネクション（null可）
     * @param tableName テーブル名（例: "TK_MEMBER"）
     * @param columns   取得カラム（null または空の場合は *）
     * @param whereMap  WHERE条件（必須）
     * @param orderBy   ORDER BY句（null可）
     * @param companyCd 会社コード（conn が null の場合に使用）
     * @return SqlResult（最初の1件のレコードが mapResult に格納される）
     * @throws SQLException DBアクセス時の例外
     */
    public static SqlResult selectOne(Connection conn, String tableName, List<String> columns,
                                      Map<String, Object> whereMap, String orderBy,
                                      String companyCd) throws SQLException {

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }
        } catch (Exception e) {
            throw new SQLException("DB接続の取得に失敗しました", e);
        }

        String sql = buildSimpleSelectSQL(actualConn, tableName, columns, whereMap, orderBy, 1);
        List<Object> params = columnsToParams(whereMap);

        String simulatedSql = buildExecutedSql(sql, params.toArray());
        System.out.println("Executing SQL: " + simulatedSql);

        try {
            List<Map<String, Object>> resultList = select(actualConn, sql, companyCd, params.toArray());

            SqlResult result = new SqlResult();
            if (!resultList.isEmpty()) {
                result.setMapResult(resultList.get(0));
            }
            result.setSize(resultList.size());
            return result;

        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 指定されたマップの値部分をリストとして返却します。
     * <p>
     * 主にSQLパラメータの設定時などで、値のみの順序付きリストが必要な場合に使用します。
     * キーの順序は保証されません（Mapの実装依存）。
     *
     * @param map パラメータマップ（キーは列名、値はSQLバインド値など）
     * @return 値のリスト（順序はマップのvalue順、キー順ではない）
     */
    private static List<Object> columnsToParams(Map<String, Object> map) {
        return map.values().stream().collect(Collectors.toList());
    }

    /**
     * 指定されたマップから、指定されたキー順に対応する値リストを生成します。
     *
     * @param map  値を取得するマップ
     * @param keys 値の取得順序を指定するキーリスト
     * @return 対応する値のリスト
     */
    public static List<Object> valuesByKeys(Map<String, Object> map, List<String> keys) {
        return keys.stream().map(map::get).collect(Collectors.toList());
    }


    /**
     * 任意のテーブルに対して、指定された WHERE 条件で SELECT を実行し、
     * 結果をリスト形式で返却します。
     *
     * <p>
     * {@link SqlResult} で行データリストを取得可能です。<br>
     * {@link SqlResult#getSize()} で取得件数を確認可能です。
     * </p>
     *
     * @param conn      DBコネクション（null指定で自動取得）
     * @param tableName テーブル名（例: "TK_MEMBER"）
     * @param whereMap  WHERE条件（カラム名をキー、値をバインド対象。null指定で IS NULL）
     * @param companyCd DB接続取得用の会社コード（connがnullの場合に使用）
     * @return SqlResult（行リストと件数が格納される）
     * @throws SQLException SQL実行時の例外
     */
    public static SqlResult selectList(Connection conn, String tableName,
                                       Map<String, Object> whereMap,
                                       String companyCd) throws SQLException {

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }
        } catch (Exception e) {
            throw new SQLException("DB接続の取得に失敗しました", e);
        }

        String sql = buildSimpleSelectSQL(actualConn, tableName, null, whereMap, null, 0);
        List<Object> params = columnsToParams(whereMap);

        try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> result = new ArrayList<>();
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    result.add(row);
                }

                SqlResult sqlResult = new SqlResult();
                sqlResult.setMapListResult(result);
                sqlResult.setSize(result.size());
                return sqlResult;
            }
        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * INSERT文のカラム名部分とバリュー部分を構築します。
     *
     * @param dataMap      対象データマップ
     * @param excludeNulls null値を除外する場合 true
     * @return [カラム部, プレースホルダ部]
     */
    private static String[] buildInsertSqlParts(Map<String, Object> dataMap, boolean excludeNulls) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (excludeNulls && entry.getValue() == null) continue;
            if (columns.length() > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(entry.getKey());
            placeholders.append("?");
        }
        return new String[]{columns.toString(), placeholders.toString()};
    }

    /**
     * UPDATE文のSET句部分を構築します。
     *
     * @param dataMap      対象データマップ
     * @param keyCols      主キー列（WHERE条件に使うため除外）
     * @param excludeNulls null値を除外する場合 true
     * @return SET句文字列（例: COL1 = ?, COL2 = ?）
     */
    private static String buildUpdateSqlSetClause(Map<String, Object> dataMap, List<String> keyCols, boolean excludeNulls) {
        StringBuilder setClause = new StringBuilder();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (keyCols.contains(entry.getKey())) continue;
            if (excludeNulls && entry.getValue() == null) continue;
            if (setClause.length() > 0) setClause.append(", ");
            setClause.append(entry.getKey()).append(" = ?");
        }
        return setClause.toString();
    }

    /**
     * 単純な SELECT 文を構築します（WHERE / ORDER BY / LIMIT 対応）。
     *
     * <p>
     * 対象のテーブルから指定されたカラムを取得し、条件に応じて WHERE 句、
     * ORDER BY 句、および LIMIT（または SQL Server の場合 TOP）句を動的に組み立てます。
     * </p>
     *
     * <p>
     * 利用例：<br>
     * {@code buildSimpleSelectSQL(conn, "TK_MEMBER", List.of("TK_NO", "NAME"), whereMap, "TK_NO ASC", 100);}
     * </p>
     *
     * @param conn      DB接続（Dialect 判定に使用されます。null 禁止）
     * @param tableName 対象テーブル名（例: "TK_MEMBER"）
     * @param columns   取得カラム名リスト（null または空指定で * を使用）
     * @param whereMap  WHERE 条件（カラム名をキー、バインド対象値をバリュー。null 可）
     * @param orderBy   ORDER BY 句（例: "TK_NO ASC"。null 可）
     * @param limit     最大取得件数（0以下で LIMIT 無し）
     * @return 構築済み SQL 文（RDBMS に応じた構文で生成されます）
     * @throws SQLException コネクションから Dialect を判定できなかった場合
     */
    public static String buildSimpleSelectSQL(Connection conn, String tableName, List<String> columns,
                                              Map<String, Object> whereMap, String orderBy, int limit) throws SQLException {

        Dialect dialect = detectDialect(conn); // 渡された接続から判断

        // カラム部
        String selectCols = (columns == null || columns.isEmpty())
                ? "*"
                : String.join(", ", columns);

        // LIMIT句 or TOP句
        String limitClause = (limit > 0) ? buildLimitClause(dialect, limit) : "";

        // SQL Serverの場合、TOPはSELECT句内に必要
        String selectClause = dialect == Dialect.SQLSERVER && !limitClause.isEmpty()
                ? "SELECT " + limitClause + " " + selectCols
                : "SELECT " + selectCols;

        StringBuilder sql = new StringBuilder(selectClause)
                .append(" FROM ").append(tableName);

        // WHERE句生成
        if (whereMap != null && !whereMap.isEmpty()) {
            String whereClause = whereMap.keySet().stream()
                    .map(key -> key + " = ?")
                    .collect(Collectors.joining(" AND "));
            sql.append(" WHERE ").append(whereClause);
        }

        // ORDER BY句
        if (orderBy != null && !orderBy.isBlank()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        // SQL Server以外はSELECT句後にLIMIT句を追加
        if (limit > 0 && dialect != Dialect.SQLSERVER) {
            sql.append(" ").append(limitClause);
        }

        return sql.toString();
    }

    // LIMIT句の構築（提供済み）
    private static String buildLimitClause(Dialect dialect, int limit) {
        switch (dialect) {
            case SQLSERVER:
                return "TOP " + limit;
            case POSTGRES:
            case MYSQL:
                return "LIMIT " + limit;
            case ORACLE:
                return "FETCH FIRST " + limit + " ROWS ONLY";
            default:
                return "";
        }
    }

    /**
     * 指定テーブルから単一レコードを Map 形式で削除します。
     * <p>
     * 削除件数は {@link SqlResult#getAffectedRows()} で取得可能です。
     * </p>
     *
     * @param conn      DB接続（null可。nullの場合は {@link DbConfigLoader} から取得）
     * @param table     対象テーブル名
     * @param data      WHERE条件として使用するデータ（カラム名→値）
     * @param whereKeys WHERE句に使うキー（例: Arrays.asList("id")）
     * @param companyCd DB接続を取得する際に使用する会社コード（connがnullの場合）
     * @return SqlResult（削除件数が affectedRows に格納される）
     * @throws SQLException SQL実行時の例外
     */
    public static int deleteByMapEx(Connection conn, String table, Map<String, Object> data,
                                          List<String> whereKeys, String companyCd) throws SQLException {

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }
        } catch (Exception e) {
            throw new SQLException("DB接続の取得に失敗しました", e);
        }

        Dialect dialect = detectDialect(actualConn);
        String sql = buildDeleteSQL(table, whereKeys, dialect);

        try (PreparedStatement ps = actualConn.prepareStatement(sql)) {
            setParams(ps, whereKeys.stream().map(data::get).toArray());
            int affected = ps.executeUpdate();

            SqlResult result = new SqlResult();
            result.setAffectedRows(affected);
            result.setSize(affected);
            return affected;
        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }


    /**
     * 任意のテーブルに対して、指定された WHERE 条件に合致するレコード件数を返します。
     *
     * <p>
     * null値の指定がある場合は IS NULL 句を使用します。
     * コネクションが null の場合は {@code DbConfigLoader} 経由で取得し、
     * 処理後に自動クローズされます。
     * </p>
     *
     * @param conn      DBコネクション（null指定で自動取得）
     * @param tableName テーブル名（例: "TK_HENKO2"）
     * @param whereMap  WHERE条件（キー=カラム名、値=null指定でIS NULL判定）
     * @param companyCd DB接続取得用の会社コード（connがnullの場合に使用）
     * @return 該当レコード件数（0以上）
     * @throws SQLException SQL実行時の例外
     */
    public static int getCount(Connection conn, String tableName,
                               Map<String, Object> whereMap, String companyCd)  {

        boolean shouldClose = false;
        Connection actualConn = conn;

        try {
            if (actualConn == null || actualConn.isClosed()) {
                actualConn = getConnectionIfNull(null, companyCd);
                shouldClose = true;
            }
        } catch (Exception e) {
            try {
                throw new SQLException("DB接続の取得に失敗しました", e);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS CNT FROM ").append(tableName);
        if (whereMap != null && !whereMap.isEmpty()) {
            sql.append(" WHERE ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : whereMap.entrySet()) {
                if (!first) sql.append(" AND ");
                String column = entry.getKey();
                Object value = entry.getValue();
                if (value == null) {
                    sql.append(column).append(" IS NULL");
                } else {
                    sql.append(column).append(" = ?");
                }
                first = false;
            }
        }

        try (PreparedStatement ps = actualConn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object value : whereMap.values()) {
                if (value != null) {
                    ps.setObject(index++, value);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("CNT") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (shouldClose && actualConn != null) {
                try {
                    actualConn.close();
                } catch (Exception e) {
                    System.err.println("[WARN] コネクションのクローズに失敗: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 採番を実行し、整形済みの番号を取得します。
     * <p>
     * {@code TLN_M_SEQ} の LINE_NO をインクリメントし、 {@code TLN_M_SEQ_PARAMETER} の定義に基づき整形された番号を返却します。
     * </p>
     *
     * @param conn      DBコネクション（共通DB）
     * @param saibanKey 採番キー（例: "AUTO_NO"）
     * @param count     発番件数
     * @return NO: 整形済み番号、NO_LIST: 発番リスト
     * @throws SQLException SQL実行時例外
     */
    public static Map<String, Object> getNumberingData(Connection conn, String saibanKey, int count) throws SQLException {
        String breakKey = resolveBreakKey(conn, saibanKey);
        ensureSeqRecordExists(conn, saibanKey, breakKey);
        return getNumberingDataInternal(conn, saibanKey, breakKey, count);
    }

    /**
     * BREAK_KEY（年月など）をTLN_M_SEQ_PARAMETERから判定
     */
    private static String resolveBreakKey(Connection conn, String saibanKey) throws SQLException {
        String sql = "SELECT VALUE FROM TLN_M_SEQ_PARAMETER WHERE SAIBAN_SIKIBETU_CODE = ? AND KIND = 2 ORDER BY SEQ_NO";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saibanKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fmt = rs.getString("VALUE");
                    return formatDate(fmt);
                } else {
                    return "NONE";
                }
            }
        }
    }

    /**
     * ブレークキーが存在しない場合はTLN_M_SEQにINSERT
     */
    private static void ensureSeqRecordExists(Connection conn, String saibanKey, String breakKey) throws SQLException {
        String checkSql = "SELECT 1 FROM TLN_M_SEQ WHERE SAIBAN_SIKIBETU_CODE = ? AND BREAK_KEY = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, saibanKey);
            ps.setString(2, breakKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    String insertSql = "INSERT INTO TLN_M_SEQ (SAIBAN_SIKIBETU_CODE, BREAK_KEY, LINE_NO, CREATED_DATE, CREATED_BY, CREATED_PRG_NM, MODIFY_COUNT) VALUES (?, ?, 0, GETDATE(), 'system', 'DbUtil.getNumberingData', 0)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setString(1, saibanKey);
                        insertPs.setString(2, breakKey);
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * 採番実行本体（BREAK_KEY を指定して実行）
     */
    private static Map<String, Object> getNumberingDataInternal(Connection conn, String saibanKey, String breakKey, int count) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Integer> numberList = new ArrayList<>();

        // 採番マスタ更新＋取得
        String updateSql = "UPDATE TLN_M_SEQ SET LINE_NO = ISNULL(LINE_NO, 0) + ? OUTPUT INSERTED.LINE_NO WHERE SAIBAN_SIKIBETU_CODE = ? AND BREAK_KEY = ?";
        int latestNo;

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, count);
            ps.setString(2, saibanKey);
            ps.setString(3, breakKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("採番マスタが存在しません: " + saibanKey + ", BREAK_KEY=" + breakKey);
                }
                latestNo = rs.getInt("LINE_NO");
            }
        }

        for (int i = count - 1; i >= 0; i--) {
            numberList.add(latestNo - i);
        }

        // 採番履歴登録
        String insertSql = "INSERT INTO TLN_M_SEQ_PARAMETER (SAIBAN_SIKIBETU_CODE, SEQ_NO, VALUE, KIND, CREATED_DATE, CREATED_BY, CREATED_PRG_NM) VALUES (?, ?, ?, 3, GETDATE(), 'system', 'DbUtil.getNumberingData')";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (Integer no : numberList) {
                ps.setString(1, saibanKey);
                ps.setInt(2, no);
                ps.setString(3, String.valueOf(no));
                ps.addBatch();
            }
            ps.executeBatch();
        }

        // フォーマット整形
        List<String> formattedList = numberList.stream()
                .map(no -> formatNumber(conn, saibanKey, no))
                .collect(Collectors.toList());

        result.put("NO", formattedList.get(0));
        result.put("NO_LIST", formattedList);
        return result;
    }

    /**
     * 番号の構築ルールに従って文字列を組み立て
     */
    private static String formatNumber(Connection conn, String saibanKey, int no) {
        List<String> parts = new ArrayList<>();

        String sql = "SELECT SEQ_NO, KIND, VALUE FROM TLN_M_SEQ_PARAMETER WHERE SAIBAN_SIKIBETU_CODE = ? ORDER BY SEQ_NO";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saibanKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int kind = rs.getInt("KIND");
                    String value = rs.getString("VALUE");

                    switch (kind) {
                        case 1:
                            parts.add(value);
                            break;
                        case 2:
                            parts.add(formatDate(value));
                            break;
                        case 3:
                            int digits = Integer.parseInt(value);
                            parts.add(String.format("%0" + digits + "d", no));
                            break;
                        default:
                            parts.add("");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("採番フォーマット取得失敗", e);
        }

        return String.join("", parts);
    }

    /**
     * %tY%tm%td → yyyyMMdd などへ変換し日付整形
     */
    private static String formatDate(String pattern) {
        String sdfPattern = pattern
                .replace("%tY", "yyyy")
                .replace("%ty", "yy")
                .replace("%tm", "MM")
                .replace("%td", "dd");
        return new SimpleDateFormat(sdfPattern).format(new Date());
    }
}