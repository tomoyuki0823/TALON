package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.iryo.SetIryoAlert;
import jp.co.technopro.talon.util.Gojo.GojoDbUtil;
import jp.co.technopro.talon.util.Gojo.GojoSoftFlagService;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.DbUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_COM_M_BANK;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;
import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.FLG_ON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SetIryoAlert の黒箱テスト例。
 * AbstractLogicBase#run(conn, paramDto) を呼び出し、
 * 内部の setGinko() を含むフローを検証する。
 */
class IryoSetIryoAlertTest {

    @Test
    void run_shouldSetFlag_whenBankMasterNotFound() {
        // Arrange
        SetIryoAlert logic = new SetIryoAlert();

        Connection conn = mock(Connection.class);
        TalonParamDto paramDto = mock(TalonParamDto.class);
        TkMemberDto memberDto = mock(TkMemberDto.class);

        // ▼ paramDto が返すターゲットデータを準備（SafeMapAccessUtil で読む想定のキー）
        Map<String, Object> target = new LinkedHashMap<>();
        target.put(MAP_KEY_TK_NO, "12172");
        target.put(MAP_KEY_ZOKU, "0");
        target.put(MAP_KEY_SHORI_TUKI, "202504");
        target.put(MAP_KEY_RYOYO_NENGETU, "202503");

        when(paramDto.getTargetData()).thenReturn(target);
        when(paramDto.getCompanyCode()).thenReturn("GOJO");

        // ▼ TkMemberDto（run() 内で setTkMemberDto が返す前提なら、executeLogic 側を差し替えるか、
        //    ここでは簡易に SetIryoAlert のメンバ取得部分がこの memberDto を使うよう
        //    必要ならコンストラクタ／セッタ導線を用意してください。
        //    サンプルでは setGinko() が memberDto を直接参照しているので、
        //    executeLogic() が setGinko(memberDto) を呼ぶ現在の実装どおりに動く前提で stub）
        when(memberDto.getGinkouCd()).thenReturn("001");
        when(memberDto.getShitenCd()).thenReturn("002");
        when(memberDto.getTkNo()).thenReturn("12172");
        when(memberDto.getJgyKbn()).thenReturn("9"); // setJgyChk が走らないように（TK_DVS_JGY_KBN_2 以外）
        // 続柄により加入/退会などを見る箇所があれば、必要に応じて stub

        // ▼ static の外部呼び出しをモック
        try (
                MockedStatic<GojoDbUtil> gojoDbUtil = mockStatic(GojoDbUtil.class);
                MockedStatic<DbUtil> db = mockStatic(DbUtil.class);
                MockedStatic<GojoSoftFlagService> soft = mockStatic(GojoSoftFlagService.class)) {

            // ★ ここが今回の NPE 回避ポイント：内部で呼ばれる setTkMemberDto を丸ごとモック
            gojoDbUtil.when(() -> GojoDbUtil.setTkMemberDto(eq(conn), anyString(), eq(paramDto)))
                    .thenReturn(memberDto);

            // 銀行マスタ存在チェック：0件（未存在）
            db.when(() -> DbUtil.getCount(
                    eq(conn),
                    eq(TABLE_COM_M_BANK),
                    eq(Map.of(MAP_KEY_BANK_CD, "001", MAP_KEY_SHITEN_CD, "002")),
                    eq("GOJO")
            )).thenReturn(0);

            // ▼ run() が内部で TkMemberDto を作る実装なら、
            //    SetIryoAlert の executeLogic() を差し替えたテスト用サブクラスにして
            //    そこで memberDto を差し込む、という方法もあります。
            //    ここでは簡便化のため、SetIryoAlert#executeLogic が
            //    setIryoAlert(memberDto) を呼ぶように“注入”できているものとします。
            //    実プロジェクトでは DI かテスト用フックを用意してください。

            // Act
            // 実行：AbstractLogicBase#run が this.conn/paramDto をセットして executeLogic を呼ぶ
            EventResultDto result = logic.run(conn, paramDto);

            // Assert
            assertEquals(EventResultDto.ok().getStatus(), result.getStatus());

            // マスタ照会が期待通り呼ばれたか
            db.verify(() -> DbUtil.getCount(
                    eq(conn),
                    eq(TABLE_COM_M_BANK),
                    eq(Map.of(MAP_KEY_BANK_CD, "001", MAP_KEY_SHITEN_CD, "002")),
                    eq("GOJO")
            ), times(1));

            // 未存在なので、フラグ更新が呼ばれていること
            soft.verify(() -> GojoSoftFlagService.updateFlagSilently(
                    eq(conn),
                    eq(TABLE_TK_IRYO),
                    eq("GINKO_MST_NG_FLG"),
                    eq(FLG_ON),
                    argThat(where -> "TK0001".equals(where.get(MAP_KEY_TK_NO))
                            && "0".equals(where.get(MAP_KEY_ZOKU))
                            && "202504".equals(where.get(MAP_KEY_SHORI_TUKI))
                            && "202503".equals(where.get(MAP_KEY_RYOYO_NENGETU)))
            ), times(1));
        }
    }
}
