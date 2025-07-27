package jp.co.technopro.talon.util;

import java.util.Map;
import java.util.Objects;

/**
 * {@code MapCheckUtil} は、Map に関する基本的な判定処理（null, 空, 重複、キー/値の存在）を提供します。
 */
public class MapCheckUtil {

    /**
     * Map が null または空かどうかを判定します。
     *
     * @param map チェック対象の Map
     * @return null または空であれば true、それ以外は false
     */
    public static boolean isEmptyOrNull(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Map に null キーが含まれているかを判定します。
     *
     * @param map チェック対象の Map
     * @return null キーが存在すれば true、Map が null でも false
     */
    public static boolean containsNullKey(Map<?, ?> map) {
        return map != null && map.containsKey(null);
    }

    /**
     * Map に null 値が含まれているかを判定します。
     *
     * @param map チェック対象の Map
     * @return null 値が1つでも含まれていれば true、Map が null でも false
     */
    public static boolean containsNullValue(Map<?, ?> map) {
        return map != null && map.values().stream().anyMatch(Objects::isNull);
    }

    /**
     * Map のキーがすべて null であるかを判定します。
     *
     * @param map チェック対象の Map
     * @return すべて null キーなら true、そうでなければ false
     */
    public static boolean allKeysAreNull(Map<?, ?> map) {
        return map != null && !map.isEmpty() && map.keySet().stream().allMatch(Objects::isNull);
    }

    /**
     * Map の値がすべて null であるかを判定します。
     *
     * @param map チェック対象の Map
     * @return すべて null 値なら true、そうでなければ false
     */
    public static boolean allValuesAreNull(Map<?, ?> map) {
        return map != null && !map.isEmpty() && map.values().stream().allMatch(Objects::isNull);
    }

    /**
     * Mapがnullまたは空かどうかを判定します。
     *
     * @param map 判定対象のMap
     * @return nullまたは空ならtrue
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 指定されたMapから指定キーの値を取り出し、いずれかがnullまたは空文字列であればtrue。
     *
     * @param map   チェック対象のMap
     * @param keys  チェック対象のキー群
     * @return いずれかがnullまたは空文字列であればtrue
     */
    public static boolean isAnyBlank(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) return true;
        for (String key : keys) {
            Object val = map.get(key);
            if (val == null) return true;
            if (val instanceof String && ((String) val).trim().isEmpty()) return true;
        }
        return false;
    }
}
