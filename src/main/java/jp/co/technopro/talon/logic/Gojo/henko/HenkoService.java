package jp.co.technopro.talon.logic.Gojo.henko;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.util.common.MapCheckUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;

public class HenkoService implements ExecutableLogic {

    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws Exception {

      return null;

    }




}