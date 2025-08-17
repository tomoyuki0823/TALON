package jp.co.technopro.talon.logic.common;

import jp.co.technopro.logger.TpiLogger;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.mapper.common.TalonParamMapper;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jp.co.technopro.talon.consts.tln.TlnMessageConst.*;
import static jp.co.technopro.talon.consts.tln.TlnSqlXmlKeyConst.*;
import static jp.co.technopro.talon.consts.tln.TlnCompanyConst.COMPANY_CD_COMMON;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;
import static jp.co.technopro.talon.util.common.DbUtil.selectById;

/**
 * イベント実行制御を行うロジッククラス。
 * <p>
 * 機能ID（FUNC_ID）・イベントID（EVENT_ID）に紐づく複数の Java ロジックを
 * TPI_M_FUNC_EVENT → TPI_M_JAVA_LOGIC の定義順に実行する。
 * <br>
 * 例外時のロールバックは呼び出し元で制御する前提。
 */
public class EventLogicExecutor {

    private static final TpiLogger log = TpiLogger.getLogger(EventLogicExecutor.class);

    /** リフレクション対象クラスの簡易キャッシュ */
    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 指定された機能ID・イベントIDに対応する Java ロジックを順番に呼び出す。
     *
     * @param conn     DB接続
     * @param paramMap パラメータマップ。FUNC_ID, EVENT_ID, 各種BLOCK情報などを含む。
     * @return 実行結果（status: true/false, message: 実行メッセージ）
     */
    public static EventResultDto executeEventLogic(Connection conn, Map<String, Object> paramMap) {
        final String funcId;
        final String eventId;

        try {
            log.classStart("EventLogicExecutor");
            log.methodStart("executeEventLogic");
            log.info("=== paramMap 内容 ===");
            if (paramMap != null) {
                for (Map.Entry<String, Object> e : paramMap.entrySet()) {
                    Object v = e.getValue();
                    log.info(e.getKey() + " => " + (v != null ? (v.getClass().getName() + ": " + v) : "null"));
                }
            }
            log.info("=== paramMap END ===");

            funcId = getRequiredValue(paramMap, MAP_KEY_FUNC_ID);
            eventId = getRequiredValue(paramMap, MAP_KEY_EVENT_ID);

            log.info(MSG_FUNC_ID_LOG + funcId);
            log.info(MSG_EVENT_ID_LOG + eventId);

            TalonParamDto paramDto = TalonParamMapper.fromMap(paramMap);
            log.info(MSG_DTO_SUCCESS);

            // イベント定義行を取得
            final List<Map<String, Object>> funcEventList;
            if (!MAP_KEY_BUTTOM.equals(eventId)) {
                funcEventList = selectById(conn, SQL_KEY_TPI_M_FUNC_EVENT, COMPANY_CD_COMMON, funcId, eventId)
                        .getMapListResult();
            } else {
                String buttomId = getRequiredValue(paramMap, MAP_KEY_BUTTOM_ID);
                funcEventList = selectById(conn, SQL_KEY_TPI_M_FUNC_EVENT_BUTTOM, COMPANY_CD_COMMON, funcId, eventId, buttomId)
                        .getMapListResult();
            }

            if (funcEventList == null || funcEventList.isEmpty()) {
                log.warn("イベントに紐づくJavaロジック定義が見つかりませんでした。funcId=" + funcId + ", eventId=" + eventId);
                // 何も実行せず正常終了（要件に応じて error にしてもよい）
                return EventResultDto.ok("実行対象のロジック定義がありませんでした。");
            }

            // 定義順に実行。失敗したら即終了（ポリシーA）
            for (Map<String, Object> row : funcEventList) {
                EventResultDto result = executeLogicIfActive(conn, row, paramDto);
                if (result != null && !result.getStatus()) {
                    log.warn("ロジック実行中断: status=false logicId=" + paramDto.getLogicId());
                    return result; // 先頭の失敗を返却
                }
            }

            return EventResultDto.ok();

        } catch (IllegalArgumentException iae) {
            log.error("必須パラメータ不足: " + iae.getMessage());
            return EventResultDto.error(MSG_JAVA_LOGIC_ERROR + iae.getMessage());

        } catch (Exception e) {
            log.error("Javaロジック実行で例外発生: " + e.getMessage(), e);
            return EventResultDto.error(MSG_JAVA_LOGIC_ERROR + e.getMessage());
        }
    }

    /**
     * ロジックが有効（IS_ACTIVE=1）の場合に限り、指定されたロジックを実行する。
     *
     * @return 実行結果。スキップ時は null（= 何もしていない）
     */
    private static EventResultDto executeLogicIfActive(Connection conn, Map<String, Object> funcEventRow, TalonParamDto paramDto) throws Exception {
        String logicId = (String) funcEventRow.get(MAP_KEY_LOGIC_ID);
        paramDto.setLogicId(logicId);

        // 会社コードは COMMON に統一
        Map<String, Object> logicRow = selectById(conn, SQL_KEY_TPI_M_JAVA_LOGIC, COMPANY_CD_COMMON, logicId)
                .getMapListResult()
                .stream()
                .findFirst()
                .orElse(null);

        if (logicRow == null || !MAP_KEY_IS_ACTIVE_ON.equals(String.valueOf(logicRow.get(MAP_KEY_IS_ACTIVE)).trim())) {
            log.info("LogicID [" + logicId + "] は無効または存在しないためスキップします。");
            return null;
        }

        return invokeLogic(conn, logicRow, paramDto, logicId);
    }

    /**
     * Javaクラスをリフレクションで呼び出し、ロジックを実行する。
     * 呼び出し対象は AbstractLogicBase を継承している必要がある。
     */
    private static EventResultDto invokeLogic(Connection conn, Map<String, Object> logicRow, TalonParamDto paramDto, String logicId) throws Exception {
        String className = (String) logicRow.get(MAP_KEY_CLASS_NAME);

        // クラスロード（キャッシュ利用）
        Class<?> clazz = CLASS_CACHE.computeIfAbsent(className, k -> {
            try { return Class.forName(k); }
            catch (ClassNotFoundException e) { throw new RuntimeException(e); }
        });

        Object instance = clazz.getDeclaredConstructor().newInstance();

        if (!(instance instanceof AbstractLogicBase)) {
            throw new IllegalStateException("クラス " + className + " は AbstractLogicBase を継承していません。");
        }

        AbstractLogicBase logic = (AbstractLogicBase) instance;
        EventResultDto result = logic.run(conn, paramDto);

        log.info("Logic [" + logicId + "] executed. status=" + result.getStatus()
                + (result.getMessage() != null ? (", msg=" + result.getMessage()) : ""));
        return result;
    }

    /**
     * Map から必須キーを取得し、未設定の場合は例外をスローする（String専用）。
     */
    private static String getRequiredValue(Map<String, Object> map, String key) {
        if (map == null) throw new IllegalArgumentException("paramMap が未設定です。");
        Object v = map.get(key);
        String value = (v instanceof String) ? (String) v : null;
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(key + " が未指定です。");
        }
        return value.trim();
    }
}
