package jp.co.technopro.talon.util.Gojo;

import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.dto.gojo.YotakukinShiharaiRirekiDto;
import jp.co.technopro.talon.sql.common.SqlLoader;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.DtoMapTransformUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SIME_STATUS;
import static jp.co.technopro.talon.consts.Gojo.GojoSqlKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoSqlXmlPathConst.SQL_GOJO_COMMON;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.*;
import static jp.co.technopro.talon.consts.tln.TlnCompanyConst.COMPANY_CD_GOJO;
import static jp.co.technopro.talon.mapper.TalonParamMapper.mapToDto;
import static jp.co.technopro.talon.util.common.DbUtil.*;

public class GojoDbUtil {

    private static final SqlLoader sqlLoader = new SqlLoader(SQL_GOJO_COMMON);

    /**
     * TK_MEMBER テーブルから特定の会員情報を1件取得します。
     *
     * @param conn   DBコネクション（null可、nullの場合は dto.getCompanyCode() に基づき自動取得）
     * @param tkNo   会員番号（検索条件）
     * @param dto    TalonパラメータDTO（会社コードを含む）
     * @return 会員情報を表す Map（該当なしの場合は null）
     * @throws SQLException           SQL実行時に発生する例外
     * @throws ClassNotFoundException JDBCドライバ読み込み失敗時の例外
     */
    public static Map<String, Object> getTkMember(Connection conn, String tkNo, TalonParamDto dto)
            throws SQLException, ClassNotFoundException {

        Map<String, Object> whereMap = Map.of(MAP_KEY_TK_NO, tkNo);

        return DbUtil.selectOne(conn, TABLE_TK_MEMBER, null, whereMap, null, dto.getCompanyCode()).getMapResult();
    }


    /**
     * TK_SHIHARAI テーブルから特定の支払情報を1件取得します。
     *
     * @param conn   DBコネクション（null可。nullの場合は dto.getCompanyCode により自動取得されます）
     * @param tkNo   会員番号（検索条件）
     * @param dto    TalonパラメータDTO（会社コードを含む）
     * @return 支払情報を表す Map（該当なしの場合は null）
     * @throws Exception SQL実行時またはDB接続時の例外
     */
    public static Map<String, Object> getTkShiharai(Connection conn, String tkNo, TalonParamDto dto) throws Exception {
        Map<String, Object> whereMap = Map.of(MAP_KEY_TK_NO, tkNo);
        return DbUtil.selectOne(conn, TABLE_TK_SHIHARAI, null, whereMap, null, dto.getCompanyCode()).getMapResult();
    }


    /**
     * TKC001 テーブルから、指定された処理月（SHORI_TUKI）に一致する締めデータを取得します。
     *
     * @param conn       DBコネクション（null可。nullの場合は dto.getCompanyCode により接続を取得）
     * @param shoriTuki  処理月（WHERE句の条件値）
     * @param dto        TalonパラメータDTO（会社コードを含む）
     * @return 処理月に該当するレコードのリスト（各行は Map<String, Object> 形式）
     * @throws SQLException SQL実行時の例外
     */
    public static List<Map<String, Object>> getSime(Connection conn, String shoriTuki, TalonParamDto dto) throws SQLException {
        Map<String, Object> whereMap = Map.of(MAP_KEY_SHORI_TUKI, shoriTuki);
        return DbUtil.selectList(conn, "TKC001", whereMap, dto.getCompanyCode()).getMapListResult();
    }

    /**
     * 特別会員番号（TK_NO）を指定して、会員基本情報（TK_MEMBER）および支払情報（TK_SHIHARAI）をDTOにマッピングします。
     * <p>
     * 主に以下の処理を行います：
     * <ul>
     *   <li>TK_MEMBER テーブルから該当するレコードを取得し {@link TkMemberDto} にマッピング</li>
     *   <li>TK_SHIHARAI テーブルから支払履歴情報を取得し、{@link YotakukinShiharaiRirekiDto} にマッピングして {@code TkMemberDto} に設定</li>
     * </ul>
     *
     * @param tkNo 特別会員番号
     * @return {@link TkMemberDto} オブジェクト（該当する会員が存在しない場合は {@code null}）
     * @throws SQLException           データベース接続やSQL実行時にエラーが発生した場合
     * @throws ClassNotFoundException JDBCドライバの読み込みに失敗した場合など
     */
    public static TkMemberDto setTkMemberDto(Connection conn, String tkNo) throws Exception {
        List<Map<String, Object>> tokMapList = selectById(conn, SQL_TK_MEMBER, COMPANY_CD_GOJO, tkNo).getMapListResult();
        if (tokMapList.isEmpty()) return null;

        Map<String, Object> tokMap = tokMapList.get(0);
        TkMemberDto tkMemberDto = mapToDto(tokMap, TkMemberDto.class);

        List<Map<String, Object>> shiharaiMapList = selectById(conn, SQL_TK_SHIHARAI, COMPANY_CD_GOJO, tkNo).getMapListResult();
        if (!shiharaiMapList.isEmpty()) {
            Map<String, Object> shiharaiMap = shiharaiMapList.get(0);
            YotakukinShiharaiRirekiDto yotakukinShiharaiRirekiDto = mapToDto(shiharaiMap, YotakukinShiharaiRirekiDto.class);
            tkMemberDto.setYotakukinShiharaiRirekiDto(yotakukinShiharaiRirekiDto);
        }

        return tkMemberDto;
    }

    /**
     * TK_HENKO2 テーブルに対象の履歴データ（TK_NO + SHORI_TUKI）が存在するかをチェックします。
     *
     * @param conn      DBコネクション
     * @param tkNo      対象の会員番号
     * @param shoriTuki 処理月
     * @return true: すでに存在している（＝履歴あり）, false: 未登録
     * @throws SQLException SQL実行時の例外
     */
    public static boolean isCntHenko2(Connection conn, String tkNo, String shoriTuki, TalonParamDto dto) throws SQLException {
        Map<String, Object> whereMap = Map.of(
                MAP_KEY_TK_NO, tkNo,
                MAP_KEY_SHORI_TUKI, shoriTuki
        );
        return DbUtil.getCount(conn, TABLE_TK_HENKO2, whereMap, dto.getCompanyCode()) > 0;
    }

    /**
     * 指定された処理月に基づき、変更反映対象（TK_MEMBER更新対象）の一覧を取得します。
     *
     * @param conn      DBコネクション
     * @param shoriTuki 処理月（＝締め日判定対象月）
     * @return TK_HENKO から取得された変更反映対象の一覧
     * @throws SQLException SQL実行時の例外
     */
    public static List<Map<String, Object>> getHenkoReflectTargetList(Connection conn, String shoriTuki) throws
            SQLException {
        Map<String, Object> whereMap = Map.of(MAP_KEY_SHORI_TUKI, shoriTuki);
        return selectList(conn, TABLE_TK_HENKO, whereMap, null).getMapListResult();
    }

    /**
     * TK_NOをキーに TK_MEMBER テーブルへレコードを更新（該当がなければINSERT）します。
     *
     * @param conn   DBコネクション
     * @param record 更新対象のレコードMap（TK_MEMBER相当）
     * @throws SQLException SQL実行例外
     */
    public static void upsertMemberRecord(Connection conn, Map<String, Object> record, TalonParamDto dto) throws SQLException {
        String tkNo = String.valueOf(record.get(MAP_KEY_TK_NO));
        Map<String, Object> whereMap = Map.of(MAP_KEY_TK_NO, tkNo);
        updateByMapEx(conn, dto.getCompanyCode(), TABLE_TK_MEMBER, record, whereMap, true);
    }


    /**
     * 預託金マスタ（TK_M_YOTEKUKIN_YOTEI）を全件取得します。
     *
     * @param conn      DBコネクション（null可。nullの場合は companyCd に応じて取得）
     * @param companyCd DB接続用の会社コード
     * @return 全件のレコードを List<Map> として返却
     * @throws Exception DB接続取得やSQL実行時にエラーが発生した場合
     */
    private static List<Map<String, Object>> getMstYotakukin(Connection conn, String companyCd) throws Exception {
        conn = DbUtil.getConnectionIfNull(conn, companyCd);
        return DbUtil.selectList(conn, "TK_M_YOTEKUKIN_YOTEI", null, null).getMapListResult();
    }

    /**
     * 預託金マスタを TalonParamDto の companyCode に基づいて取得します。
     *
     * @param conn DBコネクション（null可）
     * @param dto  TalonパラメータDTO（companyCodeを保持）
     * @return 全件のレコードを List<Map> として返却
     * @throws Exception DB接続取得やSQL実行時にエラーが発生した場合
     */
    public static List<Map<String, Object>> getMstYotakukin(Connection conn, TalonParamDto dto) throws Exception {
        return getMstYotakukin(conn, dto.getCompanyCode());
    }

    /**
     * 預託金予定テーブル（TK_T_YOTEKUKIN_YOTEI）から、指定された会員番号（TK_NO）に
     * 該当するデータを削除します。
     *
     * <p>
     * このメソッドは主に再計算処理や再登録処理の前処理として使用されます。
     * </p>
     *
     * <h3>仕様と制約:</h3>
     * <ul>
     *   <li>{@code tk_no} が {@code null} または空の場合は、例外をスローして削除処理を中止します。</li>
     *   <li>{@code companyCd} に応じたDB接続が選択されます（{@code conn} が {@code null} の場合）。</li>
     *   <li>WHERE条件は {@code TK_NO} のみで構成されます。</li>
     * </ul>
     *
     * <h3>使用例:</h3>
     * <pre>{@code
     * delYotakuyotei(conn, "10425", "gojo");
     * }</pre>
     *
     * @param conn      使用するDBコネクション（null可。nullの場合は companyCd に応じて自動取得）
     * @param tk_no     削除対象となる会員番号（null または空不可）
     * @param companyCd 接続対象の会社コード（例: "gojo", "common"）
     * @throws IllegalArgumentException TK_NOが未指定の場合
     * @throws Exception                DB操作中のSQL例外や接続取得エラーなど
     */
    public static void delYotakuyotei(Connection conn, String tk_no, String companyCd) throws SQLException {
        if (tk_no == null || tk_no.isEmpty()) {
            throw new IllegalArgumentException("TK_NOが指定されていません。削除処理を中止します。");
        }

        List<String> whereKeys = List.of(MAP_KEY_TK_NO);
        Map<String, Object> whereMap = Map.of(MAP_KEY_TK_NO, tk_no);
        DbUtil.deleteByMapEx(conn, "TK_T_YOTEKUKIN_YOTEI", whereMap, whereKeys, companyCd);
    }

    /**
     * TK_YOTAKU テーブルに預託金データの初期レコードを登録します。
     *
     * @param conn      DBコネクション（null不可）
     * @param tkNo      特別会員番号（TK_NO）
     * @param shoriTuki 処理対象月（SHORI_TUKI）
     * @param companyCd 会社コード（DB接続が null の場合やテーブル依存切替に使用）
     * @throws SQLException SQL実行時の例外
     */
    public static void insTkYotaku(Connection conn, String tkNo, String shoriTuki, String companyCd) throws SQLException {
        Map<String, Object> insMap = new HashMap<>();
        insMap.put(MAP_KEY_TK_NO, tkNo);
        insMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        DbUtil.insertByMapEx(conn, companyCd, TABLE_TK_YOTAKU, insMap, false);
    }

    public static void insTkHenko2(Connection conn, TalonParamDto paramDto, TkMemberDto tkMemberDto) throws SQLException {
        Map<String, Object> memberMap = DtoMapTransformUtil.toMap(tkMemberDto);
        insertByMapEx(conn, paramDto.getCompanyCode(), TABLE_TK_HENKO2, memberMap, false);

    }

    /**
     * 汎用コードリストに基づき、指定した処理月の締めデータを {@code TKC001} テーブルへ登録します。
     *
     * <p>
     * 各レコードは以下の項目で構成されます：<br>
     * ・処理月（{@code SHORI_TUKI}）<br>
     * ・区分コード（{@code TK_DVS}）<br>
     * ・締め状態（{@code SIME_STATUS = "1"}）<br>
     * </p>
     *
     * <p>
     * DB接続は引数の {@code conn} を使用し、TALON から渡されたものを想定しています（クローズしません）。
     * </p>
     *
     * @param conn           DBコネクション（TALON から渡された接続を想定）
     * @param shoriTuki      処理対象の月（フォーマット例: "202507"）
     * @param hanyouCodeList 汎用コードのリスト（各Mapに {@code KEY_CODE} を含む）
     * @param dto            TalonParamDto（会社コードを取得するために使用）
     * @throws SQLException 登録処理中にDBエラーが発生した場合
     */
    public static void insertSimeData(Connection conn, String shoriTuki,
                                List<Map<String, Object>> hanyouCodeList,
                                TalonParamDto dto) throws SQLException {

        for (Map<String, Object> code : hanyouCodeList) {
            Map<String, Object> insMap = new HashMap<>();
            insMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);
            insMap.put(MAP_KEY_TK_DVS, code.get("KEY_CODE"));
            insMap.put(MAP_KEY_SIME_STATUS, "1");

            DbUtil.insertByMapEx(conn, dto.getCompanyCode(), TABLE_TKC001, insMap, false);
        }
    }

    /**
     * 指定された処理月のデータが TKC001 テーブルに存在しないかを判定します。
     *
     * @param conn      DB接続
     * @param shoriTuki 処理対象の年月（例: "202507"）
     * @return データが存在しない場合は true、1件以上存在すれば false
     * @throws SQLException SQL実行時のエラー
     */
    public static boolean isTkc001Empty(Connection conn, String shoriTuki, TalonParamDto paramDto) throws SQLException {
        return getCount(conn, TABLE_TKC001, Map.of(MAP_KEY_SHORI_TUKI, shoriTuki), paramDto.getCompanyCode()) == 0;
    }

    /** TK_SHINKI テーブルから処理月で絞り込んだレコード一覧を取得 */
    public static List<Map<String, Object>> loadShinkiData(Connection conn, String shoriTuki) throws SQLException {
        Map<String, Object> whereMap = Map.of(MAP_KEY_SHORI_TUKI, shoriTuki);
        return selectList(conn, TABLE_TK_SHINKI, whereMap, null).getMapListResult();
    }

    public static void processShinkiRecord(Connection conn, String companyCd, Map<String, Object> record) throws SQLException {
        String tkNo = (String) record.get(MAP_KEY_TK_NO);
        deleteTkMember(conn, tkNo, companyCd);
        insertByMapEx(conn, companyCd, TABLE_TK_MEMBER, record, false);
    }

    /**
     * TK_MEMBER テーブルから指定された TK_NO を削除します。
     *
     * @param conn DB接続
     * @param tkNo 対象会員番号
     * @throws SQLException SQL例外が発生した場合
     */
    public static void deleteTkMember(Connection conn, String tkNo, String companyCd) throws SQLException {
        DbUtil.deleteByMapEx(conn, "TK_MEMBER" ,Map.of(MAP_KEY_TK_NO, tkNo), List.of(MAP_KEY_TK_NO), companyCd);
    }

    /**
     * TKC001 テーブルの該当レコードの SIME_STATUS を '2' に更新します。
     *
     * @param conn      DBコネクション
     * @param shoriTuki 処理月（SHORI_TUKI）
     * @param tkDvs     区分（TK_DVS）
     * @throws SQLException SQLエラーが発生した場合
     */
    public static void updateTkc001(Connection conn, String shoriTuki, String tkDvs) throws SQLException {
        String sql = sqlLoader.get(SQL_KEY_UPDATE_TKC001);
        DbUtil.update(conn, sql, shoriTuki, tkDvs);
    }


}