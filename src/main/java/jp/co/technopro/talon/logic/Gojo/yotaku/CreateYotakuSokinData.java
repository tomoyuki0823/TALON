package jp.co.technopro.talon.logic.Gojo.yotaku;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_SIME_STATUS_3;
import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_YOTAKU;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_YOTAKU;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_YOTAKU_02;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.updateTkc001;
import static jp.co.technopro.talon.util.common.DbUtil.selectList;

/**
 * 預託金送金データ作成ロジック（Nashorn版 setYotakuSonkinData の Java 置換）。
 *
 * <p>処理概要：</p>
 * <ul>
 *   <li>処理月（{@code SHORI_TUKI}）で TK_YOTAKU を抽出</li>
 *   <li>（ZOKU=01/HON, 02/HAI）×（KYUFU_DVS=1:預託金, 2:弔慰金）ごとに
 *       TK_YOTAKU_02 を DELETE→INSERT（null金額のみスキップ／0は有効）</li>
 *   <li>最後に TKC001 を 与託×最終締め（3）に更新</li>
 * </ul>
 *
 * <p>前提：</p>
 * <ul>
 *   <li>トランザクション・コネクションのライフサイクルは呼び出し元で管理（本クラスではクローズやcommit/rollbackを行わない）</li>
 *   <li>{@link TalonParamDto} に companyCode や作成者情報などが設定済み</li>
 * </ul>
 */
public class CreateYotakuSokinData extends GojoAbstractLogicBase {

    // 区分の可読性向上（誤値防止）
    private static final String ZOKU_HON = "01";
    private static final String ZOKU_HAI = "02";
    private static final String KYUFU_DVS_YOTAKUKIN = "1";
    private static final String KYUFU_DVS_TYOIKIN   = "2";

    @Override
    protected EventResultDto executeLogic() {
        return createAndInsert();
    }

    /**
     * 送金データを作成し、締め状態を「最終締め」に更新する。
     */
    private EventResultDto createAndInsert() {
        // 必須：処理月の検証（null/空は異常）
        final String shoriTuki = SafeMapAccessUtil.getString(paramDto.getConditionData(), MAP_KEY_SHORI_TUKI);
        if (shoriTuki == null || shoriTuki.isBlank()) {
            return EventResultDto.error("処理月（SHORI_TUKI）が未指定です。");
        }

        final List<Map<String, Object>> mapList = getTaisyoList(shoriTuki);

        for (Map<String, Object> map : mapList) {
            final String tkNo = (String) map.get(MAP_KEY_TK_NO);
            try {
                // HON（本人）
                createAndInsertOne(tkNo, ZOKU_HON, toBigDecimalOrNull(map.get("HON_SHIHARAI_YOTAKUKIN")),
                        KYUFU_DVS_YOTAKUKIN, paramDto, shoriTuki);
                createAndInsertOne(tkNo, ZOKU_HON, toBigDecimalOrNull(map.get("HON_SHIHARAI_TYOIKIN")),
                        KYUFU_DVS_TYOIKIN, paramDto, shoriTuki);

                // HAI（配偶者）
                createAndInsertOne(tkNo, ZOKU_HAI, toBigDecimalOrNull(map.get("HAI_SHIHARAI_YOTAKUKIN")),
                        KYUFU_DVS_YOTAKUKIN, paramDto, shoriTuki);
                createAndInsertOne(tkNo, ZOKU_HAI, toBigDecimalOrNull(map.get("HAI_SHIHARAI_TYOIKIN")),
                        KYUFU_DVS_TYOIKIN, paramDto, shoriTuki);

            } catch (SQLException e) {
                // 呼び出し元のトランザクション方針に従わせるため Runtime に包む
                throw new RuntimeException("送金データ作成中にSQL例外が発生しました。TK_NO=" + tkNo, e);
            }
        }

        // 締め状態を「最終締め」に更新
        updateTkc001(conn, shoriTuki, TK_DVS_YOTAKU, TK_DVS_SIME_STATUS_3, paramDto.getCompanyCode());
        return EventResultDto.ok();
    }

    /**
     * 対象（処理月）リスト取得。
     */
    private List<Map<String, Object>> getTaisyoList(String shoriTuki) {
        final Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(MAP_KEY_SHORI_TUKI, shoriTuki);
        try {
            return selectList(conn, TABLE_TK_YOTAKU, whereMap, paramDto.getCompanyCode()).getMapListResult();
        } catch (SQLException e) {
            throw new RuntimeException("対象抽出に失敗しました。SHORI_TUKI=" + shoriTuki, e);
        }
    }

    /**
     * 1レコード分の DELETE→INSERT を実施。金額が null の場合のみスキップ（0は有効）。
     */
    private void createAndInsertOne(String tkNo,
                                    String zoku,
                                    BigDecimal kingaku,
                                    String kingakuDvs,
                                    TalonParamDto paramDto,
                                    String shoriTuki) throws SQLException {

        if (kingaku == null) return; // 0はINSERT対象

        // 既存 DELETE（ユニークキー相当：TK_NO + ZOKU + KYUFU_DVS + SHORI_TUKI）
        final Map<String, Object> where = new HashMap<>();
        where.put(MAP_KEY_TK_NO, tkNo);
        where.put(MAP_KEY_ZOKU, zoku);
        where.put(MAP_KEY_KYUFU_DVS, kingakuDvs);
        where.put(MAP_KEY_SHORI_TUKI, shoriTuki);

        DbUtil.deleteByMapEx(
                conn,
                TABLE_TK_YOTAKU_02,
                where,
                List.of(MAP_KEY_TK_NO, MAP_KEY_ZOKU, MAP_KEY_KYUFU_DVS, MAP_KEY_SHORI_TUKI),
                paramDto.getCompanyCode()
        );

        // INSERT
        final Map<String, Object> ins = buildSokinMap(paramDto, tkNo, zoku, kingaku, kingakuDvs, shoriTuki);
        DbUtil.insertByMapEx(conn, paramDto.getCompanyCode(), TABLE_TK_YOTAKU_02, ins, true);
    }

    /**
     * INSERT用マップ組み立て。
     */
    private Map<String, Object> buildSokinMap(TalonParamDto paramDto,
                                              String tkNo,
                                              String zoku,
                                              BigDecimal kingaku,
                                              String kingakuDvs,
                                              String shoriTuki) {
        final Map<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_TK_NO, tkNo);
        map.put(MAP_KEY_ZOKU, zoku);
        map.put(MAP_KEY_SHORI_TUKI, shoriTuki);
        map.put(MAP_KEY_KYUFU_DVS, kingakuDvs);
        map.put(MAP_KEY_KINGAKU, kingaku);

        final Date now = new Date();
        map.put(MAP_KEY_CREATED_DATE, paramDto.getCreatedDate() != null ? paramDto.getCreatedDate() : now);
        map.put(MAP_KEY_CREATED_BY, paramDto.getCreatedBy());
        map.put(MAP_KEY_CREATED_PRG_NM, paramDto.getCreatedPrgNm());
        map.put(MAP_KEY_UPDATED_DATE, paramDto.getUpdatedDate() != null ? paramDto.getUpdatedDate() : now);
        map.put(MAP_KEY_UPDATED_BY, paramDto.getUpdatedBy());
        map.put(MAP_KEY_UPDATED_PRG_NM, paramDto.getUpdatedPrgNm());
        map.put("modify_count", 0); // スキーマに合わせ大文字へ統一

        return map;
    }

    /**
     * 任意型から BigDecimal へ安全変換（null/空は null、カンマ除去）。
     */
    private static BigDecimal toBigDecimalOrNull(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number)     return new BigDecimal(String.valueOf(v));
        String s = v.toString().trim();
        if (s.isEmpty()) return null;
        s = s.replace(",", "");
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            // 仕様上、非数は無視してスキップ扱いにする
            return null;
        }
    }
}
