package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.SqlResult;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.yotaku.CreateYotakuSokinData;
import jp.co.technopro.talon.util.common.DbUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.tln.TlnMapKeyConst.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CreateYotakuSokinData のユニットテスト（DbUtil の static を Mockito でモック）。
 * - selectList: TK_YOTAKU から対象データを返す
 * - deleteByMapEx / insertByMapEx: 呼び出し引数（WHERE/INSERT マップ）を捕捉して検証
 */
class CreateYotakuSokinDataTest {

    @Test
    void run_shouldDeleteAndInsert_perZokuAndDvs_andSkipWhenNull() {
        // --- Arrange ---
        Connection conn = mock(Connection.class);

        // TalonParamDto をモック
        TalonParamDto paramDto = mock(TalonParamDto.class);
        when(paramDto.getCompanyCode()).thenReturn("GOJO");

        Map<String, Object> cond = new HashMap<>();
        cond.put(MAP_KEY_SHORI_TUKI, "202501");
        when(paramDto.getConditionData()).thenReturn(cond);

        // 監査系（値ありでもなしでもOK。ここでは明示）
        when(paramDto.getCreatedBy()).thenReturn("U1");
        when(paramDto.getUpdatedBy()).thenReturn("U1");
        when(paramDto.getCreatedPrgNm()).thenReturn("FUNC1");
        when(paramDto.getUpdatedPrgNm()).thenReturn("FUNC1");
        when(paramDto.getCreatedDate()).thenReturn(new Date());
        when(paramDto.getUpdatedDate()).thenReturn(new Date());

        // TK_YOTAKU 行（1件）
        //  - 本人：預託金=1000、弔慰金=0（ゼロは有効 → 挿入）
        //  - 配偶者：預託金=500、弔慰金=null（null はスキップ）
        Map<String, Object> row = new HashMap<>();
        row.put(MAP_KEY_TK_NO, "TK001");
        row.put("HON_SHIHARAI_YOTAKUKIN", "1000"); // 文字列 → BigDecimal 変換
        row.put("HON_SHIHARAI_TYOIKIN", 0);       // 数値ゼロ
        row.put("HAI_SHIHARAI_YOTAKUKIN", new BigDecimal("500"));
        row.put("HAI_SHIHARAI_TYOIKIN", null);

        List<Map<String, Object>> resultList = Collections.singletonList(row);

        // DbUtil.selectList が返す SqlResult をモック
        SqlResult sqlResult = mock(SqlResult.class);
        when(sqlResult.getMapListResult()).thenReturn(resultList);

        // 呼び出しを捕捉するためのバッファ
        List<Map<String, Object>> deletedWhereList = new ArrayList<>();
        List<String> deletedTableList = new ArrayList<>();

        List<Map<String, Object>> insertedMapList = new ArrayList<>();
        List<String> insertedTableList = new ArrayList<>();

        try (MockedStatic<DbUtil> mocked = mockStatic(DbUtil.class)) {
            // selectList のスタブ
            mocked.when(() -> DbUtil.selectList(eq(conn), eq("TK_YOTAKU"), anyMap(), eq("GOJO")))
                  .thenReturn(sqlResult);

            // deleteByMapEx のスタブ（引数捕捉）
            mocked.when(() -> DbUtil.deleteByMapEx(
                    any(), anyString(), anyMap(), anyList(), anyString()
            )).thenAnswer(inv -> {
                deletedTableList.add(inv.getArgument(1, String.class));
                @SuppressWarnings("unchecked")
                Map<String, Object> where = inv.getArgument(2, Map.class);
                deletedWhereList.add(new HashMap<>(where));
                return 1; // 影響件数（ダミー）
            });

            // insertByMapEx のスタブ（引数捕捉）
            mocked.when(() -> DbUtil.insertByMapEx(
                    any(), anyString(), anyString(), anyMap(), anyBoolean()
            )).thenAnswer(inv -> {
                insertedTableList.add(inv.getArgument(2, String.class));
                @SuppressWarnings("unchecked")
                Map<String, Object> rec = inv.getArgument(3, Map.class);
                insertedMapList.add(new HashMap<>(rec));
                return 1; // 影響件数（ダミー）
            });

            // --- Act ---
            CreateYotakuSokinData sut = new CreateYotakuSokinData();
            EventResultDto res = sut.run(conn, paramDto); // AbstractLogicBase#run を経由

            // --- Assert (結果) ---
            assertNotNull(res);
            // 正常終了をより厳密に見る場合は res.getStatus() == OK 等を確認（実装に依存）

            // DELETE／INSERT が "TK_YOTAKU_SOKIN" に対して行われる
            assertTrue(deletedTableList.stream().allMatch(t -> t.equals("TK_YOTAKU_SOKIN")));
            assertTrue(insertedTableList.stream().allMatch(t -> t.equals("TK_YOTAKU_SOKIN")));

            // 期待：3レコード（本人:2件、配偶者:1件）— 配偶者の弔慰金は null でスキップ
            assertEquals(3, deletedWhereList.size(), "DELETE 呼び出し回数");
            assertEquals(3, insertedMapList.size(), "INSERT 呼び出し回数");

            // 個別検証（ZOKU × KYUFU_DVS × 金額）
            // 本人(01)×預託金(1) = 1000
            assertInsertPresent(insertedMapList, "TK001", "01", "1", "202501", new BigDecimal("1000"));
            // 本人(01)×弔慰金(2) = 0
            assertInsertPresent(insertedMapList, "TK001", "01", "2", "202501", BigDecimal.ZERO);
            // 配偶者(02)×預託金(1) = 500
            assertInsertPresent(insertedMapList, "TK001", "02", "1", "202501", new BigDecimal("500"));
            // 配偶者(02)×弔慰金(2) = null → なし
            assertFalse(hasInsert(insertedMapList, "TK001", "02", "2", "202501"),
                    "配偶者の弔慰金(null)は挿入されないこと");

            // DELETE の WHERE も (TK_NO, ZOKU, KYUFU_DVS, SHORI_TUKI) を全件で含む
            for (Map<String, Object> w : deletedWhereList) {
                assertEquals("TK001", w.get(MAP_KEY_TK_NO));
                assertTrue(Arrays.asList("01", "02").contains(w.get(MAP_KEY_ZOKU)));
                assertTrue(Arrays.asList("1", "2").contains(w.get(MAP_KEY_KYUFU_DVS)));
                assertEquals("202501", w.get(MAP_KEY_SHORI_TUKI));
            }
        }
    }

    // ========= Helpers =========

    private static void assertInsertPresent(List<Map<String, Object>> recs,
                                            String tkNo, String zoku, String dvs, String shoriTuki, BigDecimal kingaku) {
        Map<String, Object> found = findInsert(recs, tkNo, zoku, dvs, shoriTuki);
        assertNotNull(found, "INSERT レコードが見つからない: zoku=" + zoku + ", dvs=" + dvs);

        // 金額は compareTo で比較（0/scale 差吸収）
        BigDecimal actual = (BigDecimal) found.get(MAP_KEY_KINGAKU);
        assertNotNull(actual, "KINGAKU が null");
        assertEquals(0, actual.compareTo(kingaku), "KINGAKU 不一致");

        assertEquals(tkNo, found.get(MAP_KEY_TK_NO));
        assertEquals(zoku, found.get(MAP_KEY_ZOKU));
        assertEquals(dvs,  found.get(MAP_KEY_KYUFU_DVS));
        assertEquals(shoriTuki, found.get(MAP_KEY_SHORI_TUKI));

        // 監査系（最低限、存在はチェック）
        assertTrue(found.containsKey(MAP_KEY_CREATED_DATE));
        assertTrue(found.containsKey(MAP_KEY_CREATED_BY));
        assertTrue(found.containsKey(MAP_KEY_CREATED_PRG_NM));
        assertTrue(found.containsKey(MAP_KEY_UPDATED_DATE));
        assertTrue(found.containsKey(MAP_KEY_UPDATED_BY));
        assertTrue(found.containsKey(MAP_KEY_UPDATED_PRG_NM));
    }

    private static boolean hasInsert(List<Map<String, Object>> recs,
                                     String tkNo, String zoku, String dvs, String shoriTuki) {
        return findInsert(recs, tkNo, zoku, dvs, shoriTuki) != null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findInsert(List<Map<String, Object>> recs,
                                                  String tkNo, String zoku, String dvs, String shoriTuki) {
        for (Map<String, Object> m : recs) {
            if (Objects.equals(tkNo, m.get(MAP_KEY_TK_NO))
                    && Objects.equals(zoku, m.get(MAP_KEY_ZOKU))
                    && Objects.equals(dvs, m.get(MAP_KEY_KYUFU_DVS))
                    && Objects.equals(shoriTuki, m.get(MAP_KEY_SHORI_TUKI))) {
                return m;
            }
        }
        return null;
    }
}
