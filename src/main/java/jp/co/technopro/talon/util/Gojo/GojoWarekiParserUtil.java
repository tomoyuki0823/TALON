package jp.co.technopro.talon.util.Gojo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 和暦（日付文字列）を YearMonth へ変換するユーティリティ。
 * 代表的な和暦表記（例：令和6年4月30日 / R6/4/30 / 平成31-4-30 など）を許容します。
 * 西暦表記（yyyy/MM/dd, yyyyMMdd など）もフォールバックで受け付けます。
 */
public final class GojoWarekiParserUtil {

    private GojoWarekiParserUtil() {}

    /** 和暦用の代表パターン群（厳格パース） */
    private static final List<DateTimeFormatter> WAREKI_FORMATTERS;
    /** 西暦フォールバック（任意） */
    private static final List<DateTimeFormatter> SEIREKI_FORMATTERS;

    static {
        Locale jp = Locale.JAPAN;

        DateTimeFormatter gYMD_kanji = new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern("G")
                .appendValue(ChronoField.YEAR_OF_ERA, 1, 2, java.time.format.SignStyle.NORMAL)
                .appendLiteral('年')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('月')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral('日')
                .toFormatter(jp)
                .withChronology(JapaneseChronology.INSTANCE)
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter gYMD_slash = new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern("G")
                .appendValue(ChronoField.YEAR_OF_ERA, 1, 2, java.time.format.SignStyle.NORMAL)
                .appendLiteral('/')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .toFormatter(jp)
                .withChronology(JapaneseChronology.INSTANCE)
                .withResolverStyle(ResolverStyle.STRICT);

        DateTimeFormatter gYMD_hyphen = new DateTimeFormatterBuilder()
                .parseLenient()
                .parseCaseInsensitive()
                .appendPattern("G")
                .appendValue(ChronoField.YEAR_OF_ERA, 1, 2, java.time.format.SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .toFormatter(jp)
                .withChronology(JapaneseChronology.INSTANCE)
                .withResolverStyle(ResolverStyle.STRICT);

        WAREKI_FORMATTERS = List.of(gYMD_kanji, gYMD_slash, gYMD_hyphen);

        // 西暦パターン（任意で受容）
        SEIREKI_FORMATTERS = new ArrayList<>();
        SEIREKI_FORMATTERS.add(DateTimeFormatter.ofPattern("uuuu/M/d").withResolverStyle(ResolverStyle.STRICT));
        SEIREKI_FORMATTERS.add(DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT));
        SEIREKI_FORMATTERS.add(DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
        SEIREKI_FORMATTERS.add(DateTimeFormatter.BASIC_ISO_DATE.withResolverStyle(ResolverStyle.STRICT));
    }

    /**
     * 和暦（または西暦）日付文字列を YearMonth に変換します。
     * null / 空文字は null を返します。
     *
     * @param warekiOrSeirekiDate 和暦日付（例：令和6年4月30日 / R6/4/30 等）または西暦日付
     * @return 変換された YearMonth（null を許容）
     */
    public static YearMonth toYearMonth(String warekiOrSeirekiDate) {
        if (warekiOrSeirekiDate == null || warekiOrSeirekiDate.trim().isEmpty()) {
            return null;
        }
        String s = warekiOrSeirekiDate.trim();

        // 1) 和暦パターンで試行
        for (DateTimeFormatter f : WAREKI_FORMATTERS) {
            try {
                TemporalAccessor ta = f.withChronology(JapaneseChronology.INSTANCE).parse(s);
                JapaneseDate jdate = JapaneseDate.from(ta);
                LocalDate iso = LocalDate.from(jdate);
                return YearMonth.from(iso);
            } catch (Exception ignore) {}
        }

        // 2) 西暦パターンで試行
        for (DateTimeFormatter f : SEIREKI_FORMATTERS) {
            try {
                LocalDate iso = LocalDate.parse(s, f);
                return YearMonth.from(iso);
            } catch (Exception ignore) {}
        }
        return null; // どれも合わなければ不正
    }
}
