var HashMap = Java.type('java.util.HashMap');
var DbTestUtil = Java.type("jp.co.technopro.talon.util.DbTestUtil");
var HenkoService = Java.type('jp.co.technopro.talon.logic.HenkoService');

var params = new HashMap();
params.put("TK_NO", "10006");
params.put("SHORI_TUKI", "202507");
params.put("HON_TAISYOKU_CD", "90");  // ← 追加
params.put("HAI_TAISYOKU_CD", "99");  // ← 追加
params.put("no", 170004);  // ← 追加

var service = new HenkoService();
var conn = DbTestUtil.getTestConnection();
service.run(conn, params, "HON_TOUROKU");
