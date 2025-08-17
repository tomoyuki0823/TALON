package jp.co.technopro.talon.util.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.iryo.IryoSetGinko;
import jp.co.technopro.talon.util.Gojo.GojoDbUtil;
import jp.co.technopro.talon.util.Gojo.GojoSoftFlagService;
import jp.co.technopro.talon.util.common.DbUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IryoSetGinko} の単体テスト。
 *
 * <p>ポリシー：「ソフトエラー」のため常に {@link EventResultDto#ok()} が返ることを前提に、
 * 条件に応じてフラグ更新を行う／行わないを検証する。</p>
 *
 * <h3>モック対象（static）</h3>
 * <ul>
 *   <li>{@link GojoDbUtil#setTkMemberDto(java.sql.Connection, String, TalonParamDto)}</li>
 *   <li>{@link DbUtil#getCount(java.sql.Connection, String, java.util.Map, String)}</li>
 *   <li>{@link GojoSoftFlagService#updateFlagSilently(java.sql.Connection, String, String, String, java.util.Map)}</li>
 * </ul>
 */
class IryoSetGinkoTest {

    private static IryoSetGinko newLogic() {
        return new IryoSetGinko();
    }
    private static Connection newConn() { return mock(Connection.class); }

    private static TalonParamDto newParamWithTarget(String companyCd, String zoku, String syoriTuki, String ryoyoNengetu) {
        TalonParamDto p = new TalonParamDto();
        p.setCompanyCode(companyCd);
        Map<String, Object> target = new HashMap<>();
        if (zoku != null)         target.put(MAP_KEY_ZOKU, zoku);
        if (syoriTuki != null)    target.put(MAP_KEY_SHORI_TUKI, syoriTuki);
        if (ryoyoNengetu != null) target.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);
        p.setTargetData(target);
        return p;
    }

    private static TkMemberDto member(String tkNo, String bank, String branch) {
        TkMemberDto m = new TkMemberDto();
        m.setTkNo(tkNo);
        m.setGinkouCd(bank);
        m.setShitenCd(branch);
        return m;
    }

    @Test
    @DisplayName("会員情報が取得できない(null) → 常にOK（スキップ）")
    void returnsOk_whenMemberNull() {
        IryoSetGinko logic = newLogic();
        TalonParamDto param = newParamWithTarget("C1", "0", "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class)) {
            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(null);

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());
            mockDb.verify(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()));
        }
    }

    @Test
    @DisplayName("銀行or支店 未入力 → getCountを呼ばずにOK（フラグ更新なし）")
    void skip_whenBankOrBranchEmpty() {
        IryoSetGinko logic = newLogic();
        TalonParamDto param = newParamWithTarget("C1", "0", "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<DbUtil> mockCnt = mockStatic(DbUtil.class);
             MockedStatic<GojoSoftFlagService> mockFlag = mockStatic(GojoSoftFlagService.class)) {

            // 銀行コード空（支店は値あり）
            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()))
                  .thenReturn(member("TK001", "", "123"));

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());

            // getCount / flag更新は呼ばれない
            mockCnt.verifyNoInteractions();
            mockFlag.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("companyCode 未設定 → getCountを呼ばずにOK（フラグ更新なし）")
    void skip_whenCompanyCodeMissing() {
        IryoSetGinko logic = newLogic();
        TalonParamDto param = newParamWithTarget(null, "0", "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<DbUtil> mockCnt = mockStatic(DbUtil.class);
             MockedStatic<GojoSoftFlagService> mockFlag = mockStatic(GojoSoftFlagService.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()))
                  .thenReturn(member("TK001", "0001", "123"));

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());
            mockCnt.verifyNoInteractions();
            mockFlag.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("マスタ一致（getCount>0）→ フラグ更新しないでOK")
    void ok_whenMasterHit() throws SQLException {
        IryoSetGinko logic = newLogic();
        TalonParamDto param = newParamWithTarget("C1", "0", "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<DbUtil> mockCnt = mockStatic(DbUtil.class);
             MockedStatic<GojoSoftFlagService> mockFlag = mockStatic(GojoSoftFlagService.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()))
                  .thenReturn(member("TK001", "0001", "123"));
            mockCnt.when(() -> DbUtil.getCount(any(), anyString(), anyMap(), anyString()))
                   .thenReturn(1);

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());

            mockFlag.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("マスタ不一致（getCount==0）＆WHERE充足 → フラグ更新を呼ぶ")
    void flagOn_whenMasterNotHit_andWhereComplete() throws SQLException {
        IryoSetGinko logic = newLogic();
        // WHERE 必須: TK_NO, ZOKU, SHORI_TUKI, RYOYO_NENGETU
        TalonParamDto param = newParamWithTarget("C1", "0", "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<DbUtil> mockCnt = mockStatic(DbUtil.class);
             MockedStatic<GojoSoftFlagService> mockFlag = mockStatic(GojoSoftFlagService.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()))
                  .thenReturn(member("TK001", "0001", "123"));
            mockCnt.when(() -> DbUtil.getCount(any(), anyString(), anyMap(), anyString()))
                   .thenReturn(0);

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());

            // 列名は IryoSetGinko の定数: "GINKO_MASTER_FUSEIGO_FLG"
            mockFlag.verify(() -> GojoSoftFlagService.updateFlagSilently(
                    any(), eq(TABLE_TK_IRYO), eq("GINKO_MASTER_FUSEIGO_FLG"), eq("1"), anyMap()
            ));
        }
    }

    @Test
    @DisplayName("マスタ不一致だが WHERE 不足（例: ZOKU欠落）→ フラグ更新しない")
    void noFlag_whenWhereMissing() throws SQLException {
        IryoSetGinko logic = newLogic();
        // ZOKU を欠落させる
        TalonParamDto param = newParamWithTarget("C1", null, "202508", "202501");
        Connection conn = newConn();

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<DbUtil> mockCnt = mockStatic(DbUtil.class);
             MockedStatic<GojoSoftFlagService> mockFlag = mockStatic(GojoSoftFlagService.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any()))
                  .thenReturn(member("TK001", "0001", "123"));
            mockCnt.when(() -> DbUtil.getCount(any(), anyString(), anyMap(), anyString()))
                   .thenReturn(0);

            EventResultDto result = logic.run(conn, param);
            assertTrue(result.getStatus());

            mockFlag.verifyNoInteractions();
        }
    }


}
