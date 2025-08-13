package jp.co.technopro.talon.util.Gojo;

import java.time.YearMonth;
import java.util.Objects;

/**
 * 会員在籍期間と療養年月の関係を判定するユーティリティ。
 */
public final class GojoMembershipCheckUtil {

    private GojoMembershipCheckUtil() {}

    /**
     * 判定結果。
     * BEFORE_KANYU_PREV … 加入年月の前月「以前」
     * AFTER_TAIKAI_NEXT … 退会年月の翌月「以降」
     * OK                 … どちらにも該当せず（在籍内とみなせる）
     * INPUT_INVALID      … いずれかの入力が不正（判定不能）
     */
    public enum Result { BEFORE_KANYU_PREV, AFTER_TAIKAI_NEXT, OK, INPUT_INVALID }

    /**
     * 療養年月(yyyyMM) と 加入日(和暦日付)・退会日(和暦日付)の関係を判定します。
     * <ul>
     *   <li>加入基準：加入「年月」の前月以前なら BEFORE_KANYU_PREV</li>
     *   <li>退会基準：退会「年月」の翌月以降なら AFTER_TAIKAI_NEXT</li>
     *   <li>加入/退会どちらかが未設定(null)なら、その側の判定はスキップ</li>
     *   <li>日付形式不正などで YearMonth へ変換できなければ INPUT_INVALID</li>
     * </ul>
     *
     * @param ryoyoYYYYMM  療養年月（yyyyMM）
     * @param kanyuWareki  加入日（和暦日付文字列：例「令和6年4月30日」「R6/4/30」等。西暦も可）
     * @param taikaiWareki 退会日（和暦日付文字列）
     * @return 判定結果
     */
    public static Result judgeRyoyoVsKanyuTaikai(String ryoyoYYYYMM,
                                                 String kanyuWareki,
                                                 String taikaiWareki) {
        YearMonth ryoyo = GojoYearMonthUtil.parseYYYYMM(ryoyoYYYYMM);
        if (ryoyo == null) {
            return Result.INPUT_INVALID;
        }
        YearMonth kanyuYm  = GojoWarekiParserUtil.toYearMonth(kanyuWareki);
        YearMonth taikaiYm = GojoWarekiParserUtil.toYearMonth(taikaiWareki);

        // どちらも null の場合は在籍判定不能だが、要件に合わせ OK扱いにするならここで調整
        if (kanyuYm == null && taikaiYm == null) {
            return Result.INPUT_INVALID;
        }

        // 加入：加入年月の前月以前 → NG
        if (kanyuYm != null) {
            YearMonth kanyuPrev = kanyuYm.minusMonths(1);
            if (ryoyo.isBefore(kanyuPrev) || ryoyo.equals(kanyuPrev)) {
                return Result.BEFORE_KANYU_PREV;
            }
        }

        // 退会：退会年月の翌月以降 → NG
        if (taikaiYm != null) {
            YearMonth taikaiNext = taikaiYm.plusMonths(1);
            if (ryoyo.isAfter(taikaiNext) || ryoyo.equals(taikaiNext)) {
                return Result.AFTER_TAIKAI_NEXT;
            }
        }

        return Result.OK;
    }
}
