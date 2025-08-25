package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.Gojo.GojoMembershipCheckUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_RYOYO_NENGETU;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_ZOKU;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.setTkMemberDto;

public class IryoChkRyoyoShiki extends GojoAbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        logInfoMethodStart();

        if (paramDto.isTlnIsDelete()) return EventResultDto.ok();
        if (paramDto.isTlnIsUpdate()) return EventResultDto.ok();
        // 先に targetData から必要最小限だけ取り出す（null-safe アクセサ推奨）
        Map<String, Object> map = getTargetData();
        String zoku = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);

        // 本人/配偶者 以外はここで即判定終了（DBアクセスしない）
        if (!"0".equals(zoku) && !"1".equals(zoku)) {
            return EventResultDto.error("非認定配偶者は除外です。");
        }

        // 必要な場合のみ会員情報をロード
        TkMemberDto memberDto = setTkMemberDto(conn, getTkNo(), paramDto);

        // null セーフ（取得失敗でも落とさない設計）
        if (memberDto == null) {
            return EventResultDto.ok();
        }

        return chkRyoDtChk(memberDto, zoku, ryoyoNengetu);
    }

    /**
     * 療養年月が在籍期間外かをチェックし、該当時にフラグを静かに更新する（画面は常に正常継続）。
     * <p>
     * ルール：
     * <ul>
     *   <li>加入年月の前月以前 → {@code KANYU_MAE_FLG = FLG_ON}</li>
     *   <li>退会年月の翌月以降 → {@code TAIKAIGO_FLG = FLG_ON}</li>
     *   <li>入力不正（yyyyMM不正や和暦不正など）はフラグ対象にせずスルー</li>
     *   <li>トランザクションは呼び出し側で管理（本メソッドでは commit/rollback しない）</li>
     * </ul>
     * ログは出力しません（TALONログとの二重回避）。
     *
     * @param memberDto 会員情報DTO（加入・退会日の和暦文字列を保持）
     * @return 常に {@link EventResultDto#ok()}
     */
    private EventResultDto chkRyoDtChk(TkMemberDto memberDto, String zoku, String ryoyoNengetu) {
        String taikai_dt = null;
        String kanyu_dt  = null;

        if ("0".equals(zoku)) {             // 本人
            taikai_dt = memberDto.getHonTaikaiSeinengapi();
            kanyu_dt  = memberDto.getHonKanyuSeinengapi();
        } else {                            // 配偶者（"1"）
            taikai_dt = memberDto.getHaiTaikaiSeinengapi();
            kanyu_dt  = memberDto.getHaiKanyubi();
        }

        GojoMembershipCheckUtil.Result res =
                GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(ryoyoNengetu, kanyu_dt, taikai_dt);

        if (res == GojoMembershipCheckUtil.Result.INPUT_INVALID) {
            return EventResultDto.ok();
        }
        if (res == GojoMembershipCheckUtil.Result.BEFORE_KANYU_PREV) {
            return EventResultDto.error("療養年月が未入会時となっているため、給付対象外です。");
        }
        if (res == GojoMembershipCheckUtil.Result.AFTER_TAIKAI_NEXT) {
            return EventResultDto.error("療養年月が退会後となっているため、給付対象外です。");
        }
        return EventResultDto.ok();
    }
}
