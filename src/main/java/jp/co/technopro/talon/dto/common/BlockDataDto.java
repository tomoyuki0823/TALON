package jp.co.technopro.talon.dto.common;

import java.util.List;
import java.util.Map;

/**
 * BLOCKデータを表すDTO。
 * <p>
 * タイプが "CARD" の場合は {@code cardData} に Map を、
 * "LIST" の場合は {@code listData} に List<Map> を格納する。
 */
public class BlockDataDto {

    /** "CARD" または "LIST" */
    private String type;

    /** CARD用データ */
    private Map<String, Object> cardData;

    /** LIST用データ */
    private List<Map<String, Object>> listData;

    // --- 判定メソッド ---

    /**
     * CARD形式であるか判定します。
     */
    public boolean isCard() {
        return "CARD".equalsIgnoreCase(type);
    }

    /**
     * LIST形式であるか判定します。
     */
    public boolean isList() {
        return "LIST".equalsIgnoreCase(type);
    }

    // --- Getter / Setter ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getCardData() {
        return cardData;
    }

    public void setCardData(Map<String, Object> cardData) {
        this.cardData = cardData;
    }

    public List<Map<String, Object>> getListData() {
        return listData;
    }

    public void setListData(List<Map<String, Object>> listData) {
        this.listData = listData;
    }
}
