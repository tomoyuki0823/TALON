package jp.co.technopro.talon.sql.common;

import jp.co.technopro.logger.TpiLogger;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL定義XMLファイルを読み込むローダークラス（拡張可能な基底クラス）
 */
public class SqlLoader {

    private static final TpiLogger log = TpiLogger.getLogger(SqlLoader.class);
    protected final Map<String, String> sqlMap = new HashMap<>();

    /**
     * クラスパス上のXMLを読み込み、SQLをID付きで保持します。
     *
     * @param xmlPath XMLファイルのクラスパス相対パス
     */
    public SqlLoader(String xmlPath) {
        loadFromClasspath(xmlPath);
    }

    protected void loadFromClasspath(String xmlPath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(xmlPath)) {
            if (is == null) {
                throw new RuntimeException("SQLファイルが見つかりません: " + xmlPath);
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList nodes = doc.getElementsByTagName("select");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                String id = e.getAttribute("id").trim();
                String rawSql = e.getTextContent();
                String formattedSql = formatSqlPreserveLines(rawSql);

                if (sqlMap.containsKey(id)) {
                    throw new RuntimeException("SQL ID が重複しています: " + id);
                }

                sqlMap.put(id, formattedSql);

                // ▼ INFO → DEBUG に変更（設定で非表示にできるように）
                log.debug("Loaded SQL [" + id + "]:\n" + formattedSql);
            }

        } catch (Exception e) {
            throw new RuntimeException("SQLファイル読み込みエラー: " + xmlPath, e);
        }
    }

    public String get(String id) {
        return sqlMap.get(id);
    }

    public boolean contains(String id) {
        return sqlMap.containsKey(id);
    }

    protected static String formatSqlPreserveLines(String sql) {
        StringBuilder sb = new StringBuilder();
        String[] lines = sql.split("\n");
        for (String line : lines) {
            sb.append(line.replaceFirst("^\\s{8}", "").stripTrailing()).append("\n");
        }
        return sb.toString().trim();
    }
}
