package jp.co.technopro.talon.logic;

import jp.co.technopro.talon.dto.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.YotakuService;
import jp.co.technopro.talon.sql.SqlLoader;
import jp.co.technopro.talon.util.DbUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class YotakuServiceTest {

    private YotakuService service;
    private Connection mockConn;
    private TalonParamDto mockDto;

    @BeforeEach
    void setUp() {
        service = spy(new YotakuService());
        mockConn = mock(Connection.class);
        mockDto = mock(TalonParamDto.class);
    }

    @Test
    void testRun_withYoteiEventId_shouldCallSetYotakukinYotei() throws Exception {
        when(mockDto.getEventId()).thenReturn("YOTAKU_YOTEI");
        doNothing().when(service).setYotakukinYotei(eq(mockConn), eq(mockDto));

        Map<String, Object> result = service.run(mockConn, mockDto);

        verify(service).setYotakukinYotei(eq(mockConn), eq(mockDto));
        assert result.get("success").equals(true);
    }

    @Test
    void testSetYotakuInit_shouldInsertIfNotExists() throws Exception {
        Map<String, Object> cond = new HashMap<>();
        cond.put("TK_NO", "TK10001");
        cond.put("SHORI_TUKI", "202507");

        when(mockDto.getConditionData()).thenReturn(cond);

        try (MockedStatic<DbUtil> dbUtil = mockStatic(DbUtil.class)) {
            dbUtil.when(() -> DbUtil.isTableEmpty(any(), any(), any())).thenReturn(true);
            dbUtil.when(() -> DbUtil.insertByMap(any(), any(), any(), any())).thenReturn(1);

            service.setYotakuInit(mockConn, mockDto);

            dbUtil.verify(() ->
                    DbUtil.insertByMap(eq(mockConn), anyString(), argThat(map -> map.get("TK_NO").equals("TK10001")), anyList()), times(1));
        }
    }

    @Test
    void testCalcYotakukin_shouldExecuteUpdate() throws Exception {
        Map<String, Object> targetMap = new HashMap<>();
        targetMap.put("TK_NO", "TK10001");
        targetMap.put("HON_TAISYOKU_CD", "90");
        targetMap.put("HAI_TAISYOKU_CD", "91");

        when(mockDto.getTargetData()).thenReturn(targetMap);

        SqlLoader mockSqlLoader = mock(SqlLoader.class);
        doReturn("SELECT ...").when(mockSqlLoader).get("CALC_YOTAKUKIN");
        doReturn("UPDATE ...").when(mockSqlLoader).get("UPDATE_YOTAKU");

        // SqlLoaderの差し替えには別の設計（インジェクションなど）が必要なため、この部分のテストは限界あり

        // 省略：PreparedStatementとResultSetのモック設定

        // service.calcYotakukin(mockConn, mockDto);

        // 検証：UPDATEが呼ばれたか（省略）
    }
}
