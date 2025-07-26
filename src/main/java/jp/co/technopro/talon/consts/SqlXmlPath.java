package jp.co.technopro.talon.consts;

public class SqlXmlPath {

    private SqlXmlPath() {} // インスタンス化禁止

    /** 預託金関連SQL（YotakuService 用） */
    public static final String SQL_YOTAKU = "sql/yotaku-sql.xml";

    /** 初期登録・現職チェック（ShinkiService 用） */
    public static final String SQL_SHINKI = "sql/shinki-sql.xml";

    /** 締データ作成（SQL_COMMON 用） */
    public static final String SQL_COMMON = "sql/common.xml";
}
