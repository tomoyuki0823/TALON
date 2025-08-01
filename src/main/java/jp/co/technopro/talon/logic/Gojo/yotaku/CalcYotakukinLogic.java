package jp.co.technopro.talon.logic.Gojo.yotaku;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.dto.gojo.YotakuKingakuDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.AbstractLogicBase;
import jp.co.technopro.talon.sql.common.SqlLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoSqlXmlPathConst.SQL_GOJO_YOTAKU;

public class CalcYotakukinLogic extends GojoAbstractLogicBase {

    private final SqlLoader sqlLoader = new SqlLoader(SQL_GOJO_YOTAKU);

    @Override
    protected EventResultDto executeLogic()  {
        Map<String, Object> paramMap = paramDto.getTargetData();
        String tkNo = getTkNo();
        String honCd = (String) paramMap.get(MAP_KEY_HON_TAISYOKU_CD);
        String haiCd = (String) paramMap.get(MAP_KEY_HAI_TAISYOKU_CD);

        YotakuKingakuDto dto = calcYotakukinAmount(tkNo, honCd, haiCd);

        if (dto.isAllZero()) {
            dto = new YotakuKingakuDto();
        }

        String updateSql = sqlLoader.get("UPDATE_YOTAKU");
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, dto.getHonYotaku());
            ps.setBigDecimal(2, dto.getHonTyoi());
            ps.setBigDecimal(3, dto.getHaiYotaku());
            ps.setBigDecimal(4, dto.getHaiTyoi());
            ps.setString(5, tkNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return EventResultDto.ok();
    }

    private YotakuKingakuDto calcYotakukinAmount(String tkNo, String honCd, String haiCd)  {
        YotakuKingakuDto dto = new YotakuKingakuDto();
        String selectSql = sqlLoader.get("CALC_YOTAKUKIN");

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, tkNo);
            ps.setString(2, honCd);
            ps.setString(3, haiCd);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setHonYotaku(rs.getBigDecimal("HON_YOTAKUKIN_KINGAKU"));
                    dto.setHaiYotaku(rs.getBigDecimal("HAI_YOTAKUKIN_KINGAKU"));
                    dto.setHonTyoi(rs.getBigDecimal("HON_TYOIKIN_KINGAKU"));
                    dto.setHaiTyoi(rs.getBigDecimal("HAI_TYOIKIN_KINGAKU"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dto;
    }
}
