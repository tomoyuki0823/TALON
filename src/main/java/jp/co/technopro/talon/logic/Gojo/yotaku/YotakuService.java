package jp.co.technopro.talon.logic.gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.logic.Gojo.yotaku.CalcYotakukinLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.SetYotakuInitLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.SetYotakukinYoteiLogic;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;

/**
 * 預託金ロジックエントリーポイント。
 * 各イベントIDに対応するロジックにディスパッチします。
 */
public class YotakuService extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        String eventId = paramDto.getEventId();
        try {
            switch (eventId) {
                case YOTAKU_YOTEI:
                    return new SetYotakukinYoteiLogic().run(conn, paramDto);
                case INIT_INFO:
                    return new SetYotakuInitLogic().run(conn, paramDto);
                case CALC_YOTAKUKIN:
                    return new CalcYotakukinLogic().run(conn, paramDto);
                default:
                    return EventResultDto.error("未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return EventResultDto.error("処理中にエラーが発生しました: " + e.getMessage());
        }
    }
}
