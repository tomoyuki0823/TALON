package jp.co.technopro.logger;

import jp.co.technopro.talon.dto.common.TalonParamDto;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@code TalonLogger} は、TALONログ出力を簡素化するユーティリティクラスです。
 * Mapベースおよび DTOベース（TalonParamDto）両方に対応します。
 */
public class TalonLogger {

    // --- Mapベースログ出力 ---
    public static void logInfo(Map<String, Object> paramMap, String msg) {
        Object logger = paramMap.get("LOGGER");
        invokeLogMethod(logger, "writeInfo", msg);
    }

    public static void logError(Map<String, Object> paramMap, String msg) {
        Object logger = paramMap.get("LOGGER");
        invokeLogMethod(logger, "writeError", msg);
    }

    // --- DTOベースログ出力 ---
    public static void logInfo(TalonParamDto dto, String msg) {
        invokeLogMethod(dto.getLogger(), "writeInfo", msg);
    }

    public static void logError(TalonParamDto dto, String msg) {
        invokeLogMethod(dto.getLogger(), "writeError", msg);
    }

    // --- 共通ログ出力処理 ---
    private static void invokeLogMethod(Object logger, String methodName, String msg) {
        if (logger == null) {
            System.out.println("[WARN] LOGGER is null: " + msg);
            return;
        }

        try {
            Method method = logger.getClass().getMethod(methodName, String.class);
            method.invoke(logger, msg);
        } catch (Exception e) {
            System.out.println("[ERROR] ログ出力に失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
