package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.ExecutableLogic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static jp.co.technopro.talon.consts.EventId.HENKO_HON_TOUROKU;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;


public class IryolService implements ExecutableLogic {

    public Map<String, Object> run(Connection conn, TalonParamDto paramDto) throws SQLException {

        String eventId  = paramDto.getEventId();
        switch (eventId) {

            case HENKO_HON_TOUROKU:
                return buildResult(false, "未対応のイベントID: " + eventId);

            default:
                return buildResult(false, "未対応のイベントID: " + eventId);
        }

    }


}

