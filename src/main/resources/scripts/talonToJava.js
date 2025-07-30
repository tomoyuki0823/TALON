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

    // === Java 実行ロジック呼び出し ===
    try {
        // コミット
        var executorClass = Java.type("jp.co.technopro.talon.logic.EventLogicExecutor");
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

