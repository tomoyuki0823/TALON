package jp.co.technopro.talon.util.common;

import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code StringTransformUtil} は、文字列に対する null 安全な整形・変換・補完処理を提供するユーティリティクラスです。
 */
public class StringTransformUtil {

    private StringTransformUtil() {
        // インスタンス化防止
    }

    /**
     * null の場合は空文字、そうでなければ trim 済み文字列を返します。
     *
     * @param str 入力文字列
     * @return trim 済み文字列 or 空文字
     */
    public static String safeTrim(String str) {
        return Optional.ofNullable(str).map(String::trim).orElse("");
    }

    /**
     * null の場合はそのまま null、そうでなければ大文字に変換します。
     *
     * @param str 入力文字列
     * @return 大文字文字列 or null
     */
    public static String toUpper(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * null の場合はそのまま null、そうでなければ小文字に変換します。
     *
     * @param str 入力文字列
     * @return 小文字文字列 or null
     */
    public static String toLower(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * null または空文字列の場合にデフォルト文字列を返します。
     *
     * @param str          入力文字列
     * @param defaultValue デフォルト文字列
     * @return 入力が null/空文字列の場合は default、それ以外は入力文字列
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return (str == null || str.isEmpty()) ? defaultValue : str;
    }

    /**
     * 空文字列なら null を返します。
     *
     * @param str 入力文字列
     * @return 空文字列の場合は null、それ以外は入力文字列
     */
    public static String nullIfEmpty(String str) {
        return (str == null || str.isEmpty()) ? null : str;
    }

    /**
     * null を空文字列に変換します。
     *
     * @param str 入力文字列
     * @return null の場合は "", それ以外は入力文字列
     */
    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    /**
     * 左側を指定文字で埋めて、指定長の文字列を返します。
     *
     * @param str     入力文字列（null可）
     * @param length  最終的な長さ
     * @param padChar 埋める文字
     * @return パディング後の文字列
     */
    public static String padLeft(String str, int length, char padChar) {
        String s = Optional.ofNullable(str).orElse("");
        if (s.length() >= length) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * 右側を指定文字で埋めて、指定長の文字列を返します。
     *
     * @param str     入力文字列
     * @param length  最終的な長さ
     * @param padChar 埋める文字
     * @return パディング後の文字列
     */
    public static String padRight(String str, int length, char padChar) {
        String s = Optional.ofNullable(str).orElse("");
        if (s.length() >= length) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = s.length(); i < length; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    /**
     * ゼロ埋めして左側を指定桁数に揃えます。
     *
     * @param str    入力文字列
     * @param length 桁数
     * @return 例: zeroPadLeft("12", 5) → "00012"
     */
    public static String zeroPadLeft(String str, int length) {
        return padLeft(str, length, '0');
    }

    /**
     * 指定文字数を超える場合、末尾に "..." を付けて切り詰めます。
     *
     * @param str   入力文字列
     * @param limit 最大文字数（3文字未満の場合はそのまま返す）
     * @return 省略された文字列 or 元の文字列
     */
    public static String abbreviate(String str, int limit) {
        if (str == null || limit < 4) return str;
        if (str.length() <= limit) return str;
        return str.substring(0, limit - 3) + "...";
    }


    /**
     * 空白・タブなどの連続を1つの半角スペースに正規化します。
     *
     * @param str 入力文字列
     * @return 正規化された文字列（nullは空文字）
     */
    public static String normalizeWhitespace(String str) {
        return Optional.ofNullable(str).orElse("")
                .replaceAll("[\\s\\t\\u3000]+", " ")
                .trim();
    }

    /**
     * snake_case を camelCase に変換します。
     *
     * @param str 入力文字列
     * @return camelCase に変換された文字列
     */
    public static String snakeToCamel(String str) {
        if (str == null || str.isEmpty()) return str;
        Matcher m = Pattern.compile("_(.)").matcher(str.toLowerCase());
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * camelCase を snake_case に変換します。
     *
     * @param str 入力文字列
     * @return snake_case に変換された文字列
     */
    public static String camelToSnake(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase();
    }

    /**
     * HTML の特殊文字をエスケープします（簡易版）。
     *
     * @param str 入力文字列
     * @return エスケープされた文字列
     */
    public static String escapeHtml(String str) {
        if (str == null) return null;
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * HTML エスケープされた文字列を復元します（簡易版）。
     *
     * @param str 入力文字列
     * @return 復元された文字列
     */
    public static String unescapeHtml(String str) {
        if (str == null) return null;
        return str.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    /**
     * 指定バイト長（全角2バイト、半角1バイト）で切り詰め、必要に応じて末尾に "…" を付加します。
     *
     * @param str   入力文字列
     * @param limit バイト長の上限（UTF-8相当）
     * @return 切り詰められた文字列
     */
    public static String truncateByByteLength(String str, int limit) {
        if (str == null || limit <= 0) return "";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= limit) return str;

        StringBuilder sb = new StringBuilder();
        int byteLen = 0;
        for (char c : str.toCharArray()) {
            int charBytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            if (byteLen + charBytes > limit - 1) break; // 末尾に…付ける分
            sb.append(c);
            byteLen += charBytes;
        }
        sb.append("…");
        return sb.toString();
    }
}