var HashMap = Java.type('java.util.HashMap');
var YotakuService = Java.type('jp.co.technopro.talon.logic.YotakuService');

// パラメータ作成
var params = new HashMap();
params.put("TK_NO", "10001");

// Java 側の run メソッドを呼び出し
var service = new YotakuService();
service.run(conn, params, "YOTAKU_YOTEI");
