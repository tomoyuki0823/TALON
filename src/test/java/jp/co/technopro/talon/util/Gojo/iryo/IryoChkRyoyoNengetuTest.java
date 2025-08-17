package jp.co.technopro.talon.util.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.iryo.IryoChkRyoyoNengetu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_RYOYO_NENGETU;

/**
 * {@link IryoChkRyoyoNengetu} の単体テスト。
 * 抽象基底 {@code AbstractLogicBase} 準拠で {@code run(conn, paramDto)} を経由させて検証する。
 */
class IryoChkRyoyoNengetuTest {

    private IryoChkRyoyoNengetu logic;
    private TalonParamDto paramDto;
    private Connection conn;

    @BeforeEach
    void setUp() {
        logic = new IryoChkRyoyoNengetu();
        paramDto = new TalonParamDto();
        conn = mock(Connection.class); // 本ロジックでは未使用なのでモックで十分
    }

    @Test
    @DisplayName("38か月未満 → 正常終了")
    void ok_whenDiffLessThan38() {
        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_SHORI_TUKI, "202508");     // 処理月
        target.put(MAP_KEY_RYOYO_NENGETU, "202306");  // 差は26か月想定

        paramDto.setTargetData(target);

        EventResultDto result = logic.run(conn, paramDto);
        assertTrue(result.getStatus(), "38か月未満ならOK");
        assertNull(result.getMessage(), "OK時はメッセージnull想定");
    }

    @Test
    @DisplayName("ちょうど38か月 → エラー終了")
    void error_whenDiffIsExactly38() {
        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_SHORI_TUKI, "202508");
        target.put(MAP_KEY_RYOYO_NENGETU, "202206");  // 2025-08 と 2022-06 で 38か月

        paramDto.setTargetData(target);

        EventResultDto result = logic.run(conn, paramDto);
        assertFalse(result.getStatus(), "しきい値(>=38)はエラー");
        assertEquals("療養年月が38か月以上のため、給付対象外です。", result.getMessage());
    }

    @Test
    @DisplayName("39か月以上 → エラー終了")
    void error_whenDiffOver38() {
        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_SHORI_TUKI, "202508");
        target.put(MAP_KEY_RYOYO_NENGETU, "202205");  // 39か月以上

        paramDto.setTargetData(target);

        EventResultDto result = logic.run(conn, paramDto);
        assertFalse(result.getStatus(), "38超はエラー");
        assertEquals("療養年月が38か月以上のため、給付対象外です。", result.getMessage());
    }

    @Test
    @DisplayName("形式不正(yyyyMM以外) → スルーして正常")
    void ok_whenInvalidFormat() {
        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_SHORI_TUKI, "2025/08");    // 不正形式
        target.put(MAP_KEY_RYOYO_NENGETU, "202306");

        paramDto.setTargetData(target);

        EventResultDto result = logic.run(conn, paramDto);
        assertTrue(result.getStatus(), "形式不正はチェック対象外でOK");
    }

    @Test
    @DisplayName("未設定(null) → スルーして正常")
    void ok_whenNullInput() {
        Map<String, Object> target = new HashMap<>();
        target.put(MAP_KEY_SHORI_TUKI, null);
        target.put(MAP_KEY_RYOYO_NENGETU, null);

        paramDto.setTargetData(target);

        EventResultDto result = logic.run(conn, paramDto);
        assertTrue(result.getStatus(), "未設定はチェック対象外でOK");
    }

    @Test
    @DisplayName("targetData自体がnullでも落ちない（null-safe）")
    void ok_whenTargetDataIsNull() {
        paramDto.setTargetData(null);

        EventResultDto result = logic.run(conn, paramDto);
        assertTrue(result.getStatus(), "nullでも落ちずOK（SafeMapAccessUtil前提）");
    }
}
