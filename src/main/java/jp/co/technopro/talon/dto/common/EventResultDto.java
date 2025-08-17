package jp.co.technopro.talon.dto.common;

import java.util.HashMap;
import java.util.Map;

/**
 * EventLogicExecutor の戻り値DTO。
 * Java側から Nashorn へ戻す標準レスポンス構造（DTO基準）。
 */
public class EventResultDto {
    private boolean status = true;      // 成功: true / 失敗: false
    private String message;             // メッセージ（エラー／情報）
    private Map<String, Object> data;   // 任意データ（OPTIONAL）

    public EventResultDto() {
        this.data = new HashMap<>();
    }

    public EventResultDto(boolean status, String message) {
        this.status = status;
        this.message = message;
        this.data = new HashMap<>();
    }

    public EventResultDto(boolean status, String message, Map<String, Object> data) {
        this.status = status;
        this.message = message;
        this.data = (data != null) ? data : new HashMap<>();
    }

    // ---- factories --------------------------------------------------------
    public static EventResultDto ok() {
        return new EventResultDto(true, null);
    }

    public static EventResultDto ok(String message) {
        return new EventResultDto(true, message);
    }

    public static EventResultDto error(String message) {
        return new EventResultDto(false, message);
    }

    public static EventResultDto error(String message, Map<String, Object> data) {
        return new EventResultDto(false, message, data);
    }

    // ---- fluent helpers ---------------------------------------------------
    /** data に 1件追加して this を返す（流れるように組み立て可能） */
    public EventResultDto with(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
        return this;
    }

    // ---- getters/setters --------------------------------------------------
    public boolean getStatus() { return status; }
    public boolean isSuccess() { return status; }     // アクセサ別名（お好みで）

    public void setStatus(boolean status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = (data != null) ? data : new HashMap<>(); }

    public void put(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
    }

    public Object get(String key) {
        return (this.data != null) ? this.data.get(key) : null;
    }
}
