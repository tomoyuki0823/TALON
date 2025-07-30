package jp.co.technopro.talon.logic.common;

import jp.co.technopro.logger.TalonLogger;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.mapper.TalonParamMapper;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.tln.TlnMessageConst.*;
import static jp.co.technopro.talon.consts.tln.TlnSqlXmlKeyConst.*;
import static jp.co.technopro.talon.consts.tln.TlnCompanyConst.COMPANY_CD_COMMON;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;
import static jp.co.technopro.talon.util.common.DbUtil.selectById;

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
    public static EventResultDto executeEventLogic(Connection conn, Map<String, Object> paramMap) throws Exception {

        try {
            TalonLogger.logInfo(paramMap, "=== paramMap 内容 ===");
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                System.out.println(key + " => " + (val != null ? val.getClass().getName() + ": " + val : "null"));
            }
            System.out.println("=== paramMap END ===");

            String funcId = getRequiredValue(paramMap, MAP_KEY_FUNC_ID);
            String eventId = getRequiredValue(paramMap, MAP_KEY_EVENT_ID);

            TalonLogger.logInfo(paramMap, MSG_FUNC_ID_LOG + funcId);
            TalonLogger.logInfo(paramMap, MSG_EVENT_ID_LOG + eventId);

            TalonParamDto paramDto = TalonParamMapper.fromMap(paramMap);

            TalonLogger.logInfo(paramMap, MSG_DTO_SUCCESS);

            List<Map<String, Object>> funcEventList = null;

            if (!MAP_KEY_BUTTOM.equals(eventId)) {
                funcEventList = selectById(conn, SQL_KEY_TPI_M_FUNC_EVENT, COMPANY_CD_COMMON, funcId, eventId).getMapListResult();
            } else {
                String buttomId = getRequiredValue(paramMap, MAP_KEY_BUTTOM_ID);
                funcEventList = selectById(conn, SQL_KEY_TPI_M_FUNC_EVENT_BUTTOM, COMPANY_CD_COMMON, funcId, eventId, buttomId).getMapListResult();
            }

            for (Map<String, Object> row : funcEventList) {

                System.out.println("=== Connection isClosed: " + conn.isClosed() + " ===");
                executeLogicIfActive(conn, row, paramDto);
            }

            return EventResultDto.ok();

        } catch (Exception e) {
            e.printStackTrace();
            return EventResultDto.error(MSG_JAVA_LOGIC_ERROR + e.getMessage());
        }
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

        Map<String, Object> logicRow = selectById(conn, SQL_KEY_TPI_M_JAVA_LOGIC, "common", logicId).getMapListResult()
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
     * @param conn     DB接続
     * @param logicRow 実行対象ロジックの定義（TPI_M_JAVA_LOGIC）
     * @param paramDto 実行時引数（TalonParamDto）
     * @param logicId  ロジックID（ログ出力用）
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