package jp.co.technopro.talon.logic.common;

import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.common.EventResultDto;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Talonイベントに対応するJavaロジック共通インターフェース。
 * すべてのロジックは {@link EventResultDto} を返却する必要があります。
 */
public interface ExecutableLogic {

    /**
     * 実行ロジック。
     *
     * @param conn      DBコネクション
     * @param paramDto  入力パラメータ
     * @return 処理結果（成功／失敗とメッセージを含む）
     * @throws SQLException DB操作時の例外
     */
    EventResultDto run(Connection conn, TalonParamDto paramDto) throws Exception;
}
