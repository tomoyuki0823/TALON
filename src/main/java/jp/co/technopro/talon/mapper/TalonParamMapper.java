package jp.co.technopro.talon.mapper;

import jp.co.technopro.talon.dto.TalonParamDto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Map形式のデータから DTO（TalonParamDto や任意のクラス）へ変換するためのユーティリティクラス。
 * <p>
 * 主にTALON環境でスクリプトやAPIから受け取ったMap構造のデータを、DTOオブジェクトへ詰め替える用途に使用します。
 */
public class TalonParamMapper {

    /**
     * Mapから {@link TalonParamDto} に詰め替える。
     *
     * @param map eventId, logicId, 各種BLOCKなどの情報を含むMap構造
     * @return TalonParamDto にマッピングされたインスタンス
     */
    public static TalonParamDto fromMap(Map<String, Object> map) {
        TalonParamDto dto = new TalonParamDto();
        dto.setEventId((String) map.get("eventId"));
        dto.setLogicId((String) map.get("logicId"));
        dto.setUserMap(castMap(map.get("USER_MAP")));
        dto.setTargetData(castMap(map.get("TARGET_DATA")));
        dto.setConditionData(castMap(map.get("CONDITION_DATA")));
        dto.setBlockMeta(castList(map.get("BLOCK_META")));

        for (int i = 1; i <= 9; i++) {
            Map<String, Object> blockMap = castMap(map.get("BLOCK" + i));
            if (blockMap != null) {
                try {
                    Method setter = TalonParamDto.class.getMethod("setBlock" + i, Map.class);
                    setter.invoke(dto, blockMap);
                } catch (Exception e) {
                    throw new RuntimeException("BLOCK" + i + "のセットに失敗しました", e);
                }
            }
        }

        return dto;
    }

    /**
     * ObjectをMapにキャストする補助メソッド。
     *
     * @param obj 任意のObject
     * @return Map形式（キャスト失敗時はClassCastException）
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    /**
     * ObjectをList&lt;Map&gt; にキャストする補助メソッド。
     *
     * @param obj 任意のObject
     * @return List&lt;Map&lt;String, Object&gt;&gt; 形式（キャスト失敗時はClassCastException）
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object obj) {
        return (List<Map<String, Object>>) obj;
    }

    /**
     * 任意のDTOクラスに、Mapから自動的にフィールドをマッピングして返却する。
     * <p>
     * マップのキーはスネークケース（例：HON_SIMEI）であることを想定し、
     * キャメルケース（例：honSimei）に自動変換されて対応するフィールドへ値がセットされます。
     *
     * @param map   データを保持するMap
     * @param clazz 対象のDTOクラス（引数には無引数コンストラクタが必要）
     * @param <T>   DTOの型
     * @return マッピング済みのDTOインスタンス
     * @throws RuntimeException インスタンス生成やリフレクション操作でエラーが発生した場合
     */
    public static <T> T mapToDto(Map<String, Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String fieldName = toCamelCase(key);

                Field field = findField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    try {
                        // 型変換（BigDecimalなど）もここで入れてOK
                        Object convertedValue = convertValue(value, field.getType());
                        field.set(instance, convertedValue);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("フィールドの代入に失敗: " + fieldName, e);
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("DTOへのマッピングに失敗しました", e);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) return value.toString();
        if (targetType == int.class || targetType == Integer.class)
            return Integer.parseInt(value.toString());
        if (targetType == long.class || targetType == Long.class)
            return Long.parseLong(value.toString());
        if (targetType == BigDecimal.class)
            return new BigDecimal(value.toString());

        return value; // それ以外はそのまま
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null; // 存在しない場合はnullでスキップ
    }


    /**
     * スネークケース文字列（例: HON_SIMEI）をキャメルケース（例: honSimei）に変換する。
     *
     * @param s スネークケース形式の文字列
     * @return キャメルケースに変換された文字列
     */
    private static String toCamelCase(String s) {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                result.append(upperNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upperNext = false;
            }
        }
        return result.toString();
    }

    /**
     * ResultSetの1行からDTOにマッピングする。
     *
     * @param rs    ResultSet（事前に rs.next() が必要）
     * @param clazz DTOのクラス
     * @param <T>   DTO型
     * @return マッピングされたDTO
     * @throws SQLException SQL例外
     */
    public static <T> T mapResultSetToDto(ResultSet rs, Class<T> clazz) throws SQLException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnLabel(i); // エイリアス or カラム名
                String fieldName = toCamelCase(columnName);
                Object value = rs.getObject(i);

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (NoSuchFieldException ignored) {
                    // DTOに存在しないフィールドはスキップ
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("ResultSetからDTOへの変換に失敗しました", e);
        }
    }

    /**
     * ResultSetのすべての行をDTOのリストにマッピングする。
     *
     * @param rs    ResultSet
     * @param clazz DTOのクラス
     * @param <T>   DTO型
     * @return List形式のDTOリスト
     * @throws SQLException SQL例外
     */
    public static <T> List<T> mapResultSetToDtoList(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> list = new java.util.ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSetToDto(rs, clazz));
        }
        return list;
    }
}
