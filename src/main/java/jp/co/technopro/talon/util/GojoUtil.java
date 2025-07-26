package jp.co.technopro.talon.util;

import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.dto.gojo.YotakukinShiharaiRirekiDto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.mapper.TalonParamMapper.mapToDto;
import static jp.co.technopro.talon.util.DbUtil.selectById;

public class GojoUtil {


    public static Map<String, Object> getTkMember(Connection conn, String tk_no) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TK_MEMBER WHERE TK_NO = ?", tk_no);

    }

    public static Map<String, Object> getTkShiharai(Connection conn, String tk_no) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TK_SHIHARAI WHERE TK_NO = ?", tk_no);

    }

    /**
     * 任意のObjectからBigDecimalに変換するユーティリティ。
     */
    public static BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }

    public static BigDecimal safeSubtract(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b);
    }


    public static Map<String, Object> getSime(Connection conn, String shoriTuki) throws SQLException {

        return DbUtil.selectOneRowAsMap(conn, "SELECT * FROM TKC001 WHERE SHORI_TUKI = ?", shoriTuki);

    }

    public static void insTkYotaku(Connection conn, String tk_no, String shoriTuki) throws SQLException {

        Map<String, Object> insMap = new HashMap<>();
        insMap.put("TK_NO", tk_no);
        insMap.put("SHORI_TUKI", shoriTuki);

        DbUtil.insertByMap(conn, "TK_YOTAKU", insMap,
                Arrays.asList("TK_NO", "SHORI_TUKI"));

    }

    public static int getInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
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
     * @throws SQLException データベース接続やSQL実行時にエラーが発生した場合
     * @throws ClassNotFoundException JDBCドライバの読み込みに失敗した場合など
     */
    public static TkMemberDto setTkMemberDto(String tkNo) throws SQLException, ClassNotFoundException {
        List<Map<String, Object>> tokMapList = selectById("TK_MEMBER", tkNo);
        if (tokMapList.isEmpty()) return null;

        Map<String, Object> tokMap = tokMapList.get(0);
        TkMemberDto tkMemberDto = mapToDto(tokMap, TkMemberDto.class);

        List<Map<String, Object>> shiharaiMapList = selectById("TK_SHIHARAI", tkNo);
        if (!shiharaiMapList.isEmpty()) {
            Map<String, Object> shiharaiMap = shiharaiMapList.get(0);
            YotakukinShiharaiRirekiDto yotakukinShiharaiRirekiDto = mapToDto(shiharaiMap, YotakukinShiharaiRirekiDto.class);
            tkMemberDto.setYotakukinShiharaiRirekiDto(yotakukinShiharaiRirekiDto);
        }

        return tkMemberDto;
    }

}
