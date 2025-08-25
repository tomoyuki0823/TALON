package jp.co.technopro.talon.sql.common;

import jp.co.technopro.logger.TpiLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * SQL定義XMLファイルを読み込むローダークラス。
 * - Payara/JakartaEE などのアプリサーバを考慮して TCCL を優先
 * - 先頭スラッシュ有無の両方を試行
 * - Windows/Unix 混在に備えパス区切りを正規化
 */
public class SqlLoader {

    private static final TpiLogger log = TpiLogger.getLogger(SqlLoader.class);
    protected final Map<String, String> sqlMap = new HashMap<>();

    /**
     * クラスパス上の XML を読み込み、SQL を ID 付きで保持します。
     * 例: "sql/common/common.xml"
     *
     * @param xmlPath XMLファイルのクラスパス相対パス
     */
    public SqlLoader(String xmlPath) {
        loadFromClasspath(xmlPath);
    }

    /**
     * クラスパスから XML を読み込む。
     * TCCL → このクラスのCL → Class相対 の順で探索し、
     * 先頭スラッシュ 有/無 の両方を試す。
     */
    protected void loadFromClasspath(String xmlPath) {
        Objects.requireNonNull(xmlPath, "xmlPath");

        // パス正規化（\ → /、先頭スラッシュは外して保持）
        final String pNoSlash = xmlPath.replace("\\", "/").replaceFirst("^/", "");
        final String pWithSlash = "/" + pNoSlash;

        InputStream is = null;

        // 1) Thread Context ClassLoader（アプリサーバ下では最優先）
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null) {
            is = tccl.getResourceAsStream(pNoSlash);                 // "sql/common/common.xml"
            if (is == null) is = tccl.getResourceAsStream(pWithSlash.substring(1)); // "/..." → "..."
        }

        // 2) このクラスの ClassLoader（従来の実装）
        if (is == null) {
            ClassLoader cl = getClass().getClassLoader();
            if (cl != null) {
                is = cl.getResourceAsStream(pNoSlash);
                if (is == null) is = cl.getResourceAsStream(pWithSlash.substring(1));
            }
        }

        // 3) Class 相対（Class#getResourceAsStream は基本「先頭/あり」でルート起点）
        if (is == null) {
            is = getClass().getResourceAsStream(pWithSlash);
            if (is == null) is = getClass().getResourceAsStream("/" + pNoSlash);
        }

        if (is == null) {
            String cl1 = (tccl != null) ? tccl.getClass().getName() : "null";
            String cl2 = (getClass().getClassLoader() != null) ? getClass().getClassLoader().getClass().getName() : "null";
            throw new RuntimeException(
                    "SQLファイル読み込みエラー: " + xmlPath +
                            " (tried: '" + pNoSlash + "', '" + pWithSlash +
                            "', TCCL=" + cl1 + ", ThisCL=" + cl2 + ")"
            );
        }

        try (InputStream autoClose = is) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(autoClose);
            NodeList nodes = doc.getElementsByTagName("select");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                String id = e.getAttribute("id").trim();
                String rawSql = e.getTextContent();
                String formattedSql = formatSqlPreserveLines(rawSql);

                if (id.isEmpty()) {
                    throw new RuntimeException("SQL ID が空です（index=" + i + "）: " + xmlPath);
                }
                if (sqlMap.containsKey(id)) {
                    throw new RuntimeException("SQL ID が重複しています: " + id + " (" + xmlPath + ")");
                }

                sqlMap.put(id, formattedSql);
                log.debug("Loaded SQL [" + id + "] from " + pNoSlash + ":\n" + formattedSql);
            }
        } catch (Exception e) {
            throw new RuntimeException("SQLファイル読み込みエラー: " + xmlPath, e);
        }
    }

    /** SQL を ID で取得 */
    public String get(String id) {
        return sqlMap.get(id);
    }

    /** ID が存在するか */
    public boolean contains(String id) {
        return sqlMap.containsKey(id);
    }

    /** XML のインデントを壊さずに改行を残して整形 */
    protected static String formatSqlPreserveLines(String sql) {
        if (sql == null || sql.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = sql.split("\n");
        for (String line : lines) {
            // 先頭8スペースのヒゲ削り＋行末空白除去（必要に応じて調整）
            sb.append(line.replaceFirst("^\\s{8}", "").stripTrailing()).append("\n");
        }
        return sb.toString().trim();
    }
}
