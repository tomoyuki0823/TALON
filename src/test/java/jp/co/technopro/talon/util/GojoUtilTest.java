package jp.co.technopro.talon.util;

import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.dto.gojo.YotakukinShiharaiRirekiDto;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import static jp.co.technopro.talon.util.DbUtil.selectById;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GojoUtilTest {

    @Test
    void testSetTkMemberDto_success() throws Exception {
        String tkNo = "10002";

        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("TK_NO", tkNo);
        memberMap.put("HON_YOTAKUKIN", 100000);
        List<Map<String, Object>> memberList = Collections.singletonList(memberMap);

        Map<String, Object> shiharaiMap = new HashMap<>();
        shiharaiMap.put("HON_TYOIKIN", 10000);
        shiharaiMap.put("HON_TYOIKIN_SHIHARAI_SDT", "20250101");
        List<Map<String, Object>> shiharaiList = Collections.singletonList(shiharaiMap);

        try (MockedStatic<DbUtil> dbUtilMock = mockStatic(DbUtil.class)) {
            dbUtilMock.when(() -> DbUtil.selectById("TK_MEMBER", tkNo)).thenReturn(memberList);
            dbUtilMock.when(() -> DbUtil.selectById("TK_SHIHARAI", tkNo)).thenReturn(shiharaiList);

            TkMemberDto dto = GojoUtil.setTkMemberDto(tkNo);
            assertNotNull(dto);
            assertEquals("10002", dto.getTkNo());
            // assertEquals(new BigDecimal("100000"), dto.getHonYotakukin());

            YotakukinShiharaiRirekiDto subDto = dto.getYotakukinShiharaiRirekiDto();
            assertNotNull(subDto);
            //assertEquals(new BigDecimal("10000"), subDto.getHonTyoikin());
            assertEquals("20250101", subDto.getHonTyoikinShiharaiSdt());
        }
    }

    @Test
    void testSetTkMemberDto_emptyMember() throws Exception {
        String tkNo = "00000";

        try (MockedStatic<DbUtil> dbUtilMock = mockStatic(DbUtil.class)) {
            dbUtilMock.when(() -> DbUtil.selectById("TK_MEMBER", tkNo)).thenReturn(Collections.emptyList());

            TkMemberDto dto = GojoUtil.setTkMemberDto(tkNo);
            assertNull(dto);
        }
    }
}
