package jp.co.technopro.talon.consts.Rita;

public class RitaSqlXmlPathConst {

    private RitaSqlXmlPathConst() {} // インスタンス化禁止

    /** 預託金関連SQL（YotakuService 用） */
    public static final String SQL_GOJO_YOTAKU = "gojo-yotaku-sql.xml";

    /** 初期登録・現職チェック（ShinkiService 用） */
    public static final String SQL_GOJO_SHINKI = "gojo-shinki-sql.xml";

    /** 締データ作成（SQL_COMMON 用） */
    public static final String SQL_GOJO_COMMON = "gojo-common.xml";
}
