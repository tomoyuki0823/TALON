package jp.co.technopro.talon.logic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class LogicInvoker {

    public void execute(Connection conn, String logicId, Map<String, Object> paramMap, String eventId) throws SQLException {
        switch (logicId) {
            case "IRYO":
                new IryolService().run(conn, paramMap, eventId);
                break;

            case "YOTAKU":
                new YotakuService().run(conn, paramMap, eventId);
                break;
            case "COMMON":
                new Tkc001Service().run(conn, paramMap, eventId);
                break;

            default:
                throw new IllegalArgumentException("不明なロジックID: " + logicId);
        }
    }
}

