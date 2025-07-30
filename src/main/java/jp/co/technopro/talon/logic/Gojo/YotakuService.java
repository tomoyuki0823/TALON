package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.logger.TalonLogger;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.dto.gojo.YotakuKingakuDto;
import jp.co.technopro.talon.dto.gojo.YotakukinShiharaiRirekiDto;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;
import jp.co.technopro.talon.logic.Gojo.yotaku.strategy.YotakukinStrategyFactory;
import jp.co.technopro.talon.sql.common.SqlLoader;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringCheckUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoEventIdConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoSqlXmlPathConst.SQL_GOJO_YOTAKU;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_T_YOTEKUKIN_YOTEI;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_YOTAKU;
import static jp.co.technopro.talon.util.Gojo.GojoCalcUtil.getBigDecimal;
import static jp.co.technopro.talon.util.common.DbUtil.*;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;
import static jp.co.technopro.talon.util.common.StringUtil.isNullOrEmpty;


public class YotakuService implements ExecutableLogic {

    private final SqlLoader sqlLoader = new SqlLoader(SQL_GOJO_YOTAKU);

    @Override
    public EventResultDto run(Connection conn, TalonParamDto paramDto) throws SQLException {
        try {
            String eventId = paramDto.getEventId();
            switch (eventId) {
                case YOTAKU_YOTEI:
                    setYotakukinYotei(conn, paramDto);
                    return setYotakukinYotei(conn, paramDto);

                case INIT_INFO:
                    setYotakuInit(conn, paramDto);
                    return setYotakuInit(conn, paramDto);

                case CALC_YOTAKUKIN:
                    calcYotakukin(conn, paramDto);
                    return calcYotakukin(conn, paramDto);

                default:
                    return EventResultDto.error("未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return EventResultDto.error("処理中にエラーが発生しました: " + e.getMessage());

        }
    }

    /**
     * 預託金・弔慰金の支給額を計算し、TK_MEMBERに反映します。
     *
     * @param conn     DBコネクション（autoCommit=false推奨）
     * @param paramDto パラメータDTO（targetDataに TK_NO, HON_TAISYOKU_CD, HAI_TAISYOKU_CD を含む必要あり）
     * @return 処理結果（正常終了時は OK）
     * @throws SQLException SQL実行時にエラーが発生した場合
     */
    public EventResultDto calcYotakukin(Connection conn, TalonParamDto paramDto) throws SQLException {
        Map<String, Object> paramMap = paramDto.getTargetData();
        String tkNo = (String) paramMap.get(MAP_KEY_TK_NO);
        String honCd = (String) paramMap.get(MAP_KEY_HON_TAISYOKU_CD);
        String haiCd = (String) paramMap.get(MAP_KEY_HAI_TAISYOKU_CD);

        YotakuKingakuDto dto = calcYotakukinAmount(conn, tkNo, honCd, haiCd);

        // 初期化対象かどうか判定
        if (dto.isAllZero()) {
            dto = new YotakuKingakuDto(); // 全てゼロで初期化
        }

        // TK_MEMBER更新
        String updateSql = sqlLoader.get("UPDATE_YOTAKU");

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, dto.getHonYotaku());
            ps.setBigDecimal(2, dto.getHonTyoi());
            ps.setBigDecimal(3, dto.getHaiYotaku());
            ps.setBigDecimal(4, dto.getHaiTyoi());
            ps.setString(5, tkNo);
            ps.executeUpdate();
        }

        return EventResultDto.ok();
    }

    /**
     * 預託金・弔慰金の支給額を計算し、DTOとして返します。
     *
     * @param conn     DBコネクション
     * @param tkNo     特別会員番号
     * @param honCd    本人退職区分コード
     * @param haiCd    配偶者退職区分コード
     * @return {@link YotakuKingakuDto} 金額DTO
     * @throws SQLException SQL実行時エラー
     */
    private YotakuKingakuDto calcYotakukinAmount(Connection conn, String tkNo, String honCd, String haiCd) throws SQLException {
        YotakuKingakuDto dto = new YotakuKingakuDto();
        String selectSql = sqlLoader.get("CALC_YOTAKUKIN");

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, tkNo);
            ps.setString(2, honCd);
            ps.setString(3, haiCd);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setHonYotaku(rs.getBigDecimal("HON_YOTAKUKIN_KINGAKU"));
                    dto.setHaiYotaku(rs.getBigDecimal("HAI_YOTAKUKIN_KINGAKU"));
                    dto.setHonTyoi(rs.getBigDecimal("HON_TYOIKIN_KINGAKU"));
                    dto.setHaiTyoi(rs.getBigDecimal("HAI_TYOIKIN_KINGAKU"));
                }
            }
        }

        return dto;
    }


    /**
     * TK_MEMBER テーブルに対して支給金額を更新します。
     *
     * @param conn    DBコネクション
     * @param tkNo    会員番号
     * @param kingaku 支給金額情報
     * @throws SQLException SQL実行時の例外
     */
    private void updateTkMember(Connection conn, String tkNo, YotakuKingakuDto kingaku) throws SQLException {
        String updateSql = sqlLoader.get("UPDATE_YOTAKU");

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, kingaku.getHonYotaku());
            ps.setBigDecimal(2, kingaku.getHonTyoi());
            ps.setBigDecimal(3, kingaku.getHaiYotaku());
            ps.setBigDecimal(4, kingaku.getHaiTyoi());
            ps.setString(5, tkNo);
            ps.executeUpdate();
        }
    }


    /**
     * 預託情報の初期登録処理を実行します。
     *
     * <p>
     * TK_NOおよびSHORI_TUKIをキーに、TK_YOTAKU テーブルの存在チェックを行い、
     * レコードが存在しない場合のみ、{@code insTkYotaku} により新規登録を行います。
     * </p>
     *
     * @param conn     DBコネクション（トランザクション管理は呼び出し元に委ねます）
     * @param paramDto 条件データを含むパラメータDTO（TK_NO, SHORI_TUKI が必要）
     * @return 登録が発生した場合も含めて成功時は {@code EventResultDto.ok()} を返します。
     *         必須項目不足時は {@code EventResultDto.error(...)} を返します。
     * @throws SQLException DB操作中に発生したエラー
     */
    public EventResultDto setYotakuInit(Connection conn, TalonParamDto paramDto) throws SQLException {
        Map<String, Object> paramMap = paramDto.getConditionData();
        TalonLogger.logInfo(paramDto, "検索マップ取得");

        String tkNo = SafeMapAccessUtil.getString(paramMap, MAP_KEY_TK_NO);
        String shoriTuki = SafeMapAccessUtil.getString(paramMap, MAP_KEY_SHORI_TUKI);

        if (StringCheckUtil.isNullOrEmpty(tkNo) || StringCheckUtil.isNullOrEmpty(shoriTuki)) {
            return EventResultDto.error(MSG_NON_TK_OBJ);
        }

        if (isYotakuAlreadyRegistered(conn, tkNo, shoriTuki, paramDto.getCompanyCode())) {
            return EventResultDto.ok();
        }

        insTkYotaku(conn, paramDto.getCompanyCode(), tkNo, shoriTuki);
        TalonLogger.logInfo(paramDto, "預託情報登録完了");

        return EventResultDto.ok();
    }

    /**
     * TK_YOTAKU テーブルに該当レコードがすでに存在するかを判定します。
     *
     * @param conn      DBコネクション
     * @param tkNo      会員番号（TK_NO）
     * @param shoriTuki 処理月
     * @param companyCd 会社コード（DB接続識別用）
     * @return レコードが存在する場合は true、未登録の場合は false
     * @throws SQLException DBアクセスエラー
     */
    private boolean isYotakuAlreadyRegistered(Connection conn, String tkNo, String shoriTuki, String companyCd) throws SQLException {
        Map<String, Object> where = Map.of(
                MAP_KEY_TK_NO, tkNo,
                MAP_KEY_SHORI_TUKI, shoriTuki
        );
        return !DbUtil.isTableEmpty(conn, TABLE_TK_YOTAKU, where, companyCd);
    }


    /**
     * 預託金予定情報（TK_T_YOTEKUKIN_YOTEI）を生成・再計算して登録します。
     * <p>
     * 以下の処理を行います：
     * <ul>
     *   <li>引数の {@link TalonParamDto} から TK_NO を取得</li>
     *   <li>TK_MEMBER および TK_SHIHARAI テーブルから該当会員の情報を取得し、DTO に変換</li>
     *   <li>支払済みの預託金情報とマスタ定義（TK_M_YOTEKUKIN_YOTEI）を元に、残額や条件に応じた予定金額を 計算</li>
     *   <li>既存の予定情報を削除後、新たに INSERT を実行</li>
     * </ul>
     *
     * @param conn     DB接続オブジェクト（トランザクション内での使用を想定）
     * @param paramDto TK_NOを含むパラメータDTO。通常、画面やバッチなどから渡される。
     * @return
     * @throws SQLException           SQL操作中の例外（SELECT/DELETE/INSERT等）
     * @throws ClassNotFoundException JDBCドライバが見つからない場合などの例外
     */
    public EventResultDto setYotakukinYotei(Connection conn, TalonParamDto paramDto) throws Exception {
        String tkNo = extractTkNo(paramDto);
        if (isNullOrEmpty(tkNo)) return EventResultDto.error(MSG_NON_TK_NO);

        TkMemberDto memberDto = setTkMemberDto(conn, tkNo);
        if (memberDto == null) return EventResultDto.error(MSG_NON_TK_OBJ);

        List<Map<String, Object>> yoteiMstList = getMstYotakukin(conn, paramDto);
        deleteExistingYotei(conn, tkNo, paramDto.getCompanyCode());

        YotakukinContext ctx = buildYotakukinContext(memberDto);

        insertYoteiRecords(conn, paramDto.getCompanyCode(), tkNo, yoteiMstList, ctx);

        return EventResultDto.ok();
    }

    /**
     * DTOから TK_NO を抽出します。
     *
     * @param paramDto 条件データを含むパラメータDTO
     * @return TK_NO（nullの場合もあり）
     */
    private String extractTkNo(TalonParamDto paramDto) {
        Object tkNoObj = paramDto.getConditionData().get(MAP_KEY_TK_NO);
        return tkNoObj != null ? tkNoObj.toString() : null;
    }

    /**
     * 既存の預託金予定情報（TK_T_YOTEKUKIN_YOTEI）を削除します。
     *
     * @param conn      DB接続（トランザクション内で利用）
     * @param tkNo      削除対象の会員番号
     * @param companyCd DB接続の会社コード
     * @throws SQLException 削除処理中の例外
     */
    private void deleteExistingYotei(Connection conn, String tkNo, String companyCd) throws SQLException {
        delYotakuyotei(conn, tkNo, companyCd);
    }

    /**
     * 預託金計算用のコンテキストオブジェクトを生成します。
     * TK_MEMBER と支払履歴DTOを元に残額等をセットします。
     *
     * @param dto 会員情報DTO
     * @return 計算に使用する文脈オブジェクト
     */
    private YotakukinContext buildYotakukinContext(TkMemberDto dto) {
        YotakukinShiharaiRirekiDto paid = dto.getYotakukinShiharaiRirekiDto();
        return new YotakukinContext(
                dto.getHonTaisyokuCd(),
                dto.getHaiTaisyokuCd(),
                getBigDecimal(dto.getHonYotakukin()),
                getBigDecimal(paid.getHonYotakukin()),
                getBigDecimal(dto.getHaiYotakukin()),
                getBigDecimal(paid.getHaiYotakukin())
        );
    }

    /**
     * 預託金予定情報を戦略ロジックに従って処理し、テーブルにINSERTします。
     *
     * @param conn         DB接続（トランザクション内で利用）
     * @param companyCd    接続に使用する会社コード
     * @param tkNo         対象の会員番号
     * @param yoteiMstList 預託金予定マスタの行リスト
     * @param ctx          預託金計算文脈
     * @throws SQLException INSERT処理で発生した例外
     */
    private void insertYoteiRecords(Connection conn, String companyCd, String tkNo,
                                    List<Map<String, Object>> yoteiMstList, YotakukinContext ctx) throws SQLException {

        for (Map<String, Object> row : yoteiMstList) {
            String ptnCd = String.valueOf(row.get("PTN_CD"));
            if (isNullOrEmpty(ptnCd)) continue;

            YotakukinStrategyFactory.get(ptnCd).ifPresent(strategy -> strategy.apply(row, ctx));

            row.put(MAP_KEY_TK_NO, tkNo);
            insertByMapEx(conn, companyCd, TABLE_TK_T_YOTEKUKIN_YOTEI, row, false);
        }
    }

}