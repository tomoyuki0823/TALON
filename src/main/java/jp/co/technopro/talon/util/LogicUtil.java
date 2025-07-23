package jp.co.technopro.talon.util;

import java.util.HashMap;
import java.util.Map;

public class LogicUtil {

    /**
     * ロジック共通の返却Mapを構築する（Nashorn側への返却用）
     *
     * @param success 成功フラグ
     * @param msg     メッセージ（成功またはエラー内容）
     * @return Map形式の返却値（"success", "msg" を含む）
     */
    public static Map<String, Object> buildResult(boolean success, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("msg", msg);
        return result;
    }
}
