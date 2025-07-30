package jp.co.technopro.talon.util.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;

/**
 * 数値計算を行うユーティリティクラスです。
 * null安全な加減乗除や合計・平均・丸め処理などを提供します。
 */
public class NumberCalcUtil {

    /**
     * null 安全な加算を行います。
     *
     * @param a 加算元の数値
     * @param b 加算対象の数値
     * @return a + b（null は 0 として扱う）
     */
    public static BigDecimal safeAdd(Number a, Number b) {
        BigDecimal aVal = a == null ? BigDecimal.ZERO : new BigDecimal(a.toString());
        BigDecimal bVal = b == null ? BigDecimal.ZERO : new BigDecimal(b.toString());
        return aVal.add(bVal);
    }

    /**
     * null 安全な減算を行います。
     *
     * @param a 被減数
     * @param b 減数
     * @return a - b（null は 0 として扱う）
     */
    public static BigDecimal safeSubtract(Number a, Number b) {
        BigDecimal aVal = a == null ? BigDecimal.ZERO : new BigDecimal(a.toString());
        BigDecimal bVal = b == null ? BigDecimal.ZERO : new BigDecimal(b.toString());
        return aVal.subtract(bVal);
    }

    /**
     * null 安全な乗算を行います。
     *
     * @param a 被乗数
     * @param b 乗数
     * @return a * b（null は 0 として扱う）
     */
    public static BigDecimal safeMultiply(Number a, Number b) {
        if (a == null || b == null) return BigDecimal.ZERO;
        return new BigDecimal(a.toString()).multiply(new BigDecimal(b.toString()));
    }

    /**
     * null 安全な除算を行います。
     * 除数が 0 または null の場合は Optional.empty を返します。
     *
     * @param a 被除数
     * @param b 除数
     * @param scale 小数点以下の桁数
     * @return a / b（Optional）
     */
    public static Optional<BigDecimal> safeDivide(Number a, Number b, int scale) {
        if (a == null || b == null) return Optional.empty();
        BigDecimal divisor = new BigDecimal(b.toString());
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return Optional.empty();
        BigDecimal dividend = new BigDecimal(a.toString());
        return Optional.of(dividend.divide(divisor, scale, RoundingMode.HALF_UP));
    }

    /**
     * 任意の小数点以下の桁数に四捨五入します。
     *
     * @param value 対象の値
     * @param scale 小数点以下桁数
     * @return 丸め後の値
     */
    public static BigDecimal roundToDecimal(BigDecimal value, int scale) {
        if (value == null) return BigDecimal.ZERO;
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 合計値を求めます。
     * null や null 要素はスキップします。
     *
     * @param values 数値コレクション
     * @return 合計値（空の場合は 0）
     */
    public static BigDecimal sum(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        return values.stream()
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 平均値を求めます。
     * null や null 要素は除外します。
     *
     * @param values 数値コレクション
     * @return 平均値（Optional）
     */
    public static Optional<BigDecimal> average(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) return Optional.empty();
        long count = values.stream().filter(v -> v != null).count();
        if (count == 0) return Optional.empty();
        BigDecimal total = sum(values);
        return Optional.of(total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
    }

    /**
     * 最大値を取得します。
     *
     * @param values 数値コレクション
     * @return 最大値（Optional）
     */
    public static Optional<BigDecimal> max(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) return Optional.empty();
        return values.stream().filter(v -> v != null).max(BigDecimal::compareTo);
    }

    /**
     * 最小値を取得します。
     *
     * @param values 数値コレクション
     * @return 最小値（Optional）
     */
    public static Optional<BigDecimal> min(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) return Optional.empty();
        return values.stream().filter(v -> v != null).min(BigDecimal::compareTo);
    }
}
