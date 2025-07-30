package jp.co.technopro.talon.util.common;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * {@code SafeMapAccessUtil} は、Map の null 安全な取得・変換・フィルタ処理を簡潔に記述するためのユーティリティクラスです。
 */
public class SafeMapAccessUtil {

    /**
     * Map から指定キーの値を取得し、nullチェックを行い、Optional として返します。
     *
     * @param map 入力 Map
     * @param key キー
     * @param <K> キー型
     * @param <V> 値型
     * @return Optional にラップされた値（存在しない・null の場合は空）
     */
    public static <K, V> Optional<V> get(Map<K, V> map, K key) {
        return Optional.ofNullable(map)
                .map(m -> m.get(key))
                .filter(v -> v != null);
    }

    /**
     * Map から指定キーの値を取得し、変換関数を適用して Optional で返します。
     *
     * @param map     入力 Map
     * @param key     キー
     * @param mapper  変換関数（V → R）
     * @param <K>     キー型
     * @param <V>     値型
     * @param <R>     変換後の型
     * @return Optional にラップされた変換後の値（変換に失敗・null の場合は空）
     */
    public static <K, V, R> Optional<R> getAs(Map<K, V> map, K key, Function<V, R> mapper) {
        return Optional.ofNullable(map)
                .map(m -> m.get(key))
                .filter(v -> v != null)
                .map(mapper)
                .filter(r -> r != null);
    }

    /**
     * Map から指定キーの値を取得し、条件を満たす場合のみ Optional で返します。
     *
     * @param map      入力 Map
     * @param key      キー
     * @param filter   値に対する判定条件
     * @param <K>      キー型
     * @param <V>      値型
     * @return 条件を満たす値があれば Optional、なければ empty
     */
    public static <K, V> Optional<V> getIf(Map<K, V> map, K key, Predicate<V> filter) {
        return Optional.ofNullable(map)
                .map(m -> m.get(key))
                .filter(filter);
    }

    /**
     * Map から値を取得し、条件に一致しなければデフォルト値を返します。
     *
     * @param map          入力 Map
     * @param key          キー
     * @param filter       値に対する条件
     * @param defaultValue 条件を満たさないときのデフォルト値
     * @param <K>          キー型
     * @param <V>          値型
     * @return 値 or デフォルト
     */
    public static <K, V> V getOrElseIf(Map<K, V> map, K key, Predicate<V> filter, V defaultValue) {
        return getIf(map, key, filter).orElse(defaultValue);
    }

    /**
     * Map から文字列を取得し、空文字や null でないなら返す。
     *
     * @param map          入力 Map
     * @param key          キー
     * @param defaultValue 空や null の場合のデフォルト
     * @return 正常な文字列 or デフォルト値
     */
    public static String getStringOrDefault(Map<String, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return (value instanceof String) ? (String) value : defaultValue;
    }

    /**
     * Map から文字列を取得します。
     * 該当キーが存在しない、または値が null である場合は空文字を返します。
     * 値が String 型でない場合も空文字を返します。
     *
     * @param map 入力 Map
     * @param key 取得対象のキー
     * @return 文字列（null・非文字列なら空文字）
     */
    public static String getString(Map<String, ?> map, String key) {
        return getStringOrDefault(map, key, "");
    }


}
