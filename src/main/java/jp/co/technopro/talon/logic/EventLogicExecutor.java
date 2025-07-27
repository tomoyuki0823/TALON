package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.mapper.TalonParamMapper;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.MapKeyCommon.*;
import static jp.co.technopro.talon.consts.SqlKey.*;
import static jp.co.technopro.talon.util.DbUtil.selectById;

/**
 * イベント実行制御を行うロジッククラス。
 * <p>
 * 機能ID（FUNC_ID）とイベントID（EVENT_ID）に対応する複数の Java ロジックを
 * {@code TPI_M_FUNC_EVENT} → {@code TPI_M_JAVA_LOGIC} テーブル定義に基づいて
 * 順次呼び出します。
 * </p>
 */
public class EventLogicExecutor {

    /**
     * 指定された機能ID・イベントIDに対応する Java ロジックを順番に呼び出します。
     *
     * @param conn     DB接続
     * @param paramMap パラメータマップ。FUNC_ID, EVENT_ID, 各種BLOCK情報などを含む。
     * @return 実行結果（status: true/false, message: 実行メッセージ）
     * @throws Exception クラスロード・メソッド実行などで発生する任意の例外
     */
    public static Map<String, Object> executeEventLogic(Connection conn, Map<String, Object> paramMap) throws Exception {
        String funcId = getRequiredValue(paramMap, MAP_KEY_FUNC_ID);
        String eventId = getRequiredValue(paramMap, MAP_KEY_EVENT_ID);

        TalonParamDto paramDto = TalonParamMapper.fromMap(paramMap);

        List<Map<String, Object>> funcEventList = null;

        if (!MAP_KEY_BUTTOM.equals(eventId)) {
            funcEventList = selectById(SQL_KEY_TPI_M_FUNC_EVENT, funcId, eventId);
        } else {
            String buttomId = getRequiredValue(paramMap, MAP_KEY_BUTTOM_ID);
            funcEventList = selectById(SQL_KEY_TPI_M_FUNC_EVENT_BUTTOM , funcId, eventId, buttomId);
        }

        for (Map<String, Object> row : funcEventList) {
            executeLogicIfActive(conn, row, paramDto);
        }

        return Map.of("status", true, "message", "すべてのロジックが実行されました");
    }

    /**
     * ロジックが有効（IS_ACTIVE=1）の場合に限り、指定されたロジックを実行します。
     *
     * @param conn         DB接続
     * @param funcEventRow ロジックIDを含むイベント行（TPI_M_FUNC_EVENT）
     * @param paramDto     実行パラメータ（TalonParamDto形式）
     * @throws Exception クラス生成・メソッド呼び出しエラー
     */
    private static void executeLogicIfActive(Connection conn, Map<String, Object> funcEventRow, TalonParamDto paramDto) throws Exception {
        String logicId = (String) funcEventRow.get(MAP_KEY_LOGIC_ID);
        paramDto.setLogicId(logicId);

        Map<String, Object> logicRow = selectById(SQL_KEY_TPI_M_JAVA_LOGIC, logicId)
                .stream().findFirst().orElse(null);

        if (logicRow == null || !MAP_KEY_IS_ACTIVE_ON.equals(String.valueOf(logicRow.get(MAP_KEY_IS_ACTIVE)).trim())) {
            System.out.println("LogicID [" + logicId + "] は無効または存在しないためスキップします。");
            return;
        }

        invokeLogic(conn, logicRow, paramDto, logicId);
    }

    /**
     * Javaクラスおよびメソッドをリフレクションで呼び出し、ロジックを実行します。
     *
     * @param conn      DB接続
     * @param logicRow  実行対象ロジックの定義（TPI_M_JAVA_LOGIC）
     * @param paramDto  実行時引数（TalonParamDto）
     * @param logicId   ロジックID（ログ出力用）
     * @throws Exception クラスロード・インスタンス生成・メソッド実行時の任意の例外
     */
    private static void invokeLogic(Connection conn, Map<String, Object> logicRow, TalonParamDto paramDto, String logicId) throws Exception {
        String className = (String) logicRow.get(MAP_KEY_CLASS_NAME);
        String methodName = (String) logicRow.get(MAP_KEY_METHOD_NAME);

        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod(methodName, Connection.class, TalonParamDto.class);

        Object result = method.invoke(instance, conn, paramDto);
        System.out.println("Logic [" + logicId + "] executed. Result: " + result);
    }

    /**
     * Map から必須キーを取得し、未設定の場合は例外をスローします。
     *
     * @param map 対象のMap
     * @param key 必須キー
     * @return 値（null/空文字不可）
     * @throws IllegalArgumentException 値が null または空文字の場合
     */
    private static String getRequiredValue(Map<String, Object> map, String key) {
        String value = (String) map.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " が未指定です。");
        }
        return value;
    }
}
