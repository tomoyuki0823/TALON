package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.sql.SqlLoader;
import jp.co.technopro.talon.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.consts.EventId.*;
import static jp.co.technopro.talon.consts.ParamKey.*;
import static jp.co.technopro.talon.consts.TableName.TABLE_TK_YOTAKU;
import static jp.co.technopro.talon.util.DbUtil.*;
import static jp.co.technopro.talon.util.GojoUtil.*;
import static jp.co.technopro.talon.util.LogicUtil.buildResult;
import static jp.co.technopro.talon.util.StringUtil.isNullOrEmpty;


public class YotakuService implements ExecutableLogic {

    private final SqlLoader sqlLoader = new SqlLoader("sql/yotaku-sql.xml");

    @Override
    public Map<String, Object> run(Connection conn, Map<String, Object> params, String eventId) throws SQLException {
        try {
            switch (eventId) {
                case YOTAKU_YOTEI:
                    setYotakukinYotei(conn, params);
                    return buildResult(true, "与託金予定を登録しました");

                case INIT_INFO:
                    setYotakuInit(conn, params);
                    return buildResult(true, "初期情報の登録が完了しました");

                case CALC_YOTAKUKIN:
                    calcYotakukin(conn, params);
                    return buildResult(true, "与託金額の計算が完了しました");

                default:
                    return buildResult(false, "未対応のイベントID: " + eventId);
            }
        } catch (Exception e) {
            return buildResult(false, "処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    private void calcYotakukin(Connection conn, Map<String, Object> paramMap) throws SQLException {

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
     * 与託情報の初期登録処理を実行します。
     * TK_NOおよびSHORI_TUKIをキーにTK_YOTAKUテーブルを確認し、該当レコードが存在しない場合に限り、
     * 与託情報の登録処理を行います。
     *
     * @param conn   DBコネクション
     * @param params TK_NO, SHORI_TUKI を含むパラメータマップ
     * @throws SQLException DBアクセスエラー
     */
    private void setYotakuInit(Connection conn, Map<String, Object> params) throws SQLException {
        Map<String, Object> whereMaps = new HashMap<>();
        whereMaps.put("TK_NO", params.get("TK_NO").toString());
        whereMaps.put("SHORI_TUKI", params.get("SHORI_TUKI").toString());
        if (!isTableEmpty(conn, "TK_YOTAKU", whereMaps)) return;

        insTkYotaku(conn, params.get("TK_NO").toString(), params.get("SHORI_TUKI").toString());
    }

    /**
     * 与託金予定情報を生成・再計算して登録する。
     * TK_MEMBERおよび支払情報を元に、マスタ定義に従って予定金額を設定。
     *
     * @param conn   DB接続
     * @param params パラメータ（TK_NOを含む必要あり）
     * @throws SQLException SQL例外
     */
    private void setYotakukinYotei(Connection conn, Map<String, Object> params) throws SQLException {

        String TK_NO = params.get("TK_NO").toString();
        if (isNullOrEmpty(TK_NO)) return;

        Map<String, Object> tkMap = getTkMember(conn, TK_NO);
        Map<String, Object> tkMapShiharai = getTkShiharai(conn, TK_NO);

        List<Map<String, Object>> yotakukinYoteiMstMapList = getMstYotakukin(conn);
        delYotakuyotei(conn, TK_NO);

        if (tkMap == null) tkMap = new HashMap<>();
        if (tkMapShiharai == null) tkMapShiharai = new HashMap<>();

        String honTaisyoku = String.valueOf(tkMap.get("HON_TAISYOKU_CD"));
        String haiTaisyoku = String.valueOf(tkMap.get("HAI_TAISYOKU_CD"));

        BigDecimal honYotakukin = getBigDecimal(tkMap.get("HON_YOTAKUKIN"));
        BigDecimal honYotakukinShiharai = getBigDecimal(tkMapShiharai.get("HON_YOTAKUKIN"));
        BigDecimal honchoikin = safeSubtract(honYotakukin, honYotakukinShiharai);

        BigDecimal haiYotakukin = getBigDecimal(tkMap.get("HAI_YOTAKUKIN"));
        BigDecimal haiYotakukinShiharai = getBigDecimal(tkMapShiharai.get("HAI_YOTAKUKIN"));
        BigDecimal haichoikin = safeSubtract(haiYotakukin, haiYotakukinShiharai);

        for (Map<String, Object> map : yotakukinYoteiMstMapList) {
            String ptnCd = String.valueOf(map.get("PTN_CD"));

            switch (ptnCd) {
                case "1":
                    // 本人処理
                    map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                    map.put("HON_TYOIKIN_KINGAKU",
                            "90".equals(honTaisyoku) || "91".equals(honTaisyoku) ? honchoikin : BigDecimal.ZERO);

                    // 配偶者処理
                    map.put("HAI_YOTAKUKIN_KINGAKU",
                            "99".equals(haiTaisyoku) ? haichoikin : BigDecimal.ZERO);
                    map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    break;

                case "2":
                    // 本人処理
                    if ("90".equals(honTaisyoku) || "91".equals(honTaisyoku)) {
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    } else {
                        map.put("HON_YOTAKUKIN_KINGAKU", haichoikin);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                    }

                    // 配偶者処理
                    map.put("HAI_YOTAKUKIN_KINGAKU",
                            "99".equals(haiTaisyoku) ? haichoikin : BigDecimal.ZERO);
                    map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    break;
                case "3":
                    if ("91".equals(honTaisyoku) || "90".equals(honTaisyoku)) {
                        // 既に清算済み
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    } else {
                        map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));

                        if ("0".equals(haiTaisyoku)) {
                            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        } else {
                            map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                        }
                    }
                    break;

                case "4":
                    if ("91".equals(honTaisyoku) || "90".equals(honTaisyoku)) {
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    } else {
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));

                        if (!"0".equals(haiTaisyoku) && "99".equals(haiTaisyoku)) {
                            if (honYotakukin.compareTo(haiYotakukin) > 0) {
                                map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                                map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                            } else {
                                map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                                map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            }
                        } else {
                            map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        }
                    }
                    break;

                case "5":
                    if ("91".equals(honTaisyoku) || "90".equals(honTaisyoku)) {
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    } else {
                        map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        if (!"0".equals(haiTaisyoku)) {
                            map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        } else {
                            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        }
                    }
                    break;

                case "6":
                    if ("91".equals(honTaisyoku) || "92".equals(honTaisyoku)) {
                        if ("99".equals(haiTaisyoku)) {
                            map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        } else {
                            map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        }
                    }
                    break;

                case "7":
                    if ("91".equals(honTaisyoku) || "92".equals(honTaisyoku)) {
                        if ("99".equals(haiTaisyoku)) {
                            map.put("HON_YOTAKUKIN_KINGAKU", honYotakukin.subtract(honYotakukinShiharai));
                            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                            if (getInt(map.get("HAI_KANYUBI")) >= 4180401 && getInt(map.get("HAI_TYOIKIN_JSK")) > 0) {
                                map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                            } else {
                                map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            }
                        } else {
                            map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        }
                    } else {
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    }
                    break;

                case "8":
                case "9":
                    if (!"0".equals(haiTaisyoku) && "99".equals(haiTaisyoku)) {
                        map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    } else {
                        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    }
                    break;

                case "10":
                    map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                    map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                    if (!"0".equals(haiTaisyoku) && "99".equals(haiTaisyoku)) {
                        map.put("HAI_YOTAKUKIN_KINGAKU", haiYotakukin.subtract(haiYotakukinShiharai));
                        if (getInt(map.get("HAI_KANYUBI")) >= 4180401 && getInt(map.get("HAI_TYOIKIN_JSK")) > 0) {
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                        } else {
                            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                        }
                    }
                    break;


                default:
                    break;
            }

            map.put("TK_NO", TK_NO);
            insertByMapAutoCols(conn, "TK_T_YOTEKUKIN_YOTEI", map, DbUtil.Dialect.SQLSERVER);

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
                Arrays.asList(MAP_KEY_TK_NO, MAP_KEY_SHORI_TUKI),
                DbUtil.Dialect.SQLSERVER);

    }
}