/**
 * ボタン押下時に呼び出される標準イベント関数。
 */
function onButton() {
    onEvent("BUTTON");
}

/**
 * 登録処理の直前に呼び出される標準イベント関数。
 */
function onBlockBefore() {
    onEvent("BLOCK_BEFORE");
}

/**
 * 登録処理の直後に呼び出される標準イベント関数。
 */
function onBlockAfter() {
    onEvent("BLOCK_AFTER");
}

/**
 * 登録処理の直前に呼び出される標準イベント関数。
 */
function onRegisterBefore() {
    onEvent("REGISTER_BEFORE");
}

/**
 * 登録処理の直後に呼び出される標準イベント関数。
 */
function onRegisterAfter() {
    onEvent("REGISTER_AFTER");
}

/**
 * 検索処理の直前に呼び出される標準イベント関数。
 */
function onSearchBefore() {
    // トランザクション処理開始
    TalonDbUtil.begin(TALON.getDbConfig());
    onEvent("SEARCH_BEFORE");
    TalonDbUtil.commit(TALON.getDbConfig());
}

/**
 * 検索処理の直後に呼び出される標準イベント関数。
 */
function onSearchAfter() {
    onEvent("SEARCH_AFTER");
}

/**
 * 指定した EVENT_ID をもとに Java ロジックを呼び出します。
 * Talon の標準イベント関数から呼び出されます。
 *
 * @param eventId イベントID（例: "BUTTON", "REGISTER_BEFORE"）
 */
function onEvent(eventId) {
    callEventLogicById(eventId);
}

/**
 * 指定された EVENT_ID に基づき、対応する Java ロジックを呼び出します。
 * ユーザー情報から FUNC_ID を自動取得し、BLOCKデータ・検索条件・対象データなどを
 * paramMap に詰めたうえで Java 側の EventLogicExecutor に委譲します。
 *
 * @param EVENT_ID 処理対象イベントID（例: "BUTTON", "REGISTER_AFTER"）
 */
function callEventLogicById(EVENT_ID) {
    var log = TALON.getLogger();

    // === FUNC_ID 自動取得 ===
    var FUNC_ID = TALON.getUserInfoMap()["FUNC_ID"];
    if (!FUNC_ID) {
        TALON.addErrorMsg("【エラー】ユーザー情報から FUNC_ID を取得できませんでした。");
        return;
    }

    if (!EVENT_ID) {
        TALON.addErrorMsg("【エラー】EVENT_ID が未指定です。");
        return;
    }

    // イベントマスタにデータ詰まれていない場合は処理を実施しない。
    if (chkTpiEventMst(FUNC_ID, EVENT_ID)) return;

    var paramMap = new java.util.HashMap();

    // === BLOCKデータ取得（BLOCK1〜BLOCK9）===
    var blockMetaList = getBlockInfo(FUNC_ID);
    for (var i = 0; i < blockMetaList.length && i < 9; i++) {
        var block = blockMetaList[i];
        var blockType = block["TYPE"];
        var blockNo = i + 1;
        var blockKey = "BLOCK" + blockNo;

        var blockData = null;
        if (blockType === "CARD") {
            blockData = TALON.getBlockData_Card(blockNo);
        } else if (blockType === "LIST") {
            blockData = TALON.getBlockData_List(blockNo);
        }

        if (blockData !== null) {
            var wrapper = new java.util.HashMap();
            wrapper.put("TYPE", blockType);
            wrapper.put("DATA", blockData);
            paramMap.put(blockKey, wrapper);
        }
    }

    // === 各種データ詰め込み ===
    paramMap.put("FUNC_ID", FUNC_ID);
    paramMap.put("EVENT_ID", EVENT_ID);
    paramMap.put("BLOCK_META", blockMetaList);
    paramMap.put("CONDITION_DATA", TALON.getConditionData());
    paramMap.put("USER_MAP", TALON.getUserInfoMap());
    paramMap.put("TARGET_DATA", TALON.getTargetData());
    paramMap.put("BUTTOM_ID", TALON.getButtonName());
    paramMap.put("TLN_EVENT_ID", TALON.getEvent());
    paramMap.put("TLN_IS_INSERT", TALON.isInsert());
    paramMap.put("TLN_IS_UPDATE", TALON.isUpdate());
    paramMap.put("TLN_IS_DELETE", TALON.isDelete());
    paramMap.put("TLN_SESSION", TALON.getUserSessions(null));
    paramMap.put("LOGGER", log);
    paramMap.put("COMPANY_CODE", "Gojo");
    paramMap.put("COMPANY_CODE_COMMON", "COMMON");

    // === Java 実行ロジック呼び出し ===
    try {
        // コミット
        var executorClass = Java.type("jp.co.technopro.talon.logic.common.EventLogicExecutor");
        var resultDto = executorClass.executeEventLogic(
            TALON.getDbConfig().getDataSource().getConnection(),
            paramMap
        );

        if (resultDto != null && !resultDto.getStatus()) {
            var msg = resultDto.getMessage() || "処理に失敗しました。";
            TALON.addErrorMsg(msg);
            TALON.setIsSuccess(false);
        }

    } catch (e) {
        TALON.addErrorMsg("Javaロジック呼び出し時にエラーが発生しました: " + e.message);
        log.writeError("Javaロジック呼び出し失敗: " + e);
    }
}

function chkTpiEventMst(FUNC_ID, EVENT_ID) {

    var whereMap = {
        FUNC_ID: FUNC_ID,
        EVENT_ID: EVENT_ID
    }

    return getCount(TALON.getDbConfig(), "TPI_M_FUNC_EVENT", whereMap) == 0

}

function getBlockInfo(FUNC_ID) {

    var sql = " SELECT * FROM TLN_M_GENERAL_FUNC_SQL WHERE FUNC_ID ='" + FUNC_ID + "' AND FUNC_SEQ > -1 ORDER BY FUNC_SEQ"
    return TalonDbUtil.select(TALON.getDbConfig(), sql);
}



/**
 * 指定したテーブルと条件に基づいて件数（COUNT）を取得します。
 *
 * @param {Object} conn - TALONのDBコネクションオブジェクト
 * @param {string} tableName - 対象のテーブル名
 * @param {Object} whereMap - WHERE句の条件（キー: カラム名、値: 条件値）
 * @returns {number} 件数（取得できなかった場合は0）
 */
function getCount(conn, tableName, whereMap) {
    if (!conn) {
        TALON.addErrorMsg("getCount: DBコネクションが未指定です。");
        return 0;
    }

    if (!tableName || typeof tableName !== 'string') {
        TALON.addErrorMsg("getCount: テーブル名が不正です。");
        return 0;
    }

    var whereSql = "";
    var keys = Object.keys(whereMap || {});
    for (var i = 0; i < keys.length; i++) {
        var key = keys[i];
        var val = String(whereMap[key]).replace(/'/g, "''"); // SQLインジェクション対策
        if (i > 0) {
            whereSql += " AND ";
        }
        whereSql += key + " = '" + val + "'";
    }

    var sql = "SELECT COUNT(*) AS CNT FROM " + tableName;
    if (whereSql !== "") {
        sql += " WHERE " + whereSql;
    }

    var result = TalonDbUtil.select(conn, sql);

    if (!result || result.length === 0) {
        return 0;
    }

    var cnt = result[0]["CNT"];
    return cnt != null ? Number(cnt) : 0;
}


/**
 * SQL Server向けに単一レコードをSELECTする
 *
 * @param {Object} conn - DB接続設定
 * @param {string} tableName - テーブル名
 * @param {Array<string>} [columns] - 取得カラム（nullまたは空なら *）
 * @param {Object} whereMap - WHERE条件（必須）
 * @param {string} [orderBy] - ORDER BY句（省略可能）
 * @returns {Object|null} 取得できたレコード、なければnull
 */
function selectOne(conn, tableName, columns, whereMap, orderBy) {
    var sql = buildSimpleSelectSQL(tableName, columns, whereMap, orderBy, 1);
    var list = TalonDbUtil.select(conn, sql);
    if (!list || list.length === 0) {
        return null;
    }
    return list[0];
}

/**
 * SQL Server向けに複数レコードをSELECTする
 *
 * @param {Object} conn - DB接続設定
 * @param {string} tableName - テーブル名
 * @param {Array<string>} [columns] - 取得カラム（nullまたは空なら *）
 * @param {Object} whereMap - WHERE条件（必須）
 * @param {string} [orderBy] - ORDER BY句（省略可能）
 * @returns {Array<Object>} 取得できたレコードリスト（0件なら空配列）
 */
function selectList(conn, tableName, columns, whereMap, orderBy) {
    var sql = buildSimpleSelectSQL(tableName, columns, whereMap, orderBy, null); // TOP制限なし
    var list = TalonDbUtil.select(conn, sql);
    return list || [];
}

/**
 * 単純なSELECT文（SQL Server向け / WHERE必須 / 実パラメータ埋め込み / SQL文字列を直接返却）
 *
 * @param {string} tableName - テーブル名
 * @param {Array<string>} [columns] - 取得カラムリスト（nullまたは空なら *）
 * @param {Object} whereMap - WHERE条件（必須、JavaのMapにも対応）
 * @param {string} [orderBy] - ORDER BY句
 * @param {number} [top] - 取得件数制限（TOP句）
 * @returns {string} 完成したSQL文字列
 */
function buildSimpleSelectSQL(tableName, columns, whereMap, orderBy, top) {
    if (!tableName) {
        throw new Error("テーブル名は必須です。");
    }
    if (isEmptyMap(whereMap)) {
        throw new Error("WHERE条件が必須です。");
    }

    var isNumeric = function (value) {
        if (typeof value === "number") return true;
        if (value === null || value === undefined) return false;
        try {
            var clsName = value.getClass().getName();
            return clsName === "java.math.BigDecimal"
                || clsName === "java.lang.Integer"
                || clsName === "java.lang.Long"
                || clsName === "java.lang.Double";
        } catch (e) {
            return false;
        }
    };

    var selectClause = "SELECT ";
    if (top && typeof top === "number") {
        selectClause += "TOP " + top + " ";
    }

    selectClause += (!columns || columns.length === 0) ? "*" : columns.join(", ");
    var sql = selectClause + " FROM " + tableName;

    var whereParts = [];

    var keyIter = getMapKeys(whereMap);
    for (var i = 0; i < keyIter.length; i++) {
        var key = keyIter[i];
        var value = whereMap[key];

        if (value === null || value === undefined) {
            whereParts.push(key + " IS NULL");
        } else if (isNumeric(value)) {
            whereParts.push(key + " = " + String(value));
        } else {
            var escaped = String(value).replace(/'/g, "''");
            whereParts.push(key + " = '" + escaped + "'");
        }
    }

    sql += " WHERE " + whereParts.join(" AND ");

    if (orderBy) {
        sql += " ORDER BY " + orderBy;
    }

    return sql;
}

/**
 * Mapが空かどうかを判定（Java Map対応）
 * @param {Object} map
 * @returns {boolean}
 */
function isEmptyMap(map) {
    if (!map) return true;
    if (typeof map.size === "function") {
        return map.size() === 0;
    }
    if (typeof map.keySet === "function" && typeof map.keySet().isEmpty === "function") {
        return map.keySet().isEmpty();
    }
    return Object.keys(map).length === 0;
}

/**
 * Java MapまたはJSオブジェクトのキー一覧を配列で取得
 * @param {Object} map
 * @returns {Array<string>}
 */
function getMapKeys(map) {
    if (typeof map.keySet === "function" && typeof map.get === "function") {
        var iter = map.keySet().iterator();
        var result = [];
        while (iter.hasNext()) {
            var key = iter.next();
            result.push(String(key));
        }
        return result;
    }
    return Object.keys(map);
}


/**
 * 現在の日付を "yyyyMM" 形式で取得します。
 *
 * @return {string} 現在の日付を "yyyyMM" 形式で表した文字列 (例: "202411")
 */
function getCurrentYearMonth() {
    var LocalDate = Java.type("java.time.LocalDate");
    var DateTimeFormatter = Java.type("java.time.format.DateTimeFormatter");

    var today = LocalDate.now();
    var formatter = DateTimeFormatter.ofPattern("yyyyMM");

    return today.format(formatter);
}

/**
 * エラーメッセージを設定し、成功フラグを false に設定します。
 *
 * @param {string} msg - 表示するエラーメッセージ。
 * @param {string} targetId - 対象の項目ID（省略可能）。
 */
function setErrorMsg(msg, targetId) {
    if (targetId) {
        TALON.addErrorMsg(msg, targetId);
    } else {
        TALON.addErrorMsg(msg);
    }
    TALON.setIsSuccess(false);
}

/**
 * 処理年月を取得
 */
function getSyorituki() {
    var sql = "SELECT SHORI_NENDO FROM COM_M_SOKIN_NENGETSU WHERE CURRENT_FLG = '1'";
    var result = TalonDbUtil.select(TALON.getDbConfig(), sql);

    return (result.length > 0 && result[0]['SHORI_NENDO']) ? result[0]['SHORI_NENDO'] : "";
}
