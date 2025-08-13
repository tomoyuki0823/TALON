package jp.co.technopro.talon.config;

import org.apache.log4j.Logger;

/**
 * TpiLogger は Log4J v1 を使用した独自のロガーラッパークラスです。
 * <p>
 * TALON とは独立したログ出力を実現し、ログレベルや形式の統一、将来的な差し替えを容易にします。
 */
public class TpiLogger {

    private final Logger logger;

    private TpiLogger(Class<?> clazz) {
        this.logger = Logger.getLogger(clazz);
    }

    /**
     * 指定されたクラス名に基づいて TpiLogger を生成します。
     *
     * @param clazz 呼び出し元のクラス
     * @return TpiLogger インスタンス
     */
    public static TpiLogger getLogger(Class<?> clazz) {
        return new TpiLogger(clazz);
    }

    /**
     * INFO レベルのログを出力します。
     *
     * @param msg ログメッセージ
     */
    public void info(String msg) {
        logger.info("[INFO] " + msg);
    }

    /**
     * DEBUG レベルのログを出力します。
     *
     * @param msg ログメッセージ
     */
    public void debug(String msg) {
        logger.debug("[DEBUG] " + msg);
    }

    /**
     * WARN レベルのログを出力します。
     *
     * @param msg ログメッセージ
     */
    public void warn(String msg) {
        logger.warn("[WARN] " + msg);
    }

    /**
     * ERROR レベルのログを出力します。
     *
     * @param msg ログメッセージ
     */
    public void error(String msg) {
        logger.error("[ERROR] " + msg);
    }

    /**
     * ERROR レベルで例外付きのログを出力します。
     *
     * @param msg ログメッセージ
     * @param t   Throwable（例外）
     */
    public void error(String msg, Throwable t) {
        logger.error("[ERROR] " + msg, t);
    }
}
