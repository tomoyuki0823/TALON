package jp.co.technopro.talon.util;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code MapTransformUtil} は、Map に対する変換・整形・抽出処理を提供するユーティリティクラスです。
 * <p>null キー/値の除去、値のマッピング、キー・値のリスト化などのよく使う操作を提供します。</p>
 */
public class MapTransformUtil {

    private MapTransformUtil() {
        // インスタンス化防止
    }

    /**
     * null キーまたは null 値を含むエントリを除外した新しい Map を返します。
     *
     * @param map 入力 Map
     * @param <K> キー型
     * @param <V> 値型
     * @return null キー・値を除外した Map（順序維持）
     */
    public static <K, V> Map<K, V> removeNullKeyOrValue(Map<K, V> map) {
        if (map == null) return Collections.emptyMap();
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    /**
     * Map の値を Function で変換し、新しい Map を返します。
     *
     * @param map         入力 Map
     * @param valueMapper 値の変換関数
     * @param <K>         キー型
     * @param <V>         元の値型
     * @param <R>         変換後の値型
     * @return 変換された値を持つ Map（キーは元のまま）
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> valueMapper) {
        if (map == null || valueMapper == null) return Collections.emptyMap();
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> valueMapper.apply(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    /**
     * Map を指定した条件でフィルタし、新しい Map を返します。
     *
     * @param map    入力 Map
     * @param filter (key, value) による判定関数
     * @param <K>    キー型
     * @param <V>    値型
     * @return 条件を満たすエントリだけを含む Map
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, BiPredicate<K, V> filter) {
        if (map == null || filter == null) return Collections.emptyMap();
        return map.entrySet().stream()
                .filter(e -> filter.test(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    /**
     * Map のキーをリスト化して返します。
     *
     * @param map 入力 Map
     * @param <K> キー型
     * @param <V> 値型
     * @return キーのリスト（順序維持）
     */
    public static <K, V> List<K> toKeyList(Map<K, V> map) {
        if (map == null) return Collections.emptyList();
        return new ArrayList<>(map.keySet());
    }

    /**
     * Map の値をリスト化して返します。
     *
     * @param map 入力 Map
     * @param <K> キー型
     * @param <V> 値型
     * @return 値のリスト（順序維持）
     */
    public static <K, V> List<V> toValueList(Map<K, V> map) {
        if (map == null) return Collections.emptyList();
        return new ArrayList<>(map.values());
    }

    /**
     * 値が null のエントリを除外して Map を返します（キーの null は許容）。
     *
     * @param map 入力 Map
     * @param <K> キー型
     * @param <V> 値型
     * @return 値が null のエントリを除いた Map
     */
    public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
        if (map == null) return Collections.emptyMap();
        return map.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }
}
