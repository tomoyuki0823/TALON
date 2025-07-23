package jp.co.technopro.talon.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GojoUtil {


    public static Map<String, Object> getTkMember(Connection conn, String tk_no) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TK_MEMBER WHERE TK_NO = ?", tk_no);

    }

    public static Map<String, Object> getTkShiharai(Connection conn, String tk_no) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TK_SHIHARAI WHERE TK_NO = ?", tk_no);

    }

    /**
     * 任意のObjectからBigDecimalに変換するユーティリティ。
     */
    public static BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }

    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b);
    }


    public static Map<String, Object> getSime(Connection conn, String shoriTuki) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?", shoriTuki);

    }

    public static void insTkYotaku(Connection conn, String tk_no, String shoriTuki) throws SQLException {

        Map<String, Object> insMap = new HashMap<>();
        insMap.put("TK_NO", tk_no);
        insMap.put("SHORI_TUKI", shoriTuki);

        DbUtil.insertByMap(conn, "TK_YOTAKU", insMap,
                Arrays.asList("TK_NO", "SHORI_TUKI"),
                DbUtil.Dialect.SQLSERVER);

    }

    public static int getInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}
