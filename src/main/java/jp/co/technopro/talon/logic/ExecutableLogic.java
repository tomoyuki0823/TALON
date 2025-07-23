package jp.co.technopro.talon.logic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface ExecutableLogic {
    void run(Connection conn, Map<String, Object> params, String eventId) throws SQLException;
}
