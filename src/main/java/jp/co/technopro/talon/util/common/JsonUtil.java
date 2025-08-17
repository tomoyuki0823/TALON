package jp.co.technopro.talon.util.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Jacksonの薄いラッパ。
 */
public class JsonUtil {
    private static final ObjectMapper M = new ObjectMapper();
    private static final TypeReference<Map<String, String>> STR_MAP = new TypeReference<Map<String, String>>() {};

    /**
     * ヘッダJSONの {token} を実トークンに差し替えて Map で返す。
     * 例: { "Authorization":"Bearer {token}" } → トークン埋め込み後にMap化
     */
    public static Map<String, String> injectTokenToHeader(String headerJson, String token) {
        try {
            String replaced = headerJson == null ? "{}" : headerJson.replace("{token}", token);
            return M.readValue(replaced, STR_MAP);
        } catch (Exception e) {
            throw new IllegalArgumentException("ヘッダJSONの解析に失敗しました", e);
        }
    }
}
