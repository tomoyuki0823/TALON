package jp.co.technopro.talon.api.justdb.dto;

import java.util.Objects;

/**
 * MST_CALL_API_PARAM の1レコードDTO。
 */
public class ApiCallParam {
    private String method;
    private String url;
    private String headerJson; // DBに格納されたヘッダJSON文字列（{ "Authorization": "Bearer {token}" }等）

    public ApiCallParam(String method, String url, String headerJson) {
        this.method = method;
        this.url = url;
        this.headerJson = headerJson;
    }

    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public String getHeaderJson() { return headerJson; }

    @Override public String toString() {
        return "ApiCallParam{" + "method='" + method + '\'' + ", url='" + url + '\'' + '}';
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiCallParam)) return false;
        ApiCallParam that = (ApiCallParam) o;
        return Objects.equals(method, that.method) && Objects.equals(url, that.url) && Objects.equals(headerJson, that.headerJson);
    }
    @Override public int hashCode() {
        return Objects.hash(method, url, headerJson);
    }
}
