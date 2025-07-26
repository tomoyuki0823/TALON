package jp.co.technopro.talon.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code CollectionTransformUtil} は、コレクションに対する変換・整形処理を提供するユーティリティクラスです。
 * <p>
 * null の除去、重複要素の削除、クリーンなリストの生成など、よく使われる加工処理をまとめています。
 */
public class CollectionTransformUtil {

    private CollectionTransformUtil() {
        // インスタンス化防止
    }

    /**
     * 入力コレクションから null 要素を除去し、元の順序を維持した新しいリストを返します。
     *
     * @param collection 入力コレクション
     * @param <T>        要素の型
     * @return null 要素を除いたリスト。入力が null の場合は空リスト。
     */
    public static <T> List<T> removeNull(Collection<T> collection) {
        if (collection == null) return Collections.emptyList();
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 入力コレクションから重複要素を除去し、元の順序を維持した新しいリストを返します。
     * <p>内部的に {@link LinkedHashSet} を使用しています。</p>
     *
     * @param collection 入力コレクション
     * @param <T>        要素の型
     * @return 重複を除いたリスト。null 要素も保持されます。入力が null の場合は空リスト。
     */
    public static <T> List<T> mergeDistinct(Collection<T> collection) {
        if (collection == null) return Collections.emptyList();
        return new ArrayList<>(new LinkedHashSet<>(collection));
    }

    /**
     * 入力コレクションから null 要素と重複要素の両方を除去し、元の順序を維持した新しいリストを返します。
     *
     * @param collection 入力コレクション
     * @param <T>        要素の型
     * @return null と重複を除いたリスト。入力が null の場合は空リスト。
     */
    public static <T> List<T> clean(Collection<T> collection) {
        if (collection == null) return Collections.emptyList();
        return collection.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 入力コレクションをソートして新しいリストとして返します（Comparator 必須）。
     * null 要素は除外されます。
     *
     * @param collection 入力コレクション
     * @param comparator ソート条件
     * @param <T>        要素の型
     * @return ソートされたリスト。null 要素と null 入力は除外。
     */
    public static <T> List<T> sortNonNull(Collection<T> collection, Comparator<? super T> comparator) {
        if (collection == null || comparator == null) return Collections.emptyList();
        return collection.stream()
                .filter(Objects::nonNull)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * コレクションを Map に変換します（キーを指定、null 要素除去、重複キーは後勝ち）。
     *
     * @param collection 入力コレクション
     * @param keyMapper  キーの抽出関数
     * @param <T>        要素の型
     * @param <K>        キーの型
     * @return Map（null 要素除去、重複キーは最後の値で上書き）
     */
    public static <T, K> Map<K, T> toMap(Collection<T> collection, java.util.function.Function<T, K> keyMapper) {
        if (collection == null || keyMapper == null) return Collections.emptyMap();
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        keyMapper,
                        e -> e,
                        (prev, next) -> next,
                        LinkedHashMap::new
                ));
    }
}
