package jp.co.technopro.talon.util.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DtoMapTransformUtil {

    private DtoMapTransformUtil() {}

    /**
     * 任意のDTOを Map<String, Object> に変換します。
     *
     * @param dto DTOインスタンス
     * @return DBに挿入可能な形式の Map
     */
    public static Map<String, Object> toMap(Object dto) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                String key = camelToSnake(field.getName());
                map.put(key, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("フィールド変換失敗: " + field.getName(), e);
            }
        }
        return map;
    }

    /**
     * キャメルケースをスネークケース（大文字）に変換します。
     * 例: "honTaikaiYoteibi" → "HON_TAIKAI_YOTEIBI"
     */
    private static String camelToSnake(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}
