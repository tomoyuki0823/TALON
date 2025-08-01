package jp.co.technopro.talon.mapper.common;

import jp.co.technopro.talon.dto.common.BlockDataDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;

/**
 * Map形式のデータやResultSetから、DTOオブジェクト（例: {@link TalonParamDto}）へ変換するためのユーティリティクラス。
 * <p>
 * TALONスクリプトやDBアクセスから取得したデータをJavaオブジェクトへ詰め替える処理に用います。
 * スネークケース→キャメルケース変換や、BLOCKデータの動的バインディングにも対応します。
 * </p>
 */
public class TalonParamMapper {

    /**
     * Mapから {@link TalonParamDto} に詰め替える。
     * <p>
     * Talon側から渡された paramMap を元に、DTOへ各種情報（イベントID、ユーザー情報、BLOCK構成、
     * 対象データ、ボタンID、フラグ類、セッション情報など）を詰め替えます。
     * </p>
     * <p>
     * BLOCK1〜BLOCK9 のような動的な構造にも対応しており、画面設計変更にも柔軟に対応可能です。
     * </p>
     *
     * @param map イベントID、ユーザー情報、BLOCK構成などを含むMap構造（null許容）
     * @return {@link TalonParamDto} にマッピングされたインスタンス
     * @throws RuntimeException BLOCKデータのリフレクション処理に失敗した場合
     */

    public static TalonParamDto fromMap(Map<String, Object> map) {
        TalonParamDto dto = new TalonParamDto();
        dto.setEventId((String) map.get(MAP_KEY_EVENT_ID));
        dto.setUserMap(castMap(map.get(MAP_KEY_USER_MAP)));
        dto.setTargetData(castMap(map.get(MAP_KEY_TARGET_DATA)));
        dto.setConditionData(castMap(map.get(MAP_KEY_CONDITION_DATA)));
        dto.setBlockMeta(castList(map.get(MAP_KEY_BLOCK_META)));
        dto.setButtomId((String) map.get(MAP_KEY_BUTTOM_ID));
        dto.setTlnEventId((String) map.get(MAP_KEY_TLN_EVENT_ID));
        dto.setTlnIsInsert(Boolean.TRUE.equals(map.get(MAP_KEY_TLN_IS_INSERT)));
        dto.setTlnIsUpdate(Boolean.TRUE.equals(map.get(MAP_KEY_TLN_IS_UPDATE)));
        dto.setTlnIsDelete(Boolean.TRUE.equals(map.get(MAP_KEY_TLN_IS_DELETE)));
        dto.setTlnSession(castList(map.get(MAP_KEY_TLN_SESSION)));
        dto.setLogger(map.get(MAP_KEY_LOGGER));
        dto.setCompanyCode((String) map.get(MAP_KEY_COMPANY_CODE));
        dto.setCompanyCodeCommon((String) map.get(MAP_KEY_COMPANY_CODE_COMMON));
        // BLOCK1〜BLOCK9 に対応するデータを動的にバインド
        for (int i = 1; i <= 9; i++) {
            Object wrapperObj = map.get("BLOCK" + i);
            if (!(wrapperObj instanceof Map)) continue;

            Map<String, Object> wrapper = castMap(wrapperObj);
            BlockDataDto blockDto = new BlockDataDto();
            blockDto.setType((String) wrapper.get(MAP_KEY_TYPE));

            if (MAP_KEY_CARD.equalsIgnoreCase(blockDto.getType())) {
                blockDto.setCardData(castMap(wrapper.get(MAP_KEY_DATA)));
            } else if (MAP_KEY_LIST.equalsIgnoreCase(blockDto.getType())) {
                blockDto.setListData(castList(wrapper.get(MAP_KEY_DATA)));
            }

            try {
                Method setter = TalonParamDto.class.getMethod("setBlock" + i, BlockDataDto.class);
                setter.invoke(dto, blockDto);
            } catch (Exception e) {
                throw new RuntimeException("BLOCK" + i + " のセットに失敗しました", e);
            }
        }

        return dto;
    }

    /**
     * 任意のDTOクラスに、Mapから自動的にフィールドをマッピングして返却する。
     * <p>
     * マップのキーはスネークケース（例：HON_SIMEI）を想定し、
     * DTO側のキャメルケース（例：honSimei）へ変換されたフィールド名に値が設定されます。
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
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(instance, convertedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("DTOへのマッピングに失敗しました", e);
        }
    }

    /**
     * ResultSetの1行からDTOにマッピングする。
     *
     * @param rs    ResultSet（事前に rs.next() を実行する必要あり）
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
                String columnName = meta.getColumnLabel(i);
                String fieldName = toCamelCase(columnName);
                Object value = rs.getObject(i);

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (NoSuchFieldException ignored) {
                    // DTOに存在しないフィールドは無視
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("ResultSetからDTOへの変換に失敗しました", e);
        }
    }

    /**
     * ResultSetの全行をDTOのリストにマッピングする。
     *
     * @param rs    ResultSet（未消費状態）
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

    // ===== 内部ユーティリティ =====

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
     * スネークケース文字列をキャメルケースに変換する（例：HON_SIMEI → honSimei）。
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
     * DTOクラスに存在するフィールドを名前で取得する。
     *
     * @param clazz     対象クラス
     * @param fieldName フィールド名
     * @return 対応するField（存在しない場合はnull）
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 値をターゲット型に変換する。
     *
     * @param value      入力値
     * @param targetType 変換先の型
     * @return 変換後の値
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) return value.toString();
        if (targetType == int.class || targetType == Integer.class)
            return Integer.parseInt(value.toString());
        if (targetType == long.class || targetType == Long.class)
            return Long.parseLong(value.toString());
        if (targetType == BigDecimal.class)
            return new BigDecimal(value.toString());

        return value;
    }
}
