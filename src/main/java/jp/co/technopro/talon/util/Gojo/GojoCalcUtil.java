package jp.co.technopro.talon.util.Gojo;

import java.math.BigDecimal;

public class GojoCalcUtil {

    /**
     * 任意の {@code Object} を整数値（{@code int}）として取得します。
     * <p>
     * 対象が {@link Number} 型であれば {@code intValue()} を使用し、
     * 文字列などの場合は {@code Integer.parseInt()} により変換を試みます。
     * 数値に変換できない場合や {@code null} の場合は {@code 0} を返します。
     * </p>
     *
     * @param value 任意のオブジェクト
     * @return 整数値（変換失敗時や {@code null} の場合は 0）
     */
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

    /**
     * 任意の {@code Object} を {@link BigDecimal} に変換します。
     * <p>
     * 対象が {@link Number} 型や {@link BigDecimal} の場合はそのまま使用し、
     * 文字列などの場合は {@code new BigDecimal(String)} によって変換を試みます。
     * {@code null} の場合は {@code BigDecimal.ZERO} を返します。
     * </p>
     *
     * @param value 任意のオブジェクト
     * @return {@code BigDecimal} 値（変換失敗時は例外、{@code null} の場合は {@code BigDecimal.ZERO}）
     * @throws NumberFormatException 数値変換に失敗した場合
     */
    public static BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }

    /**
     * 2つの {@link BigDecimal} を減算します。
     * 引数が {@code null} の場合は {@code BigDecimal.ZERO} として扱います。
     *
     * @param a 減算対象の値（被減数）
     * @param b 減算する値（減数）
     * @return a - b の結果
     */
    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b);
    }

    /**
     * 2つの {@link BigDecimal} を加算します。
     * 引数が {@code null} の場合は {@code BigDecimal.ZERO} として扱います。
     *
     * @param a 加算対象の値
     * @param b 加算する値
     * @return a + b の結果
     */
    public static BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.add(b);
    }

    /**
     * 2つの {@link BigDecimal} を乗算します。
     * 引数が {@code null} の場合は {@code BigDecimal.ZERO} として扱います。
     *
     * @param a 乗算対象の値
     * @param b 乗算する値
     * @return a × b の結果
     */
    public static BigDecimal safeMultiply(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.multiply(b);
    }

    /**
     * 2つの {@link BigDecimal} を除算します。
     * ゼロ除算や {@code null} を安全に扱い、ゼロで割った場合は {@code BigDecimal.ZERO} を返します。
     *
     * @param a 被除数
     * @param b 除数
     * @return a ÷ b の結果（b が null またはゼロの場合は 0）
     */
    public static BigDecimal safeDivide(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null || BigDecimal.ZERO.compareTo(b) == 0) return BigDecimal.ZERO;
        return a.divide(b, 10, BigDecimal.ROUND_HALF_UP); // scale=10, 丸めあり
    }
}
