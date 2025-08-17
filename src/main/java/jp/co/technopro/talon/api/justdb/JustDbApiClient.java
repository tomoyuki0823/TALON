package jp.co.technopro.talon.api.justdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JUST-DB向けの軽量HTTPクライアント。
 * <p>JS版 {@code callApi(method, url, data, headers, ...)} をJavaへ移植。
 * JDK標準の {@link HttpURLConnection} を用いる（Java 11対応）。</p>
 */
public class JustDbApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 指定URLに対してJSONを送受信する（トークン・任意ヘッダ・タイムアウト・リトライ対応）。
     *
     * @param method           "GET" / "POST" / "PUT"
     * @param url              エンドポイントURL
     * @param data             送信するオブジェクト（GET時は無視）。null可
     * @param headers          追加ヘッダ（Authorization等）。null可
     * @param connectTimeoutMs 接続タイムアウトms（null時5000）
     * @param readTimeoutMs    読み取りタイムアウトms（null時5000）
     * @param retryCount       リトライ回数（null時3）
     * @return レスポンスJSON（JacksonのJsonNode）
     * @throws IOException 通信失敗 or HTTPエラー時
     */
    public static JsonNode callApi(String method,
                                   String url,
                                   Object data,
                                   Map<String, String> headers,
                                   Integer connectTimeoutMs,
                                   Integer readTimeoutMs,
                                   Integer retryCount) throws IOException {

        int ct = connectTimeoutMs != null ? connectTimeoutMs : 5000;
        int rt = readTimeoutMs != null ? readTimeoutMs : 5000;
        int rc = retryCount != null ? retryCount : 3;

        IOException lastError = null;

        for (int attempt = 0; attempt <= rc; attempt++) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(ct);
                conn.setReadTimeout(rt);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                if (headers != null) {
                    for (Map.Entry<String, String> e : headers.entrySet()) {
                        conn.setRequestProperty(e.getKey(), e.getValue());
                    }
                }

                if ((method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) && data != null) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream();
                         OutputStreamWriter w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                        w.write(MAPPER.writeValueAsString(data));
                        w.flush();
                    }
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String body;
                try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    body = sb.toString();
                }
                if (code >= 200 && code < 300) {
                    return MAPPER.readTree(body.isEmpty() ? "{}" : body);
                } else {
                    throw new IOException("HTTP error " + code + (body.isEmpty() ? "" : " - " + body));
                }
            } catch (IOException ex) {
                lastError = ex;
                if (attempt == rc) throw lastError;
                // retry
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        // 到達しない
        throw new IOException("Unexpected error");
    }
}
