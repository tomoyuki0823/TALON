package jp.co.technopro.talon.logic.Gojo.shinki;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SHINKI;
import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_GOJYO_TAIKAI_CD;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_DUPLICATE_GENSYOKU;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_GEN_T_KAIIN;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_SHINKI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;
import static jp.co.technopro.talon.util.Gojo.GojoLogicUtil.getShoriTukiFromConditionData;
import static jp.co.technopro.talon.util.common.DbUtil.isTableEmpty;

public class ShinkiService implements ExecutableLogic {

    @Override
    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws SQLException {

        return null;
    }
}
