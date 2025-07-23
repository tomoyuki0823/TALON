package jp.co.technopro.talon.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OracleDialect implements DbDialect {

    @Override
    public List<String> getTableColumns(Connection conn, String tableName, Map<String, Object> valueMap) throws SQLException {
        List<String> result = new ArrayList<>();
        String owner = conn.getMetaData().getUserName();
        String sql = "SELECT COLUMN_NAME FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ? AND OWNER = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            ps.setString(2, owner.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    if (valueMap.containsKey(col)) {
                        result.add(col);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getProductName() {
        return "Oracle";
    }
}
