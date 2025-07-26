package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.dto.gojo.YotakukinShiharaiRirekiDto;
import jp.co.technopro.talon.logic.ExecutableLogic;
import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;
import jp.co.technopro.talon.logic.Gojo.yotaku.strategy.YotakukinStrategyFactory;
import jp.co.technopro.talon.sql.SqlLoader;
import jp.co.technopro.talon.util.DbUtil;
import jp.co.technopro.talon.util.SafeMapAccessUtil;
import jp.co.technopro.talon.util.StringCheckUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.EventId.*;
import static jp.co.technopro.talon.consts.ParamKey.*;
import static jp.co.technopro.talon.consts.SqlXmlPath.SQL_YOTAKU;
import static jp.co.technopro.talon.consts.TableName.TABLE_TK_YOTAKU;
import static jp.co.technopro.talon.util.DbUtil.*;
import static jp.co.technopro.talon.util.GojoUtil.*;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;
import static jp.co.technopro.talon.util.StringUtil.isNullOrEmpty;


public class YotakuService implements ExecutableLogic {

    private final SqlLoader sqlLoader = new SqlLoader(SQL_YOTAKU);

    @Override
    public Map<String, Object>  run(Connection conn, TalonParamDto paramDto) throws SQLException {
        try {
            String eventId = paramDto.getEventId();
            switch (eventId) {
                case YOTAKU_YOTEI:
                    setYotakukinYotei(conn, paramDto);
                    return buildResult(true, "与託金予定を登録しました");

                case INIT_INFO:
                    setYotakuInit(conn, paramDto);
                    return buildResult(true, "初期情報の登録が完了しました");

                case CALC_YOTAKUKIN:
                    calcYotakukin(conn, paramDto);
                    return buildResult(true, "与託金額の計算が完了しました");

                default:
                    return buildResult(false, "未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return buildResult(false, "処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    private void calcYotakukin(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> paramMap = paramDto.getTargetData();
        String tkNo = (String) paramMap.get(MAP_KEY_TK_NO);
        String honCd = (String) paramMap.get(MAP_KEY_HON_TAISYOKU_CD);
        String haiCd = (String) paramMap.get(MAP_KEY_HAI_TAISYOKU_CD);

        // ① 預託金予定取得
        String selectSql = sqlLoader.get("CALC_YOTAKUKIN");
        BigDecimal honYotaku = BigDecimal.ZERO;
        BigDecimal haiYotaku = BigDecimal.ZERO;
        BigDecimal honTyoi = BigDecimal.ZERO;
        BigDecimal haiTyoi = BigDecimal.ZERO;
        boolean hasData = false;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, tkNo);
            ps.setString(2, honCd);
            ps.setString(3, haiCd);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hasData = true;
                    honYotaku = Optional.ofNullable(rs.getBigDecimal("HON_YOTAKUKIN_KINGAKU")).orElse(BigDecimal.ZERO);
                    haiYotaku = Optional.ofNullable(rs.getBigDecimal("HAI_YOTAKUKIN_KINGAKU")).orElse(BigDecimal.ZERO);
                    honTyoi = Optional.ofNullable(rs.getBigDecimal("HON_TYOIKIN_KINGAKU")).orElse(BigDecimal.ZERO);
                    haiTyoi = Optional.ofNullable(rs.getBigDecimal("HAI_TYOIKIN_KINGAKU")).orElse(BigDecimal.ZERO);
                }
            }
        }

        // すべてがゼロなら、ゼロ初期化として実行
        boolean shouldUpdate = hasData && (
                honYotaku.compareTo(BigDecimal.ZERO) != 0 ||
                        haiYotaku.compareTo(BigDecimal.ZERO) != 0 ||
                        honTyoi.compareTo(BigDecimal.ZERO) != 0 ||
                        haiTyoi.compareTo(BigDecimal.ZERO) != 0
        );

        // 実データに意味がある場合のみ更新。なければ全てゼロで更新
        if (!shouldUpdate) {
            honYotaku = BigDecimal.ZERO;
            haiYotaku = BigDecimal.ZERO;
            honTyoi = BigDecimal.ZERO;
            haiTyoi = BigDecimal.ZERO;
        }

        String updateSql = sqlLoader.get("UPDATE_YOTAKU");
        try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
            psUpd.setBigDecimal(1, honYotaku);
            psUpd.setBigDecimal(2, honTyoi);
            psUpd.setBigDecimal(3, haiYotaku);
            psUpd.setBigDecimal(4, haiTyoi);
            psUpd.setString(5, tkNo);
            psUpd.executeUpdate();
        }
    }

    /**
     * 預託情報の初期登録処理を実行します。
     * TK_NOおよびSHORI_TUKIをキーにTK_YOTAKUテーブルを確認し、該当レコードが存在しない場合に限り、
     * 与託情報の登録処理を行います。
     *
     * @param conn   DBコネクション
     * @param paramDto TK_NO, SHORI_TUKI を含むパラメータマップ
     * @throws SQLException DBアクセスエラー
     */
    public void setYotakuInit(Connection conn, TalonParamDto paramDto) throws SQLException {

        Map<String, Object> paramMap = paramDto.getConditionData();

        String tkNo = SafeMapAccessUtil.getString(paramMap, MAP_KEY_TK_NO);
        String shoriTuki = SafeMapAccessUtil.getString(paramMap, MAP_KEY_SHORI_TUKI);

        if (StringCheckUtil.isNullOrEmpty(tkNo) || StringCheckUtil.isNullOrEmpty(shoriTuki)) return;

        Map<String, Object> whereMaps = new HashMap<>();
        whereMaps.put(MAP_KEY_TK_NO, tkNo);
        whereMaps.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        if (!DbUtil.isTableEmpty(conn, TABLE_TK_YOTAKU, whereMaps)) return;

        insTkYotaku(conn, tkNo, shoriTuki);
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
     * @param conn      DB接続オブジェクト（トランザクション内での使用を想定）
     * @param paramDto  TK_NOを含むパラメータDTO。通常、画面やバッチなどから渡される。
     * @throws SQLException           SQL操作中の例外（SELECT/DELETE/INSERT等）
     * @throws ClassNotFoundException JDBCドライバが見つからない場合などの例外
     */
    public void setYotakukinYotei(Connection conn, TalonParamDto paramDto) throws SQLException, ClassNotFoundException {

        Map<String, Object> params = paramDto.getConditionData();

        String TK_NO = params.get("TK_NO").toString();
        if (isNullOrEmpty(TK_NO)) return;

        TkMemberDto tkMemberDto  = setTkMemberDto(TK_NO);

        if (tkMemberDto == null) return;
        YotakukinShiharaiRirekiDto yotakukinShiharaiRirekiDto = tkMemberDto.getYotakukinShiharaiRirekiDto();
        List<Map<String, Object>> yotakukinYoteiMstMapList = getMstYotakukin(conn);
        delYotakuyotei(conn, TK_NO);

        String honTaisyoku = tkMemberDto.getHonTaisyokuCd();
        String haiTaisyoku = tkMemberDto.getHaiTaisyokuCd();

        BigDecimal honYotakukin = getBigDecimal(tkMemberDto.getHonYotakukin());
        BigDecimal honYotakukinShiharai = getBigDecimal(yotakukinShiharaiRirekiDto.getHonYotakukin());

        BigDecimal haiYotakukin = getBigDecimal(tkMemberDto.getHaiYotakukin());
        BigDecimal haiYotakukinShiharai = getBigDecimal(yotakukinShiharaiRirekiDto.getHaiYotakukin());

        YotakukinContext ctx = new YotakukinContext(honTaisyoku, haiTaisyoku,
                honYotakukin, honYotakukinShiharai,
                haiYotakukin, haiYotakukinShiharai);

        for (Map<String, Object> map : yotakukinYoteiMstMapList) {
            String ptnCd = String.valueOf(map.get("PTN_CD"));
            if (isNullOrEmpty(ptnCd)) continue;

            YotakukinStrategyFactory.get(ptnCd).ifPresent(strategy -> strategy.apply(map, ctx));

            map.put("TK_NO", TK_NO);
            insertByMapEx(conn, "TK_T_YOTEKUKIN_YOTEI", map);
        }
    }

    public List<Map<String, Object>> getMstYotakukin(Connection conn) throws SQLException {

        return DbUtil.selectListAsMap(conn, "SELECT * FROM TK_M_YOTEKUKIN_YOTEI", new Object[0]);
    }

    public void delYotakuyotei(Connection conn, String tk_no) throws SQLException {

        DbUtil.delete(conn, "DELETE FROM TK_T_YOTEKUKIN_YOTEI WHERE TK_NO = ?", tk_no);
    }

    public void insTkYotaku(Connection conn, String tk_no, String shoriTuki) throws SQLException {

        Map<String, Object> insMap = new HashMap<>();
        insMap.put(MAP_KEY_TK_NO, tk_no);
        insMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        DbUtil.insertByMap(conn, TABLE_TK_YOTAKU, insMap,
                Arrays.asList(MAP_KEY_TK_NO, MAP_KEY_SHORI_TUKI));

    }
}