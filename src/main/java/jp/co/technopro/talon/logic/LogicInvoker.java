package jp.co.technopro.talon.logic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static jp.co.technopro.talon.consts.LogicId.*;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;

public class LogicInvoker {

    public Map<String, Object> execute(Connection conn, String logicId, Map<String, Object> paramMap, String eventId) throws SQLException {
        switch (logicId) {
            case IRYO:
                return new IryolService().run(conn, paramMap, eventId);
            case YOTAKU:
                return new YotakuService().run(conn, paramMap, eventId);
            case COMMON:
                return new Tkc001Service().run(conn, paramMap, eventId);
            default:
                return buildResult(false, "不明なロジックID: " + logicId);
        }
    }
}
