package jp.co.technopro.talon.logic.Gojo.yotaku;

import jp.co.technopro.logger.TalonLogger;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringCheckUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_TK_OBJ;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_YOTAKU;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;
import static jp.co.technopro.talon.util.common.DbUtil.isTableEmpty;

public class SetYotakuInitLogic extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic()  {

        String tkNo = getTkNo();
        String shoriTuki = getShoriTuki();

        if (StringCheckUtil.isNullOrEmpty(tkNo) || StringCheckUtil.isNullOrEmpty(shoriTuki)) {
            return EventResultDto.error(MSG_NON_TK_OBJ);
        }

        if (isYotakuAlreadyRegistered(tkNo, shoriTuki, paramDto.getCompanyCode())) {
            return EventResultDto.ok();
        }

        insTkYotaku(conn, paramDto.getCompanyCode(), tkNo, shoriTuki);
        TalonLogger.logInfo(paramDto, "預託情報登録完了");

        return EventResultDto.ok();
    }

    private boolean isYotakuAlreadyRegistered(String tkNo, String shoriTuki, String companyCd)  {
        Map<String, Object> where = Map.of(
                MAP_KEY_TK_NO, tkNo,
                MAP_KEY_SHORI_TUKI, shoriTuki
        );
        return !isTableEmpty(conn, TABLE_TK_YOTAKU, where, companyCd);
    }
}
