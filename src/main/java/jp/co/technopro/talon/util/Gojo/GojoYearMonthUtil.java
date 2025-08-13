package jp.co.technopro.talon.util.Gojo;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * yyyyMM 文字列の厳格バリデーションと比較小物。
 */
public final class GojoYearMonthUtil {
    private GojoYearMonthUtil() {}

    private static final DateTimeFormatter YM_FMT =
            DateTimeFormatter.ofPattern("uuuuMM").withResolverStyle(ResolverStyle.STRICT);

    /**
     * yyyyMM を YearMonth に変換（厳格）。不正時は null。
     */
    public static YearMonth parseYYYYMM(String yyyymm) {
        if (yyyymm == null) return null;
        String s = yyyymm.trim();
        if (s.length() != 6) return null;
        try {
            return YearMonth.parse(s, YM_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

