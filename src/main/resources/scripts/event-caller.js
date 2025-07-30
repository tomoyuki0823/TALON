function callEventLogicById(EVENT_ID) {
    var paramMap = new java.util.HashMap();

    var DbTestUtil = Java.type("jp.co.technopro.talon.util.DbTestUtil");
    paramMap.put("FUNC_ID", "TK_SHINKI_05");
    paramMap.put("EVENT_ID", "BLOCK_BEFORE");

    // === targetMap を定義 ===
    var targetMap = new java.util.HashMap();
    targetMap.put("SITYOSON_NM", "札幌市豊平区");
    targetMap.put("HAI_IRYOHI", null);
    targetMap.put("HON_TAIKAI_JIYU", "");
    targetMap.put("SHITEN_CD", "009");
    targetMap.put("CREATED_DATE", new java.util.Date());
    targetMap.put("FUYO_BIRTH", "");
    targetMap.put("HAI_TAIKAI_JIYU", "");
    targetMap.put("HAI_ZOKU", "");
    targetMap.put("SHOKU_MEI", "課長");
    targetMap.put("HAI_KANYUBI", "");
    targetMap.put("JGY_KBN", "1");
    targetMap.put("SIBU_NM", "札幌豊平");
    targetMap.put("HAI_IKIGAI", null);
    targetMap.put("HAI_SEINENGAPI", "");
    targetMap.put("UPDATED_PRG_NM", "TK_SHINKI_05");
    targetMap.put("modify_count", "1");
    targetMap.put("FUYO_KANA_NM", "");
    targetMap.put("SHOZOKU_MEI", "");
    targetMap.put("CREATED_BY", "10222");
    targetMap.put("HAI_TAIKAI_YOTEIBI", "");
    targetMap.put("HAI_NINTEI", "");
    targetMap.put("SHITEN_NM", "東京営業部");
    targetMap.put("HON_IKIGAI", "80000");
    targetMap.put("HON_ZOKU", "0");
    targetMap.put("GINKOU_NM", "みずほ");
    targetMap.put("KOUZA_NO", "2511574");
    targetMap.put("HON_TAIKAI_YOTEIBI", "");
    targetMap.put("TEL2", "");
    targetMap.put("HON_SIMEI", "木藤　實　　　　　　");
    targetMap.put("YUBIN_NO", "2510057");
    targetMap.put("SIBU_CD", "007");
    targetMap.put("UPDATED_BY", "10222");
    targetMap.put("JUSHO2", "1112");
    targetMap.put("JUSHO1", "藤沢市城南");
    targetMap.put("GOJYO_TAIKAI_WAREKI", " ");
    targetMap.put("no", "18686");
    targetMap.put("SICHOSON_CD", "107");
    targetMap.put("HON_TAIKAI_SEINENGAPI", "");
    targetMap.put("SIBU_HIKANYU", "");
    targetMap.put("HAI_KANYU_UMU", "1");
    targetMap.put("GINKOU_CD", "0001");
    targetMap.put("HON_IRYOHI", "240000");
    targetMap.put("TK_NO", "999999");
    targetMap.put("HON_SEIBETU", "M");
    targetMap.put("FUYO_SEIBETSU", "");
    targetMap.put("HON_SIBOBI", "");
    targetMap.put("CMT3", "");
    targetMap.put("CMT2", "");
    targetMap.put("CMT1", "");
    targetMap.put("HON_SEINENGAPI", "3210114");
    targetMap.put("HON_KANYU_SEINENGAPI", "5046841");
    targetMap.put("KOUZAMEIGI", "");
    targetMap.put("HAI_NINTEI_NO", "");
    targetMap.put("HAI_KANA_SIMEI", "");
    targetMap.put("SHUBETU", "1");
    targetMap.put("TALON_DELETE_FLG", "");
    targetMap.put("HON_KANA_SIMEI", "ｷﾄｳ ﾐﾉﾙ");
    targetMap.put("HAI_SIMEI", "");
    targetMap.put("GOJYO_KANYU_BI_WAREKI", " ");
    targetMap.put("SHORI_TUKI", "202508");
    targetMap.put("HON_KANYU_NENREI", "");
    targetMap.put("HAI_KANYU_NENREI", "");
    targetMap.put("UPDATED_DATE", new java.util.Date());
    targetMap.put("TEL", "09063150504");
    targetMap.put("CREATED_PRG_NM", "TK_SHINKI_05");
    targetMap.put("FUYO_KANJI_NM", "");
    targetMap.put("HAI_SEIBETU", "");

    paramMap.put("TARGET_DATA", targetMap);

    // DB接続
    var executorClass = Java.type("jp.co.technopro.talon.logic.EventLogicExecutor");
    var conn = DbTestUtil.getTestConnection();

    executorClass.executeEventLogic(conn, paramMap);
}
