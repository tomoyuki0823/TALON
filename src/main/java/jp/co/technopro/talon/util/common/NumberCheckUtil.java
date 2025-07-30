package jp.co.technopro.talon.util.common;

import java.math.BigDecimal;

/**
 * 数値の判定を行うユーティリティクラスです。
 * null安全なチェックや正負・範囲の判定などを提供します。
 */
public class NumberCheckUtil {

    /**
     * null または 0 の場合に true を返します。
     *
     * @param number 判定対象の数値
     * @return null または 0 の場合 true
     */
    public static boolean isZero(Number number) {
        if (number == null) return true;
        return new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 正の数（> 0）の場合に true を返します。
     *
     * @param number 判定対象の数値
     * @return 正の値であれば true
     */
    public static boolean isPositive(Number number) {
        if (number == null) return false;
        return new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 負の数（< 0）の場合に true を返します。
     *
     * @param number 判定対象の数値
     * @return 負の値であれば true
     */
    public static boolean isNegative(Number number) {
        if (number == null) return false;
        return new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 2つの数値が等しいかどうかを判定します。
     * null同士の場合は true、片方のみ null の場合は false を返します。
     *
     * @param a 数値a
     * @param b 数値b
     * @return 等しければ true
     */
    public static boolean isEqual(Number a, Number b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
    }

    /**
     * 指定した範囲内に値が含まれているかを判定します（両端含む）。
     * null は常に false を返します。
     *
     * @param target 判定対象の数値
     * @param min    最小値（含む）
     * @param max    最大値（含む）
     * @return 範囲内であれば true
     */
    public static boolean isBetween(Number target, Number min, Number max) {
        if (target == null || min == null || max == null) return false;
        BigDecimal val = new BigDecimal(target.toString());
        return val.compareTo(new BigDecimal(min.toString())) >= 0 &&
                val.compareTo(new BigDecimal(max.toString())) <= 0;
    }

    /**
     * 整数かどうかを判定します。
     *
     * @param number 数値
     * @return 小数点以下がなければ true
     */
    public static boolean isInteger(Number number) {
        if (number == null) return false;
        BigDecimal bd = new BigDecimal(number.toString());
        return bd.stripTrailingZeros().scale() <= 0;
    }

    /**
     * 文字列が数値としてパース可能かを判定します。
     *
     * @param str 入力文字列
     * @return 数値として解釈可能なら true
     */
    public static boolean isParsable(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
