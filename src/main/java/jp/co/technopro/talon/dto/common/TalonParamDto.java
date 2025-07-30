package jp.co.technopro.talon.dto.common;

import java.util.List;
import java.util.Map;

/**
 * TALON → Java へのパラメータDTO。
 * BLOCK情報やユーザー情報など、汎用的に受け取る構造。
 */
public class TalonParamDto {

    // === 基本情報 ===
    private String eventId;
    private String logicId;
    private Map<String, Object> userMap;

    // === データ関連 ===
    private Map<String, Object> targetData;
    private Map<String, Object> conditionData;
    private List<Map<String, Object>> blockMeta;

    private Map<String, Object> block1;
    private Map<String, Object> block2;
    private Map<String, Object> block3;
    private Map<String, Object> block4;
    private Map<String, Object> block5;
    private Map<String, Object> block6;
    private Map<String, Object> block7;
    private Map<String, Object> block8;
    private Map<String, Object> block9;

    // === TALON制御関連 ===
    /** Talonの実行イベントID（例: "onButton"） */
    private String tlnEventId;

    /** 登録処理かどうか（Talon.isInsert()） */
    private boolean tlnIsInsert;

    /** 更新処理かどうか（Talon.isUpdate()） */
    private boolean tlnIsUpdate;

    /** 削除処理かどうか（Talon.isDelete()） */
    private boolean tlnIsDelete;

    /** Talonのセッション情報 */
    private List<Map<String, Object>> tlnSession;

    // === 環境・識別関連 ===
    private String buttomId;
    private Object logger;
    private String companyCode;
    private String companyCodeCommon;

    // === Getter / Setter ===
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getLogicId() {
        return logicId;
    }

    public void setLogicId(String logicId) {
        this.logicId = logicId;
    }

    public Map<String, Object> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, Object> userMap) {
        this.userMap = userMap;
    }

    public Map<String, Object> getTargetData() {
        return targetData;
    }

    public void setTargetData(Map<String, Object> targetData) {
        this.targetData = targetData;
    }

    public Map<String, Object> getConditionData() {
        return conditionData;
    }

    public void setConditionData(Map<String, Object> conditionData) {
        this.conditionData = conditionData;
    }

    public List<Map<String, Object>> getBlockMeta() {
        return blockMeta;
    }

    public void setBlockMeta(List<Map<String, Object>> blockMeta) {
        this.blockMeta = blockMeta;
    }

    public Map<String, Object> getBlock1() {
        return block1;
    }

    public void setBlock1(Map<String, Object> block1) {
        this.block1 = block1;
    }

    public Map<String, Object> getBlock2() {
        return block2;
    }

    public void setBlock2(Map<String, Object> block2) {
        this.block2 = block2;
    }

    public Map<String, Object> getBlock3() {
        return block3;
    }

    public void setBlock3(Map<String, Object> block3) {
        this.block3 = block3;
    }

    public Map<String, Object> getBlock4() {
        return block4;
    }

    public void setBlock4(Map<String, Object> block4) {
        this.block4 = block4;
    }

    public Map<String, Object> getBlock5() {
        return block5;
    }

    public void setBlock5(Map<String, Object> block5) {
        this.block5 = block5;
    }

    public Map<String, Object> getBlock6() {
        return block6;
    }

    public void setBlock6(Map<String, Object> block6) {
        this.block6 = block6;
    }

    public Map<String, Object> getBlock7() {
        return block7;
    }

    public void setBlock7(Map<String, Object> block7) {
        this.block7 = block7;
    }

    public Map<String, Object> getBlock8() {
        return block8;
    }

    public void setBlock8(Map<String, Object> block8) {
        this.block8 = block8;
    }

    public Map<String, Object> getBlock9() {
        return block9;
    }

    public void setBlock9(Map<String, Object> block9) {
        this.block9 = block9;
    }

    public String getTlnEventId() {
        return tlnEventId;
    }

    public void setTlnEventId(String tlnEventId) {
        this.tlnEventId = tlnEventId;
    }

    public boolean isTlnIsInsert() {
        return tlnIsInsert;
    }

    public void setTlnIsInsert(boolean tlnIsInsert) {
        this.tlnIsInsert = tlnIsInsert;
    }

    public boolean isTlnIsUpdate() {
        return tlnIsUpdate;
    }

    public void setTlnIsUpdate(boolean tlnIsUpdate) {
        this.tlnIsUpdate = tlnIsUpdate;
    }

    public boolean isTlnIsDelete() {
        return tlnIsDelete;
    }

    public void setTlnIsDelete(boolean tlnIsDelete) {
        this.tlnIsDelete = tlnIsDelete;
    }

    public List<Map<String, Object>> getTlnSession() {
        return tlnSession;
    }

    public void setTlnSession(List<Map<String, Object>> tlnSession) {
        this.tlnSession = tlnSession;
    }

    public String getButtomId() {
        return buttomId;
    }

    public void setButtomId(String buttomId) {
        this.buttomId = buttomId;
    }

    public Object getLogger() {
        return logger;
    }

    public void setLogger(Object logger) {
        this.logger = logger;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyCodeCommon() {
        return companyCodeCommon;
    }

    public void setCompanyCodeCommon(String companyCodeCommon) {
        this.companyCodeCommon = companyCodeCommon;
    }
}
