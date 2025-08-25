package jp.co.technopro.talon.logic.Gojo.sime;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.henko.HenkoSimeRenkeiLogic;
import jp.co.technopro.talon.logic.Gojo.shinki.ShinkiSimeRenkeiLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.CalcYotakukinLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.CreateYotakuSokinData;
import jp.co.technopro.talon.logic.Gojo.yotaku.YotakuUpdateSimeFinal;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.sql.Connection;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;

public final class GojoSimeRenkeiDispatcher {

    private GojoSimeRenkeiDispatcher() {
    }

    /**
     * Nashorn から渡された DTO をもとに TK_DVS のみで分岐します。
     * <p>
     * TK_DVS は <b>TARGET_DATA</b> を第一優先、なければ <b>CONDITION_DATA</b> を参照します。
     * トランザクション（commit/rollback）は呼び出し元で管理してください。
     */
    public static EventResultDto dispatch(Connection conn, TalonParamDto paramDto) {
        if (paramDto == null) {
            return EventResultDto.error("paramDto が未指定です。");
        }
        final Map<String, Object> cond = paramDto.getConditionData();
        if (cond == null) {
            return EventResultDto.error("conditionData が未設定です。");
        }

        // 会社コード（プロジェクト方針で必須）
        if (paramDto.getCompanyCode() == null || paramDto.getCompanyCode().isBlank()) {
            return EventResultDto.error("会社コードが未指定です。");
        }

        // 処理月チェック
        final String shoriTuki = SafeMapAccessUtil.getString(cond, MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return EventResultDto.error(MSG_NON_SHORI_TUKI);
        }

        // --- TK_DVS 取得：TARGET_DATA 優先、なければ CONDITION_DATA ---
        final Map<String, Object> target = paramDto.getTargetData(); // null 許容
        final String tkDvs = firstNonBlank(
                SafeMapAccessUtil.getString(target, MAP_KEY_TK_DVS),
                SafeMapAccessUtil.getString(cond, MAP_KEY_TK_DVS)
        );

        if (tkDvs == null) {
            return EventResultDto.error("TK_DVS が未指定です。");
        }

        final String simeDvs = SafeMapAccessUtil.getString(target, MAP_KEY_SIME_STATUS);

        // --- TK_DVS だけで分岐 ---
        switch (tkDvs) {
            case TK_DVS_SHINKI:
                return new ShinkiSimeRenkeiLogic().run(conn, paramDto);

            case TK_DVS_HENKO:
                return new HenkoSimeRenkeiLogic().run(conn, paramDto);

            case TK_DVS_YOTAKU:

                switch (simeDvs) {
                    case TK_DVS_SIME_STATUS_1:
                        return new CreateYotakuSokinData().run(conn, paramDto);
                    case TK_DVS_SIME_STATUS_2:
                        return new YotakuUpdateSimeFinal().run(conn, paramDto);
                    default:
                        return EventResultDto.ok();
                }


            default:
                return EventResultDto.error("未対応のTK_DVSです: " + tkDvs);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
