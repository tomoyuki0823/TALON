package jp.co.technopro.talon.dto.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_CREATED_BY;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_CREATED_PRG_NM;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_UPDATED_BY;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_UPDATED_DATE;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_UPDATED_PRG_NM;

/**
 * TALON業務ロジック用共通DTOの基底クラス。
 * <p>
 * 共通的なユーザー情報・環境情報のアクセサを提供します。
 */
public abstract class AbstractTalonDto {

    /**
     * ユーザー情報（TALON.getUserInfoMap()に相当）
     */
    protected Map<String, Object> userMap;

    public Map<String, Object> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, Object> userMap) {
        this.userMap = userMap;
    }

    /**
     * ユーザーID取得（null安全）
     */
    public String getUserId() {
        return getString(MAP_KEY_USER_ID);
    }

    /**
     * 機能ID（画面ID）取得
     */
    public String getFuncId() {
        return getString(MAP_KEY_FUNC_ID);
    }

    // ==============================
    // 作成情報
    // ==============================

    /**
     * 作成日時（常に現在日時）
     */
    public Date getCreatedDate() {
        return new Date();
    }

    /**
     * 作成者ユーザーID
     */
    public String getCreatedBy() {
        return getUserId();
    }

    /**
     * 作成プログラム名
     */
    public String getCreatedPrgNm() {
        return getFuncId();
    }

    // ==============================
    // 更新情報
    // ==============================

    /**
     * 更新日時（常に現在日時）
     */
    public Date getUpdatedDate() {
        return new Date();
    }

    /**
     * 更新者ユーザーID
     */
    public String getUpdatedBy() {
        return getUserId();
    }

    /**
     * 更新プログラム名
     */
    public String getUpdatedPrgNm() {
        return getFuncId();
    }

    /**
     * 登録用：CREATED_～ / UPDATED_～ の共通マップを返す（modify_count=0含む）
     */
    public Map<String, Object> buildInsAuditMap() {
        Map<String, Object> auditMap = new HashMap<>();

        auditMap.put(MAP_KEY_CREATED_DATE, getCreatedDate());
        auditMap.put(MAP_KEY_CREATED_BY, getCreatedBy());
        auditMap.put(MAP_KEY_CREATED_PRG_NM, getCreatedPrgNm());
        auditMap.put(MAP_KEY_UPDATED_DATE, getUpdatedDate());
        auditMap.put(MAP_KEY_UPDATED_BY, getUpdatedBy());
        auditMap.put(MAP_KEY_UPDATED_PRG_NM, getUpdatedPrgNm());

        return auditMap;
    }

    /**
     * 登録用：CREATED_～ / UPDATED_～ の共通マップを返す（modify_count=0含む）
     */
    public Map<String, Object> buildUpdAuditMap() {
        Map<String, Object> auditMap = new HashMap<>();
        
        auditMap.put(MAP_KEY_UPDATED_DATE, getUpdatedDate());
        auditMap.put(MAP_KEY_UPDATED_BY, getUpdatedBy());
        auditMap.put(MAP_KEY_UPDATED_PRG_NM, getUpdatedPrgNm());

        return auditMap;
    }

    /**
     * 共通Mapキー取得ヘルパー
     */
    protected String getString(String key) {
        Object value = userMap != null ? userMap.get(key) : null;
        return value != null ? value.toString() : null;
    }
}
