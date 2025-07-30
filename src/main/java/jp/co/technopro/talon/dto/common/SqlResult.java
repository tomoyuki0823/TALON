package jp.co.technopro.talon.dto.common;

import java.util.List;
import java.util.Map;

/**
 * SQL実行結果を保持する共通DTOクラス。
 * <ul>
 *     <li>更新系: affectedRows, insertedId</li>
 *     <li>1件取得系: mapResult</li>
 *     <li>複数件取得系: listMapResult, size</li>
 * </ul>
 */
public class SqlResult {

    /** 更新件数（INSERT/UPDATE/DELETE） */
    private int affectedRows;

    /** 挿入時のID（自動採番など） */
    private Object insertedId;

    /** SELECT 1件用：カラム名→値 */
    private Map<String, Object> mapResult;

    /** SELECT 複数件用：行のリスト */
    private List<Map<String, Object>> mapListResult;

    /** 件数（listMapResultのサイズやCOUNT(*)） */
    private Integer size;

    // ===== コンストラクタ =====

    public SqlResult() {}

    public SqlResult(int affectedRows, Object insertedId) {
        this.affectedRows = affectedRows;
        this.insertedId = insertedId;
    }

    // ===== ゲッター・セッター =====

    public int getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public Object getInsertedId() {
        return insertedId;
    }

    public void setInsertedId(Object insertedId) {
        this.insertedId = insertedId;
    }

    public Map<String, Object> getMapResult() {
        return mapResult;
    }

    public void setMapResult(Map<String, Object> mapResult) {
        this.mapResult = mapResult;
    }


    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    // ===== 型安全な補助メソッド（mapResult専用）=====

    public String getString(String key) {
        return mapResult == null ? null : (String) mapResult.get(key);
    }

    public Integer getInt(String key) {
        return mapResult == null ? null : (Integer) mapResult.get(key);
    }

    public Boolean getBoolean(String key) {
        return mapResult == null ? null : (Boolean) mapResult.get(key);
    }

    public List<Map<String, Object>> getMapListResult() {
        return mapListResult;
    }

    public void setMapListResult(List<Map<String, Object>> mapListResult) {
        this.mapListResult = mapListResult;
    }
}
