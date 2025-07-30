package jp.co.technopro.talon.util.common;

import jp.co.technopro.talon.dto.common.TalonParamDto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.tln.TlnTableNameConst.TABLE_TLN_M_HANYO_CODE;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_KEY_CODE;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.MAP_KEY_SIKIBETU_CODE;

public class TalonSelectUtil {

    /**
     * 汎用コードマスタから指定された識別コードに一致するレコード一覧を取得します。
     *
     * @param conn         DB接続
     * @param sikibetuCode 取得対象の識別コード（例: "TK_DVS"）
     * @return 該当レコードの一覧（Map形式）
     * @throws SQLException SQL実行時のエラー
     */
    public static List<Map<String, Object>> selectHanyoMapList(Connection conn, String sikibetuCode, TalonParamDto dto) throws SQLException {
        return DbUtil.selectList(
                conn,
                TABLE_TLN_M_HANYO_CODE,
                Map.of(MAP_KEY_SIKIBETU_CODE, sikibetuCode),
                dto.getCompanyCode()
        ).getMapListResult();
    }

    /**
     * 汎用コードマスタから指定された識別コードに一致するレコード一覧を取得します。
     *
     * @param conn         DB接続
     * @param sikibetuCode 取得対象の識別コード（例: "TK_DVS"）
     * @return 該当レコードの一覧（Map形式）
     * @throws SQLException SQL実行時のエラー
     */
    public static Map<String, Object> selectHanyoMap(Connection conn, String sikibetuCode, String keyCode, TalonParamDto dto) throws SQLException {
        return DbUtil.selectOne(
                conn,
                TABLE_TLN_M_HANYO_CODE,
                null,
                Map.of(MAP_KEY_SIKIBETU_CODE, sikibetuCode, MAP_KEY_KEY_CODE, keyCode),
                null,
                dto.getCompanyCode()
        ).getMapResult();
    }
}
