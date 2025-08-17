package jp.co.technopro.talon.util.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.iryo.IryoChkRyoyoShiki;
import jp.co.technopro.talon.util.Gojo.GojoDbUtil;
import jp.co.technopro.talon.util.Gojo.GojoMembershipCheckUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_RYOYO_NENGETU;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_ZOKU;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IryoChkRyoyoShikiTest {

    @Test
    @DisplayName("本人：未入会前 → エラー終了")
    void honnin_beforeKanyuPrev_error() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "0");                // 本人
        target.put(MAP_KEY_RYOYO_NENGETU, "202401");  // テスト値
        paramDto.setTargetData(target);

        TkMemberDto member = new TkMemberDto();
        member.setHonKanyuSeinengapi("R04/04");       // 例：和暦表記の想定
        member.setHonTaikaiSeinengapi(null);

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<GojoMembershipCheckUtil> mockJudge = mockStatic(GojoMembershipCheckUtil.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(member);
            mockJudge.when(() -> GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(anyString(), any(), any()))
                     .thenReturn(GojoMembershipCheckUtil.Result.BEFORE_KANYU_PREV);

            EventResultDto result = logic.run(conn, paramDto);
            assertFalse(result.getStatus());
            assertEquals("療養年月が未入会時となっているため、給付対象外です。", result.getMessage());
        }
    }

    @Test
    @DisplayName("本人：退会後 → エラー終了")
    void honnin_afterTaikaiNext_error() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "0");
        target.put(MAP_KEY_RYOYO_NENGETU, "202501");
        paramDto.setTargetData(target);

        TkMemberDto member = new TkMemberDto();
        member.setHonKanyuSeinengapi("R02/04");
        member.setHonTaikaiSeinengapi("R06/12");

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<GojoMembershipCheckUtil> mockJudge = mockStatic(GojoMembershipCheckUtil.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(member);
            mockJudge.when(() -> GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(anyString(), any(), any()))
                     .thenReturn(GojoMembershipCheckUtil.Result.AFTER_TAIKAI_NEXT);

            EventResultDto result = logic.run(conn, paramDto);
            assertFalse(result.getStatus());
            assertEquals("療養年月が退会後となっているため、給付対象外です。", result.getMessage());
        }
    }

    @Test
    @DisplayName("本人：入力不正（形式不正など）→ OK（スルー）")
    void honnin_inputInvalid_ok() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "0");
        target.put(MAP_KEY_RYOYO_NENGETU, "2025/01"); // 不正形式例
        paramDto.setTargetData(target);

        TkMemberDto member = new TkMemberDto();
        member.setHonKanyuSeinengapi("R02/04");
        member.setHonTaikaiSeinengapi(null);

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<GojoMembershipCheckUtil> mockJudge = mockStatic(GojoMembershipCheckUtil.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(member);
            mockJudge.when(() -> GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(anyString(), any(), any()))
                     .thenReturn(GojoMembershipCheckUtil.Result.INPUT_INVALID);

            EventResultDto result = logic.run(conn, paramDto);
            assertTrue(result.getStatus());
            assertNull(result.getMessage());
        }
    }

    @Test
    @DisplayName("配偶者：未入会前 → エラー終了（配偶者フィールド経由）")
    void haigusha_beforeKanyuPrev_error() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "1");                // 配偶者
        target.put(MAP_KEY_RYOYO_NENGETU, "202401");
        paramDto.setTargetData(target);

        TkMemberDto member = new TkMemberDto();
        member.setHaiKanyubi("R06/01");
        member.setHaiTaikaiSeinengapi(null);

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class);
             MockedStatic<GojoMembershipCheckUtil> mockJudge = mockStatic(GojoMembershipCheckUtil.class)) {

            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(member);
            mockJudge.when(() -> GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(anyString(), any(), any()))
                     .thenReturn(GojoMembershipCheckUtil.Result.BEFORE_KANYU_PREV);

            EventResultDto result = logic.run(conn, paramDto);
            assertFalse(result.getStatus());
            assertEquals("療養年月が未入会時となっているため、給付対象外です。", result.getMessage());
        }
    }

    @Test
    @DisplayName("その他続柄（例: '2'）→ エラー（非認定配偶者は除外）")
    void otherZoku_error() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "2");               // その他
        target.put(MAP_KEY_RYOYO_NENGETU, "202401");
        paramDto.setTargetData(target);

        // member の有無に依らず default 分岐で早期returnするため、staticモックは省略可
        EventResultDto result = logic.run(conn, paramDto);
        assertFalse(result.getStatus());
        assertEquals("非認定配偶者は除外です。", result.getMessage());
    }

    @Test
    @DisplayName("memberDtoが取得できない(null)でも落ちない（分岐到達前にNPEにならない実装が望ましい）")
    void memberNull_okOrHandled() {
        IryoChkRyoyoShiki logic = new IryoChkRyoyoShiki();
        TalonParamDto paramDto = new TalonParamDto();
        Connection conn = mock(Connection.class);

        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_ZOKU, "0");
        target.put(MAP_KEY_RYOYO_NENGETU, "202401");
        paramDto.setTargetData(target);

        try (MockedStatic<GojoDbUtil> mockDb = mockStatic(GojoDbUtil.class)) {
            mockDb.when(() -> GojoDbUtil.setTkMemberDto(any(), anyString(), any())).thenReturn(null);

            // 現実装は memberDto を即参照しているため NPE の可能性があります。
            // 推奨：IryoChkRyoyoShiki 側で memberDto == null をOKで返すガードを入れる。
            // ここでは「落ちないこと」を期待とする（ガード導入後に有効化）。
            EventResultDto result = logic.run(conn, paramDto);
            assertTrue(result.getStatus());
        } catch (NullPointerException npe) {
            fail("memberDto == null で NPE。実装側に null ガードを入れることを推奨します。");
        }
    }
}
