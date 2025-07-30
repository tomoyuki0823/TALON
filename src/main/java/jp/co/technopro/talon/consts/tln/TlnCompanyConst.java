package jp.co.technopro.talon.consts.tln;

/**
 * 会社コード定数クラス。
 * <p>
 * 各種処理で使用する会社コード（COMPANY_CD）を定義する。
 */
public final class TlnCompanyConst {

    private TlnCompanyConst() {
        // インスタンス化禁止
    }

    /** 互助会けの会社コード */
    public static final String COMPANY_CD_RITA = "Rita";

    /** RITA向けの会社コード */
    public static final String COMPANY_CD_GOJO = "Gojo";

    /** 古河AS向けの会社コード */
    public static final String COMPANY_CD_FURUKAWA_AS = "furukawa";

    /** NSKの会社コード */
    public static final String COMPANY_CD_NSK = "Nsk";

    /** 共通処理用の会社コード */
    public static final String COMPANY_CD_COMMON = "Common";
}
