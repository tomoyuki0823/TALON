package jp.co.technopro.talon.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code CollectionCheckUtil} は、コレクション（List, Set, Map）に関する
 * よくあるチェック処理を共通化したユーティリティクラスです。
 */
public class CollectionCheckUtil {

    /**
     * null ではなく、かつ空でないコレクションかどうかを判定します。
     *
     * @param c チェック対象のコレクション
     * @return null でなく、かつ空でなければ true
     */
    public static boolean isNotEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    /**
     * null または空のコレクションかどうかを判定します。
     *
     * @param c チェック対象のコレクション
     * @return null または空であれば true
     */
    public static boolean isEmptyOrNull(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * null ではなく、かつ空でないマップかどうかを判定します。
     *
     * @param m チェック対象のマップ
     * @return null でなく、かつ空でなければ true
     */
    public static boolean isNotEmpty(Map<?, ?> m) {
        return m != null && !m.isEmpty();
    }

    /**
     * null または空のマップかどうかを判定します。
     *
     * @param m チェック対象のマップ
     * @return null または空であれば true
     */
    public static boolean isEmptyOrNull(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * コレクションに null が1つでも含まれているかを判定します。
     *
     * @param c チェック対象のコレクション
     * @return null 要素が含まれていれば true、それ以外は false
     */
    public static boolean anyNull(Collection<?> c) {
        return c != null && c.stream().anyMatch(Objects::isNull);
    }

    /**
     * コレクションが null またはすべて null 要素で構成されているかを判定します。
     *
     * @param c チェック対象のコレクション
     * @return null またはすべて null の場合 true
     */
    public static boolean containsOnlyNull(Collection<?> c) {
        return c == null || c.stream().allMatch(Objects::isNull);
    }

    /**
     * リストに重複要素が含まれているかを判定します。
     *
     * @param list チェック対象のリスト
     * @return 同じ要素が2つ以上含まれていれば true
     */
    public static boolean containsDuplicates(List<?> list) {
        if (list == null) return false;
        Set<Object> unique = new HashSet<>();
        return list.stream().anyMatch(e -> !unique.add(e));
    }

    /**
     * コレクション内の null でない要素数をカウントします。
     *
     * @param c チェック対象のコレクション
     * @return null を除いた要素数（nullなら0）
     */
    public static long countNonNull(Collection<?> c) {
        return c == null ? 0 : c.stream().filter(Objects::nonNull).count();
    }

    /**
     * すべての要素が null または空文字列（trim済）で構成されているかを判定します。
     * 非String型の要素が含まれる場合は false を返します。
     *
     * @param list チェック対象のコレクション
     * @return すべて空文字または null の場合 true
     */
    public static boolean allEmptyString(Collection<?> list) {
        if (list == null || list.isEmpty()) return true;

        return list.stream()
                .allMatch(item -> (item == null || (item instanceof String && ((String) item).trim().isEmpty())));
    }
}
