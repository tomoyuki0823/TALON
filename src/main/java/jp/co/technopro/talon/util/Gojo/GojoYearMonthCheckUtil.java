package jp.co.technopro.talon.util.Gojo;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * yyyyMMを扱うチェックユーティリティ。
 */
public final class GojoYearMonthCheckUtil {
    private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private GojoYearMonthCheckUtil() {
    }

    /**
     * yyyyMM → YearMonth（失敗時 empty）
     */
    public static Optional<YearMonth> parseYyyyMM(String yyyyMM) {
        if (yyyyMM == null || yyyyMM.isBlank()) return Optional.empty();
        try {
            return Optional.of(YearMonth.parse(yyyyMM, YYYYMM));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /**
     * 月差（絶対値）
     */
    public static long monthDiffAbs(YearMonth a, YearMonth b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return Math.abs(ChronoUnit.MONTHS.between(a, b));
    }

    /**
     * yyyyMM×2 → 月差（絶対値、失敗時 empty）
     */
    public static Optional<Long> monthDiffAbs(String aYyyyMM, String bYyyyMM) {
        var a = parseYyyyMM(aYyyyMM);
        var b = parseYyyyMM(bYyyyMM);
        if (a.isEmpty() || b.isEmpty()) return Optional.empty();
        return Optional.of(monthDiffAbs(a.get(), b.get()));
    }


    /**
     * 2つの yyyymm 文字列の月差が thresholdMonths 未満であれば true を返します。
     * <br>※以下も true（OK扱い）で返します：
     * <ul>
     *   <li>どちらかが null/空白</li>
     *   <li>どちらかが yyyymm 形式でない、または月が 1..12 でない</li>
     * </ul>
     * つまり、false が返るのは「両方が正しい yyyymm かつ 月差がしきい値以上」の時だけです。
     *
     * @param yyyymm1         1つ目の年月（yyyyMM）
     * @param yyyymm2         2つ目の年月（yyyyMM）
     * @param thresholdMonths 月差しきい値（例：38）
     * @return true: 問題なし（フラグ不要） / false: 問題あり（フラグ要）
     */
    public static boolean isDiffLessThanMonths(String yyyymm1, String yyyymm2, int thresholdMonths) {
        YearMonth ym1 = parseYearMonth(yyyymm1);
        YearMonth ym2 = parseYearMonth(yyyymm2);

        // 入力が空や形式不正なら「スルー（OK）」
        if (ym1 == null || ym2 == null) {
            return true;
        }

        int diff = Math.abs((ym2.getYear() - ym1.getYear()) * 12 + (ym2.getMonthValue() - ym1.getMonthValue()));
        return diff < thresholdMonths;
    }

    /**
     * yyyymm を YearMonth に変換。形式不正なら null。
     */
    private static YearMonth parseYearMonth(String yyyymm) {
        if (yyyymm == null) return null;
        String s = yyyymm.trim();
        if (!s.matches("^\\d{6}$")) return null;

        int year = Integer.parseInt(s.substring(0, 4));
        int month = Integer.parseInt(s.substring(4, 6));
        if (month < 1 || month > 12) return null;

        return YearMonth.of(year, month);
    }
}
