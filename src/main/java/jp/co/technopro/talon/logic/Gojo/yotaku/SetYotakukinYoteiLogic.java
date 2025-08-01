package jp.co.technopro.talon.logic.Gojo.yotaku;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;
import jp.co.technopro.talon.logic.Gojo.yotaku.strategy.YotakukinStrategyFactory;
import jp.co.technopro.talon.util.Gojo.GojoCalcUtil;
import jp.co.technopro.talon.util.common.DbUtil;
import jp.co.technopro.talon.util.common.StringUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMessagesConst.MSG_NON_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_T_YOTEKUKIN_YOTEI;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.*;

public class SetYotakukinYoteiLogic extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {

        String tkNo = getTkNo();

        if (StringUtil.isNullOrEmpty(tkNo)) return EventResultDto.error(MSG_NON_TK_NO);

        TkMemberDto memberDto = setTkMemberDto(conn, tkNo, paramDto);
        if (memberDto == null) return EventResultDto.error("対象会員が存在しません。");

        List<Map<String, Object>> yoteiMstList = getMstYotakukin(conn, paramDto);
        deleteExistingYotei(conn, tkNo, paramDto.getCompanyCode());

        YotakukinContext ctx = buildYotakukinContext(memberDto);
        insertYoteiRecords(conn, paramDto.getCompanyCode(), tkNo, yoteiMstList, ctx);

        return EventResultDto.ok();
    }


    private void deleteExistingYotei(Connection conn, String tkNo, String companyCd) {
        try {
            delYotakuyotei(conn, tkNo, companyCd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private YotakukinContext buildYotakukinContext(TkMemberDto dto) {
        var paid = dto.getYotakukinShiharaiRirekiDto();
        return new YotakukinContext(
                dto.getHonTaisyokuCd(),
                dto.getHaiTaisyokuCd(),
                GojoCalcUtil.getBigDecimal(dto.getHonYotakukin()),
                GojoCalcUtil.getBigDecimal(paid.getHonYotakukin()),
                GojoCalcUtil.getBigDecimal(dto.getHaiYotakukin()),
                GojoCalcUtil.getBigDecimal(paid.getHaiYotakukin())
        );
    }

    private void insertYoteiRecords(Connection conn, String companyCd, String tkNo,
                                    List<Map<String, Object>> yoteiMstList, YotakukinContext ctx) {
        for (Map<String, Object> row : yoteiMstList) {
            String ptnCd = String.valueOf(row.get("PTN_CD"));
            if (StringUtil.isNullOrEmpty(ptnCd)) continue;

            YotakukinStrategyFactory.get(ptnCd).ifPresent(strategy -> strategy.apply(row, ctx));

            row.put(MAP_KEY_TK_NO, tkNo);
            try {
                DbUtil.insertByMapEx(conn, companyCd, TABLE_TK_T_YOTEKUKIN_YOTEI, row, false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}