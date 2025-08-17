package jp.co.technopro.logger;

import org.apache.log4j.Logger;

/**
 * アプリケーション共通ログラッパークラス。
 * Log4j をラップし、統一フォーマットでログ出力を提供します。
 */
public class TpiLogger {

    private final Logger logger;

    /** 指定クラス用のロガーを取得します。 */
    public static TpiLogger getLogger(Class<?> clazz) {
        return new TpiLogger(clazz);
    }

    private TpiLogger(Class<?> clazz) {
        this.logger = Logger.getLogger(clazz);
    }

    // =========================================================
    // 共通ログ API
    // =========================================================

    /** クラス開始ログ */
    public void classStart(String className) {
        logger.info("[CLASS-START] " + className);
    }

    /** メソッド開始ログ（メソッド名自動取得） */
    public void methodStart() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("[METHOD-START] " + methodName);
    }

    /** メソッド開始ログ（補足付き） */
    public void methodStart(String additionalMsg) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("[METHOD-START] " + methodName + " - " + additionalMsg);
    }

    /** INFOログ */
    public void info(String msg) {
        logger.info("[INFO] " + msg);
    }

    /** WARNログ */
    public void warn(String msg) {
        logger.warn("[WARN] " + msg);
    }

    /** ERRORログ（メッセージのみ） */
    public void error(String msg) {
        logger.error("[ERROR] " + msg);
    }

    /** ERRORログ（例外付き） */
    public void error(String msg, Throwable t) {
        logger.error("[ERROR] " + msg, t);
    }

    /** DEBUGログ */
    public void debug(String msg) {
        logger.debug("[DEBUG] " + msg);
    }
}
