package jp.co.technopro.talon.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MySqlDialect implements DbDialect {

    @Override
    public List<String> getTableColumns(Connection conn, String tableName, Map<String, Object> valueMap) throws SQLException {
        List<String> result = new ArrayList<>();
        String schema = conn.getCatalog(); // MySQLではカタログ名がスキーマ
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, schema);
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
        return "MySQL";
    }
}
