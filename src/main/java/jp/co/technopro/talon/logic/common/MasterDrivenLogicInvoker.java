package jp.co.technopro.talon.logic.common;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class MasterDrivenLogicInvoker {

    /**
     * イベントIDに基づきマスタ定義を参照して該当Javaロジックを呼び出します。
     *
     * @param conn     DB接続
     * @param paramMap パラメータ（FUNC_ID, eventId を含む必要あり）
     * @return 処理結果（Map形式）
     * @throws Exception エラー発生時
     */
    public Map<String, Object> execute(Connection conn, Map<String, Object> paramMap) throws Exception {
        String funcId = (String) paramMap.get("FUNC_ID");
        String eventId = (String) paramMap.get("eventId");

        if (funcId == null || eventId == null) {
            throw new IllegalArgumentException("FUNC_ID または eventId が null です");
        }

        String sql = ""
                + "SELECT j.CLASS_NAME, j.METHOD_NAME "
                + "FROM TPI_M_FUNC_EVENT f "
                + "JOIN TPI_M_JAVA_LOGIC j ON f.LOGIC_ID = j.LOGIC_ID "
                + "WHERE f.FUNC_ID = ? AND f.EVENT_ID = ? AND j.IS_ACTIVE = 1 "
                + "ORDER BY f.DISP_ORDER";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, funcId);
            ps.setString(2, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;

                while (rs.next()) {
                    found = true;
                    String className = rs.getString("CLASS_NAME");
                    String methodName = rs.getString("METHOD_NAME");

                    Class<?> clazz = Class.forName(className);
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    Method method = clazz.getMethod(methodName, Connection.class, Map.class);
                    method.invoke(instance, conn, paramMap);
                }

                if (!found) {
                    throw new IllegalStateException("対応するロジックがマスタに定義されていません: FUNC_ID=" + funcId + ", EVENT_ID=" + eventId);
                }
            }
        }

        return Map.of("success", true);
    }
}
