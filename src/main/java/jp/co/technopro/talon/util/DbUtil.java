package jp.co.technopro.talon.util;

import jp.co.technopro.talon.db.DbConfigLoader;
import jp.co.technopro.talon.sql.SqlLoader;
import jp.co.technopro.talon.util.db.DbDialect;
import jp.co.technopro.talon.util.db.DbDialectFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DbUtil {

    private static final SqlLoader sqlLoader = new SqlLoader("sql/common.xml"); // 任意のパス

    public enum Dialect {
        SQLSERVER, POSTGRES, ORACLE, MYSQL
    }

    /**
     * 指定されたコネクションが null の場合、新しい DB コネクションを取得します。
     *
     * @param conn 使用中の DB コネクション（null 許容）
     * @return 有効な DB コネクション
     * @throws SQLException コネクション取得時にエラーが発生した場合
     */
    public static Connection getConnectionIfNull(Connection conn) throws SQLException, ClassNotFoundException {
        if (conn != null) return conn;
        return DbConfigLoader.load().getConnection();
    }

    /**
     * 指定したSQLを実行し、結果をList<Map>形式で取得します。
     * 内部でコネクションを自動取得し、別の {@link #select(Connection, String, Object...)} を呼び出します。
     *
     * <p>各Mapは1行のレコードを表し、カラム名をキー、カラム値を値として保持します。</p>
     *
     * @param sql    実行するSQL文（例: "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params SQLのプレースホルダにバインドするパラメータ（可変長）
     * @return SQL実行結果のレコードリスト（各行はMap形式）
     * @throws Exception コネクション取得やSQL実行中に発生した例外
     */
    public static List<Map<String, Object>> select(String sql, Object... params) throws Exception {
        try (Connection conn = getConnectionIfNull(null)) {
            return select(conn, sql, params);
        }
    }

    /**
     * SQL IDを指定してSQLを実行し、結果をList<Map>で返却します。
     *
     * @param sqlId  SQL定義ファイルで定義されたSQLのID
     * @param params プレースホルダにバインドするパラメータ
     * @return SQL実行結果のレコードリスト（各行はMap形式）
     * @throws SQLException SQL実行時の例外
     */
    public static List<Map<String, Object>> selectById(String sqlId, Object... params) throws SQLException, ClassNotFoundException {
        String sql = sqlLoader.get(sqlId);
        if (sql == null) {
            throw new IllegalArgumentException("SQL ID が見つかりません: " + sqlId);
        }

        try (Connection conn = getConnectionIfNull(null)) {
            return select(conn, sql, params);
        }
    }


    /**
     * 指定されたSQLを指定のDBコネクション上で実行し、
     * 結果を1行1MapとしてList形式で返却します。
     *
     * <p>各Mapのキーはカラム名（ResultSetのラベル）、値は対応するオブジェクトです。</p>
     *
     * @param conn   使用するDBコネクション（null不可）
     * @param sql    実行するSQL文（例："SELECT * FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params SQL文中のプレースホルダ（"?"）にバインドされるパラメータ（可変長）
     * @return レコード一覧（各行をMap<String, Object>として表現）
     * @throws SQLException SQL構文エラーや実行中のDB例外が発生した場合
     */
    public static List<Map<String, Object>> select(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }


    /**
     * 単一レコードを取得するSQLを実行し、結果をMap形式で返します。
     * 結果が0件の場合は null を返します。
     *
     * <p>1件のみ取得することを前提とした SELECT 用メソッドです。複数行が返ってくるSQLに対しては先頭1件のみを返却します。</p>
     *
     * @param conn   DBコネクション（null不可）
     * @param sql    実行するSQL文（例: "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params プレースホルダにバインドされるパラメータ（可変長）
     * @return 取得されたレコード1件（Map形式）、0件の場合は null
     * @throws SQLException SQL実行時の例外
     */
    public static Map<String, Object> selectOneRowAsMap(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? toMap(rs) : null;
            }
        }
    }

    /**
     * 指定したSQLを実行し、結果セットを1行ずつMap形式に変換してListで返します。
     * 各レコードはMap<String, Object>として返され、リストは順序を保持します。
     *
     * @param conn   使用するDBコネクション（null不可）
     * @param sql    実行するSQL文（例: "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params SQL文のプレースホルダ（?）に対応するバインドパラメータ（可変長）
     * @return クエリ結果のリスト（各要素が1行分のMap）
     * @throws SQLException SQL実行時の例外
     */
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


    /**
     * 指定したSQLを実行してレコードを削除します。
     * SQL文にはプレースホルダ（"?"）を含めることができ、可変長引数として値をバインドできます。
     *
     * @param conn   使用するDBコネクション（null不可）
     * @param sql    実行するDELETE文（例: "DELETE FROM TKC001 WHERE SHORI_TUKI = ?"）
     * @param params SQLのプレースホルダにバインドするパラメータ（可変長）
     * @return 削除されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int delete(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            return stmt.executeUpdate();
        }
    }


    /**
     * 指定されたSQLを実行し、更新されたレコード数を返します。
     * UPDATE文やINSERT文にも使用可能です。
     *
     * @param conn   使用するDBコネクション（null不可）
     * @param sql    実行するSQL文（例: "UPDATE TKC001 SET STATUS = ? WHERE ID = ?"）
     * @param params SQLのプレースホルダにバインドするパラメータ（可変長）
     * @return 実行結果として更新されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int update(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    /**
     * 指定されたテーブルに対して、Mapデータを挿入します。
     * 対象カラムと順序は明示的に columns で指定され、Dialectは自動で判定されます。
     *
     * @param conn    DBコネクション
     * @param table   挿入対象のテーブル名
     * @param data    挿入するデータ（キー=カラム名）
     * @param columns 挿入対象とするカラム名リスト（順序に意味あり）
     * @return 挿入されたレコード件数（通常は1）
     * @throws SQLException SQL実行時の例外
     */
    public static int insertByMap(Connection conn, String table, Map<String, Object> data, List<String> columns) throws SQLException {
        Dialect dialect = detectDialect(conn);
        return insertByMap(conn, table, data, columns, dialect);
    }

    /**
     * 指定されたMapデータを指定テーブルに挿入します。
     * カラム名リストと対応する値はMapから抽出され、プレースホルダにバインドされます。
     *
     * @param conn    使用するDBコネクション（null不可）
     * @param table   挿入先のテーブル名（例: "TKC001"）
     * @param data    挿入するデータ（キーがカラム名、値がバインド値）
     * @param columns 挿入対象とするカラム名リスト（順序必須）
     * @param dialect DB方言（SQL構文の違いに対応）
     * @return 挿入されたレコード件数（通常は1）
     * @throws SQLException SQL実行時の例外
     */
    private static int insertByMap(Connection conn, String table, Map<String, Object> data, List<String> columns, Dialect dialect) throws SQLException {
        String sql = buildInsertSQL(table, columns, dialect);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[] values = columns.stream()
                    .map(col -> data.get(normalizeColumnKey(col, dialect)))  // ← ここで正規化
                    .toArray();
            setParams(ps, values);
            return ps.executeUpdate();
        }
    }


    /**
     * 指定テーブルに対して、MapデータをもとにUPDATE文を実行します。
     * SET句とWHERE句の対象カラムはそれぞれ明示的に指定します。
     * Dialectは内部で自動判定されます。
     *
     * @param conn      DBコネクション
     * @param table     対象テーブル名
     * @param data      更新対象の値を含むMap（キー=カラム名）
     * @param columns   更新対象カラム（SET句）
     * @param whereKeys 条件カラム（WHERE句）
     * @return 更新されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int updateByMap(Connection conn, String table, Map<String, Object> data, List<String> columns, List<String> whereKeys) throws SQLException {
        Dialect dialect = detectDialect(conn);
        return updateByMap(conn, table, data, columns, whereKeys, dialect);
    }


    /**
     * 指定されたMapデータを用いて、テーブル上のレコードを更新します。
     * 更新対象カラムとWHERE条件カラムは明示的に指定します。
     *
     * @param conn      使用するDBコネクション（null不可）
     * @param table     更新対象のテーブル名（例: "TKC001"）
     * @param data      更新に使用するデータ（キー=カラム名、値=バインド値）
     * @param columns   SET句に含める更新対象カラム名リスト
     * @param whereKeys WHERE句に使用するキー列名リスト
     * @param dialect   DB方言（SQL構文差分対応用）
     * @return 更新されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    private static int updateByMap(Connection conn, String table, Map<String, Object> data, List<String> columns, List<String> whereKeys, Dialect dialect) throws SQLException {
        String sql = buildUpdateSQL(table, columns, whereKeys, dialect);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<Object> values = new ArrayList<>();
            for (String col : columns) {
                values.add(data.get(normalizeColumnKey(col, dialect)));
            }
            for (String key : whereKeys) {
                values.add(data.get(normalizeColumnKey(key, dialect)));
            }
            setParams(ps, values.toArray());
            return ps.executeUpdate();
        }
    }

    /**
     * 指定テーブルから、Mapで与えた条件に合致するレコードを削除します。
     * WHERE句に使うカラムを明示し、Dialectは内部で自動判定されます。
     *
     * @param conn      DBコネクション
     * @param table     削除対象のテーブル名
     * @param data      条件データ（キー=カラム名、値=検索値）
     * @param whereKeys WHERE句に使用するカラム名リスト
     * @return 削除されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    public static int deleteByMap(Connection conn, String table, Map<String, Object> data, List<String> whereKeys) throws SQLException {
        Dialect dialect = detectDialect(conn);
        return deleteByMap(conn, table, data, whereKeys, dialect);
    }

    /**
     * 指定されたMapデータをもとに、指定テーブルからWHERE句でレコードを削除します。
     * WHERE句のカラムと値は `whereKeys` に基づいてMapから取得されます。
     *
     * @param conn      DBコネクション（null不可）
     * @param table     削除対象のテーブル名（例: "TKC001"）
     * @param data      WHERE句条件値を持つMap（キー=カラム名、値=条件値）
     * @param whereKeys WHERE句に使用するカラム名リスト
     * @param dialect   DB方言（SQL構文の差分に対応）
     * @return 削除されたレコード件数
     * @throws SQLException SQL実行時の例外
     */
    private static int deleteByMap(Connection conn, String table, Map<String, Object> data, List<String> whereKeys, Dialect dialect) throws SQLException {
        String sql = buildDeleteSQL(table, whereKeys, dialect);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[] values = whereKeys.stream()
                    .map(key -> data.get(normalizeColumnKey(key, dialect)))  // ← 安全に正規化
                    .toArray();
            setParams(ps, values);
            return ps.executeUpdate();
        }
    }

    /**
     * 複数件のデータをバルクインサート（バッチ）でテーブルに挿入します。
     * 各Mapが1行分のデータを表し、columnsで指定したカラム順に値をバインドします。
     *
     * @param conn    DBコネクション（null不可）
     * @param table   挿入対象のテーブル 名（例: "TKC001"）
     * @param list    挿入するレコードリスト（各要素がMap形式）
     * @param columns 挿入対象カラム名リスト（順序に意味あり）
     * @return 成功した挿入件数の合計
     * @throws SQLException SQL実行時の例外
     */
    private static int insertByArray(Connection conn, String table, List<Map<String, Object>> list, List<String> columns) throws SQLException {

        Dialect dialect = detectDialect(conn);
        return insertByArray(conn, table, list, columns, dialect);
    }

    /**
     * 複数件のデータをバルクインサート（バッチ）でテーブルに挿入します。
     * 各Mapが1行分のデータを表し、columnsで指定したカラム順に値をバインドします。
     *
     * @param conn    DBコネクション（null不可）
     * @param table   挿入対象のテーブル 名（例: "TKC001"）
     * @param list    挿入するレコードリスト（各要素がMap形式）
     * @param columns 挿入対象カラム名リスト（順序に意味あり）
     * @param dialect DB方言（SQL構文の差分に対応）
     * @return 成功した挿入件数の合計
     * @throws SQLException SQL実行時の例外
     */
    private static int insertByArray(Connection conn, String table, List<Map<String, Object>> list, List<String> columns, Dialect dialect) throws SQLException {
        String sql = buildInsertSQL(table, columns, dialect);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, Object> data : list) {
                Object[] values = columns.stream()
                        .map(col -> data.get(normalizeColumnKey(col, dialect)))  // ← 安全に正規化
                        .toArray();
                setParams(ps, values);
                ps.addBatch();
            }
            return Arrays.stream(ps.executeBatch()).sum();
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
        for (int i = 0; i < params.length; i++) {
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
     * 指定したテーブルに対して、複数のカラム条件（AND結合）に基づきレコードが存在するかを確認します。
     * <p>
     * 条件に合致するレコードが1件も存在しない場合に {@code true} を返します。
     * </p>
     *
     * <pre>{@code
     * Map<String, Object> whereMap = Map.of(
     *     "SHORI_TUKI", "202507",
     *     "TK_DVS", null  // IS NULL も可能
     * );
     * boolean isEmpty = isTableEmpty(conn, "TKC001", whereMap);
     * }</pre>
     *
     * @param conn      データベース接続（JDBC Connection）
     * @param tableName 対象のテーブル名（例: "TKC001"）
     * @param whereMap  WHERE条件を表すマップ（キー：カラム名、値：バインド値。nullの場合は IS NULL になる）
     * @return レコードが存在しなければ {@code true}、存在すれば {@code false}
     * @throws SQLException SQLの実行またはデータ取得に失敗した場合
     */
    public static boolean isTableEmpty(Connection conn, String tableName, Map<String, Object> whereMap) throws SQLException {
        List<String> whereClauseList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : whereMap.entrySet()) {
            if (entry.getValue() == null) {
                whereClauseList.add(entry.getKey() + " IS NULL");
            } else {
                whereClauseList.add(entry.getKey() + " = ?");
                paramList.add(entry.getValue());
            }
        }

        String whereClause = String.join(" AND ", whereClauseList);
        String sql = String.format("SELECT COUNT(*) AS cnt FROM %s WHERE %s", tableName, whereClause);

        return DbUtil.select(conn, sql, paramList.toArray()).stream().findFirst().map(row -> ((Number) row.get("cnt")).intValue() == 0).orElse(true);
    }


    /**
     * 指定されたテーブルの定義情報を INFORMATION_SCHEMA.COLUMNS から取得し、
     * 引数の valueMap に存在するカラムのみを抽出して返します。
     *
     * <p>主に {@code insertByMap(...)} 用のカラムリスト生成に使用します。</p>
     *
     * @param conn      DBコネクション（SQL Serverに接続済であること）
     * @param tableName 対象テーブル名（例: "TKC001"）
     * @param valueMap  値を保持するMap（キー=カラム名候補）
     * @return テーブルに実在し、かつ valueMap に含まれているカラム名リスト
     * @throws SQLException SQL実行時の例外
     */
    public static List<String> gettableColList(Connection conn, String tableName, Map<String, Object> valueMap) throws SQLException {
        List<String> availableCols = new ArrayList<>();
        Dialect dialect = detectDialect(conn);

        String sql;
        boolean caseSensitive = false;

        switch (dialect) {
            case SQLSERVER:
                sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'dbo'";
                break;
            case POSTGRES:
                sql = "SELECT column_name FROM information_schema.columns WHERE table_name = ?";
                caseSensitive = true; // PostgreSQLは小文字変換される
                break;
            case ORACLE:
                sql = "SELECT column_name FROM user_tab_columns WHERE table_name = ?";
                tableName = tableName.toUpperCase(); // Oracleは大文字で保持される
                break;
            case MYSQL:
            default:
                sql = "SELECT column_name FROM information_schema.columns WHERE table_name = ?";
                break;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    boolean contains;

                    if (caseSensitive) {
                        // PostgreSQL → 小文字化
                        contains = valueMap.containsKey(col.toLowerCase());
                    } else if (dialect == Dialect.ORACLE) {
                        // Oracle → 大文字化
                        contains = valueMap.containsKey(col.toUpperCase());
                    } else {
                        contains = valueMap.containsKey(col);
                    }

                    if (contains) {
                        availableCols.add(col);
                    }
                }
            }
        }

        return availableCols;
    }

    private static String normalizeColumnKey(String key, Dialect dialect) {
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
     * nullを含めるかどうかは `includeNulls` で制御します。
     *
     * @param conn         DBコネクション（null不可）
     * @param tableName    テーブル名（例："TKC001"）
     * @param valueMap     挿入する値のMap（キー=カラム名、値=挿入値）
     * @param includeNulls trueの場合、null値も含めて挿入対象にする
     * @throws SQLException SQL実行時のエラー
     */
    public static void insertByMapEx(Connection conn, String tableName, Map<String, Object> valueMap, boolean includeNulls) throws SQLException {
        DbDialect dialect = DbDialectFactory.createDialect(conn);
        List<String> columns = dialect.getTableColumns(conn, tableName, valueMap);

        List<String> insertCols = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        for (String col : columns) {
            Object val = valueMap.get(col);
            if (val != null || includeNulls) {
                insertCols.add(col);
                params.add(val);
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(", ", insertCols), insertCols.stream().map(c -> "?").collect(Collectors.joining(", ")));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();
        }
    }


    /**
     * 任意のテーブルに対して、指定された値マップを用いてレコードを更新します。
     * includeNulls=true の場合、値が null のカラムも更新対象とします。
     *
     * @param conn         データベース接続
     * @param tableName    更新対象のテーブル名
     * @param valueMap     更新する値のマップ（カラム名→値）
     * @param whereMap     WHERE句の条件（カラム名→値）
     * @param includeNulls null値のカラムも更新する場合は true
     * @return 更新された件数
     * @throws SQLException             SQL実行時に発生した例外
     * @throws IllegalArgumentException 入力値に不備がある場合（例: カラムが空など）
     */
    public static int updateByMapEx(Connection conn, String tableName, Map<String, Object> valueMap, Map<String, Object> whereMap, boolean includeNulls) throws SQLException {
        DbDialect dialect = DbDialectFactory.createDialect(conn);
        List<String> columns = dialect.getTableColumns(conn, tableName, valueMap);

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("カラム情報が取得できませんでした: " + tableName);
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
            throw new IllegalArgumentException("更新対象のカラムが存在しません（nullを含めたか確認してください）: " + tableName);
        }

        List<String> whereClauses = new ArrayList<>();
        for (String col : whereMap.keySet()) {
            whereClauses.add(col + " = ?");
            params.add(whereMap.get(col));
        }

        if (whereClauses.isEmpty()) {
            throw new IllegalArgumentException("WHERE条件が指定されていません。安全のため全件更新は禁止されています。");
        }

        String setClause = String.join(", ", setClauses);
        String whereClause = String.join(" AND ", whereClauses);
        String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, setClause, whereClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps.executeUpdate();
        }
    }


    /**
     * 指定されたSQLを実行し、結果セットの先頭1件をMap形式で返します。
     * 結果が0件の場合は null を返します。複数件が返ってきた場合も先頭の1件のみを返却します。
     *
     * <p>各Mapのキーはカラム名（エイリアス含む）であり、値は対応するカラム値です。</p>
     *
     * @param conn   DBコネクション（null不可）
     * @param sql    実行するSELECT文（例: "SELECT * FROM TKC001 WHERE ID = ?"）
     * @param params プレースホルダにバインドされるパラメータ（可変長）
     * @return 取得された1レコード（Map形式）、該当しなければ null
     * @throws SQLException SQL実行時の例外
     */
    public static Map<String, Object> selectOne(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    Map<String, Object> result = new HashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        result.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    return result;
                }
            }
        }
        return null;
    }


    /**
     * 指定テーブルに対して、WHERE条件に基づく1件取得SQLを自動生成して実行します。
     *
     * @param conn     DBコネクション
     * @param table    対象テーブル名（例: "TKC001"）
     * @param whereMap WHERE句に使用するカラム名と値のマップ
     * @return 該当レコード（1件のみ、存在しなければnull）
     * @throws SQLException SQL実行時の例外
     */
    public static Map<String, Object> selectOneByMap(Connection conn, String table, Map<String, Object> whereMap) throws SQLException {
        if (whereMap == null || whereMap.isEmpty()) {
            throw new IllegalArgumentException("WHERE条件が指定されていません");
        }

        String whereClause = whereMap.keySet().stream().map(key -> key + " = ?").collect(Collectors.joining(" AND "));

        String sql = String.format("SELECT * FROM %s WHERE %s", table, whereClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (String key : whereMap.keySet()) {
                ps.setObject(i++, whereMap.get(key));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    Map<String, Object> row = new HashMap<>();
                    for (int j = 1; j <= meta.getColumnCount(); j++) {
                        row.put(meta.getColumnLabel(j), rs.getObject(j));
                    }
                    return row;
                }
            }
        }

        return null;
    }


    /**
     * 任意のテーブルに対して、指定された WHERE 条件で SELECT を実行し、
     * 結果をリスト形式で返します。
     *
     * @param conn      DBコネクション
     * @param tableName テーブル名（例: "TK_MEMBER"）
     * @param whereMap  WHERE条件（カラム名をキー、値をバインド対象。null指定で IS NULL）
     * @return 行データのリスト（カラム名→値の Map）。該当がなければ空リスト。
     * @throws SQLException SQL実行時の例外
     */
    public static List<Map<String, Object>> selectList(Connection conn, String tableName, Map<String, Object> whereMap) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        List<Object> paramList = new ArrayList<>();

        if (whereMap != null && !whereMap.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            for (Map.Entry<String, Object> entry : whereMap.entrySet()) {
                if (entry.getValue() == null) {
                    conditions.add(entry.getKey() + " IS NULL");
                } else {
                    conditions.add(entry.getKey() + " = ?");
                    paramList.add(entry.getValue());
                }
            }
            sql.append(String.join(" AND ", conditions));
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < paramList.size(); i++) {
                ps.setObject(i + 1, paramList.get(i));
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

                return result;
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
     * Dialectに応じたLIMIT句を生成します。
     *
     * @param dialect RDBMSの種類
     * @param limit   最大件数
     * @return LIMIT句（SQLServerはTOP句として返却）
     */
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
     * 指定されたテーブルに対して、複数のMapレコードを一括でINSERTします。
     * 各Mapは1行のデータを表し、キーがカラム名、値が対応する挿入値です。
     * null値の扱いは `includeNulls` フラグで制御されます。
     *
     * <p>内部的には1件ずつ {@link #insertByMapEx(Connection, String, Map, boolean)} を呼び出して処理します。</p>
     *
     * @param conn         DB接続オブジェクト（null不可）
     * @param table        挿入対象のテーブル名（例: "TKC001"）
     * @param records      挿入するレコードのリスト（各Mapが1行を表す）
     * @param includeNulls trueの場合、Map内のnull値もINSERT対象に含めます
     * @throws SQLException SQL実行時の例外
     */
    public static void insertBatchByMap(Connection conn, String table, List<Map<String, Object>> records, boolean includeNulls) throws SQLException {
        if (records == null || records.isEmpty()) return;
        for (Map<String, Object> record : records) {
            insertByMapEx(conn, table, record, includeNulls);
        }
    }

    /**
     * 単一レコードをMap形式で指定テーブルに挿入します。
     *
     * @param conn   DB接続（null可。nullの場合は {@link DbConfigLoader} から取得）
     * @param table  対象テーブル名
     * @param data   挿入するカラムと値のMap
     * @return 挿入件数（通常は1）
     * @throws SQLException SQL実行時の例外
     */
    public static int insertByMapEx(Connection conn, String table, Map<String, Object> data) throws SQLException, ClassNotFoundException {
        conn = getConnectionIfNull(conn);
        Dialect dialect = detectDialect(conn);
        List<String> columns = new ArrayList<>(data.keySet());
        String sql = buildInsertSQL(table, columns, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, columns.stream().map(data::get).toArray());
            return ps.executeUpdate();
        }
    }

    /**
     * 単一レコードをMap形式で指定テーブルに更新します。
     *
     * @param conn       DB接続（null可。nullの場合は {@link DbConfigLoader} から取得）
     * @param table      対象テーブル名
     * @param data       更新対象のデータ（カラム名→値）
     * @param columns    SET対象カラムリスト（例: Arrays.asList("name", "age")）
     * @param whereKeys  WHERE句で使用するカラムリスト（例: Arrays.asList("id")）
     * @return 更新件数（通常は1）
     * @throws SQLException SQL実行時の例外
     */
    public static int updateByMapEx(Connection conn, String table, Map<String, Object> data,
                                    List<String> columns, List<String> whereKeys) throws SQLException, ClassNotFoundException {
        conn = getConnectionIfNull(conn);
        Dialect dialect = detectDialect(conn);
        String sql = buildUpdateSQL(table, columns, whereKeys, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<Object> values = new ArrayList<>();
            for (String col : columns) values.add(data.get(col));
            for (String key : whereKeys) values.add(data.get(key));
            setParams(ps, values.toArray());
            return ps.executeUpdate();
        }
    }

    /**
     * 指定テーブルから単一レコードをMap形式で削除します。
     *
     * @param conn       DB接続（null可。nullの場合は {@link DbConfigLoader} から取得）
     * @param table      対象テーブル名
     * @param data       WHERE条件として使用するデータ（カラム名→値）
     * @param whereKeys  WHERE句に使うキー（例: Arrays.asList("id")）
     * @return 削除件数（通常は1）
     * @throws SQLException SQL実行時の例外
     */
    public static int deleteByMapEx(Connection conn, String table, Map<String, Object> data,
                                    List<String> whereKeys) throws SQLException, ClassNotFoundException {
        conn = getConnectionIfNull(conn);
        Dialect dialect = detectDialect(conn);
        String sql = buildDeleteSQL(table, whereKeys, dialect);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, whereKeys.stream().map(data::get).toArray());
            return ps.executeUpdate();
        }
    }



}