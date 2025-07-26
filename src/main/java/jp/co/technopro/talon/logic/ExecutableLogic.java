package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.dto.TalonParamDto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface ExecutableLogic {
    Map<String, Object> run(Connection conn, TalonParamDto paramDto) throws SQLException;
}
