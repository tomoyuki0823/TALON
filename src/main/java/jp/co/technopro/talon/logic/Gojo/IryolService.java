package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;

import java.sql.Connection;
import java.sql.SQLException;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.HENKO_HON_TOUROKU;


public class IryolService implements ExecutableLogic {

    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws SQLException {

        String eventId  = paramDto.getEventId();
        switch (eventId) {

            case HENKO_HON_TOUROKU:
                return EventResultDto.ok();

            default:
                return EventResultDto.error("未対応のイベントID: " + eventId);
        }

    }


}

