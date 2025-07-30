package jp.co.technopro.talon.dto.common;

import java.util.HashMap;
import java.util.Map;

/**
 * EventLogicExecutor の戻り値DTO。
 * Java側から Nashorn へ戻す標準レスポンス構造。
 */
public class EventResultDto {
    private boolean status = true;      // 成功: true / 失敗: false
    private String message;             // エラーメッセージ（ある場合）
    private Map<String, Object> data;   // 任意の追加データ（OPTIONAL）

    public EventResultDto() {
        this.data = new HashMap<>();
    }

    public EventResultDto(boolean status, String message) {
        this.status = status;
        this.message = message;
        this.data = new HashMap<>();
    }

    public static EventResultDto ok() {
        return new EventResultDto(true, null);
    }

    public static EventResultDto error(String message) {
        return new EventResultDto(false, message);
    }

    // === getter/setter ===
    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void put(String key, Object value) {
        this.data.put(key, value);
    }

    public Object get(String key) {
        return this.data.get(key);
    }
}
