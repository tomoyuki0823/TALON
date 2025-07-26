package jp.co.technopro.talon.util;

import java.util.Objects;

/**
 * {@code StringCheckUtil} は、文字列に対する基本的なバリデーション処理を提供するユーティリティクラスです。
 * <p>
 * null や空文字、空白文字のみのチェック、数値・英字の正規表現チェックなど、共通的な条件分岐処理をカプセル化しています。
 * </p>
 *
 * <p>使用例：</p>
 * <pre>{@code
 * if (StringCheckUtil.isNotBlank(name)) {
 *     // 処理
 * }
 * }</pre>
 */
public class StringCheckUtil {

    // --- 内部ロジック（ラムダ式） ---
    private static final java.util.function.Predicate<String> isNotEmpty = s -> s != null && !s.isEmpty();
    private static final java.util.function.Predicate<String> isNotBlank = s -> s != null && !s.trim().isEmpty();

    /**
     * null でなく、かつ空文字でないかを判定します。
     *
     * @param s チェック対象の文字列
     * @return nullでなく、空文字でもなければ true、それ以外は false
     */
    public static boolean isNotEmpty(String s) {
        return isNotEmpty.test(s);
    }

    /**
     * null でなく、かつ空白を除いた文字列が空でないかを判定します。
     *
     * @param s チェック対象の文字列
     * @return nullでなく、空白文字を除いて空でなければ true、それ以外は false
     */
    public static boolean isNotBlank(String s) {
        return isNotBlank.test(s);
    }

    /**
     * null または空文字かを判定します。
     *
     * @param s チェック対象の文字列
     * @return null もしくは "" の場合 true、それ以外は false
     */
    public static boolean isNullOrEmpty(String s) {
        return !isNotEmpty(s);
    }

    /**
     * null または空白文字のみかを判定します。
     *
     * @param s チェック対象の文字列
     * @return null もしくは空白だけの文字列であれば true、それ以外は false
     */
    public static boolean isBlankOrNull(String s) {
        return !isNotBlank(s);
    }

    /**
     * 半角数字（0〜9）のみで構成されているかを判定します。
     *
     * @param s チェック対象の文字列
     * @return nullでなく、かつ {@code \\d+} にマッチする場合 true
     */
    public static boolean isNumeric(String s) {
        return s != null && s.matches("\\d+");
    }

    /**
     * 英字（大文字または小文字）のみで構成されているかを判定します。
     *
     * @param s チェック対象の文字列
     * @return nullでなく、かつ {@code [a-zA-Z]+} にマッチする場合 true
     */
    public static boolean isAlpha(String s) {
        return s != null && s.matches("[a-zA-Z]+");
    }

    // ※ 必要に応じて isAlphanumeric() や isEmail() などの追加も検討可能です。
}
