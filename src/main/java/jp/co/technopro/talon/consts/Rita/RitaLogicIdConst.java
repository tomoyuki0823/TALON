package jp.co.technopro.talon.consts.Rita;

/**
 * 業務ロジック識別子（Logic ID）定数クラス。
 * {@link LogicInvoker} などで使用される識別キーを定義します。
 */
public final class RitaLogicIdConst {

    private RitaLogicIdConst() {} // インスタンス化禁止

    /** 新規登録（ShinkiService） */
    public static final String SHINKI = "SHINKI";

    /** 変更管理（HenkoService） */
    public static final String HENKO = "HENKO";

    /** 医療系処理（IryoService） */
    public static final String IRYO = "IRYO";

    /** 与託金処理（YotakuService） */
    public static final String YOTAKU = "YOTAKU";

    /** 長寿祝金（ChojuService） */
    public static final String CHOJU = "CHOJU";

    /** 宿泊(道内)（StayDounaiService） */
    public static final String STAY_DOUNAI = "STAY_DOUNAI";

    /** 宿泊(道内)（StayDougaiService） */
    public static final String STAY_DOUGAI = "STAY_DOUGAI";

    /** 共通処理（TKC001） */
    public static final String COMMON = "COMMON";

}
