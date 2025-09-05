package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_JGY_KBN_2;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.setTkMemberDto;

public class IryoChkJgyChk extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        TkMemberDto memberDto = setTkMemberDto(conn, getTkNo(), paramDto);
        return chkJgyChk(memberDto);
    }

    /**
     * 事業区分に応じて、対象外（生きがい等）の場合はエラー終了とする。
     * <p>
     * ・{@code JGY_KBN == TK_DVS_JGY_KBN_2} のとき
     * {@code EventResultDto.error("事業区分が生きがいのため対象外です。")} を返す。<br>
     * ・上記以外（会員情報なし／区分未設定を含む）は正常終了（OK）とする。<br>
     * ・トランザクションは呼び出し側で管理する。
     * </p>
     *
     * @param memberDto 会員情報DTO（null の可能性あり）
     * @return 対象外は Error、対象内は OK
     */
    private EventResultDto chkJgyChk(TkMemberDto memberDto) {

        logInfoMethodStart();

        if (paramDto.isTlnIsDelete()) return EventResultDto.ok();
        if (paramDto.isTlnIsUpdate()) return EventResultDto.ok();

        // nullセーフ：会員情報未取得は「判定不可」扱いでOK継続
        if (memberDto == null) return EventResultDto.ok();

        // 事業区分を取得
        String jgyKbn = memberDto.getJgyKbn();

        // 事業区分「null」は、制度改定前の会員
        // 現在の区分体系は「0」と同義なので「null → 0」と補正
        if (jgyKbn == null) {
            jgyKbn = "0";
            memberDto.setJgyKbn(jgyKbn);
        }

        // 事業区分 = 2（生きがい）は対象外
        if (TK_DVS_JGY_KBN_2.equals(jgyKbn)) {
            return EventResultDto.error(
                    "特別会員番号 : " + getTkNo() + " 事業区分が生きがいのため対象外です。"
            );
        }

        // それ以外（null, 0, 1）は対象
        return EventResultDto.ok();
    }
}
