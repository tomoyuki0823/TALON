package jp.co.technopro.talon.logic;

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

    @BeforeEach
    void setUp() {
        service = spy(new YotakuService());
        mockConn = mock(Connection.class);
    }

    @Test
    void testSetYotakukinYotei_shouldCallInsertByMapAutoCols() throws Exception {
        // テスト用データ
        Map<String, Object> params = new HashMap<>();
        params.put("TK_NO", "10001");

        Map<String, Object> tkMember = new HashMap<>();
        tkMember.put("HON_TAISYOKU_CD", "90");
        tkMember.put("HAI_TAISYOKU_CD", "99");
        tkMember.put("HON_YOTAKUKIN", new BigDecimal("10000"));
        tkMember.put("HAI_YOTAKUKIN", new BigDecimal("8000"));

        Map<String, Object> tkShiharai = new HashMap<>();
        tkShiharai.put("HON_YOTAKUKIN", new BigDecimal("2000"));
        tkShiharai.put("HAI_YOTAKUKIN", new BigDecimal("3000"));

        List<Map<String, Object>> mstList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("PTN_CD", "1");
        mstList.add(map1);

        // モック設定

        doReturn(mstList).when(service).getMstYotakukin(mockConn);
        doNothing().when(service).delYotakuyotei(mockConn, "10001");

        try (MockedStatic<DbUtil> mockedStatic = mockStatic(DbUtil.class)) {
            mockedStatic.when(() ->
                    DbUtil.insertByMapAutoCols(any(), anyString(), anyMap(), any())
            ).thenReturn(1);

            // 実行
            service.run(mockConn, params, "YOTAKU_YOTEI");

            // 検証
            mockedStatic.verify(() ->
                    DbUtil.insertByMapAutoCols(
                            eq(mockConn),
                            eq("TK_T_YOTEKUKIN_YOTEI"),
                            argThat(map -> "1".equals(map.get("PTN_CD"))),
                            eq(DbUtil.Dialect.SQLSERVER)
                    ), times(1)
            );
        }
    }
}
