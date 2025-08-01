package jp.co.technopro.talon.util.common;

import java.math.BigDecimal;

public class MapUtil {

    /**
     * ObjectをBigDecimalへ変換（nullセーフ）。
     */
    public static BigDecimal getBigDecimal(Object val) {
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
