package jp.co.technopro.talon.util.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * {@code MapMergeUtil} は、2つの Map を {@link BiFunction} を用いて柔軟にマージするためのユーティリティです。
 */
public class MapMergeUtil {

    private MapMergeUtil() {
        // インスタンス化防止
    }

    /**
     * 2つの Map を指定されたマージ関数で結合します。
     * <p>重複キーが存在する場合、マージ関数を適用して値を決定します。</p>
     *
     * @param map1        ベースの Map
     * @param map2        マージ対象の Map
     * @param mergeFunc   値のマージ関数 (既存値, 新規値) → 結合値
     * @param <K>         キー型
     * @param <V>         値型
     * @return マージされた Map（LinkedHashMap）
     */
    public static <K, V> Map<K, V> mergeWithRule(Map<K, V> map1, Map<K, V> map2, BiFunction<V, V, V> mergeFunc) {
        Map<K, V> result = new LinkedHashMap<>();
        if (map1 != null) {
            result.putAll(map1);
        }
        if (map2 != null) {
            for (Map.Entry<K, V> entry : map2.entrySet()) {
                result.merge(entry.getKey(), entry.getValue(), mergeFunc);
            }
        }
        return result;
    }
}
