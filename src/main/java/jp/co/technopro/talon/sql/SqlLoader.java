package jp.co.technopro.talon.sql;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SqlLoader {

    private final Map<String, String> sqlMap = new HashMap<>();

    /**
     * コンストラクタ：クラスパス上のXMLファイルを読み込む
     * @param xmlPath クラスパス相対のXMLファイルパス（例："sql/user-sql.xml"）
     */
    public SqlLoader(String xmlPath) {
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

                // 行ごとに整形：各行の先頭インデントを削除して改行維持
                String formattedSql = formatSqlPreserveLines(rawSql);

                if (sqlMap.containsKey(id)) {
                    throw new RuntimeException("SQL ID が重複しています: " + id);
                }

                sqlMap.put(id, formattedSql);
                System.out.println("Loaded SQL [" + id + "]:\n" + formattedSql);
            }

        } catch (Exception e) {
            throw new RuntimeException("SQLファイル読み込みエラー: " + xmlPath, e);
        }
    }

    /**
     * 指定したIDのSQLを取得
     * @param id SQLのID
     * @return 該当SQL文（なければnull）
     */
    public String get(String id) {
        return sqlMap.get(id);
    }

    /**
     * インデントを整えて改行を保ったままSQLを整形します。
     * 例: 8文字以上のインデントを削除（お好みで調整可）
     *
     * @param sql CDATAなどから得た元のSQL文字列
     * @return 整形済みのSQL
     */
    private static String formatSqlPreserveLines(String sql) {
        StringBuilder sb = new StringBuilder();
        String[] lines = sql.split("\n");
        for (String line : lines) {
            // 行頭の8スペース（またはタブ等）を除去（必要に応じて調整）
            sb.append(line.replaceFirst("^\\s{8}", "").stripTrailing()).append("\n");
        }
        return sb.toString().trim(); // 最後の改行は不要ならtrim()
    }
}
