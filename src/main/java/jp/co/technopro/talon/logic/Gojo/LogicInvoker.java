package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.mapper.TalonParamMapper;

import java.sql.Connection;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoLogicIdConst.*;

public class LogicInvoker {

    public EventResultDto execute(Connection conn, Map<String, Object> paramMap) throws Exception {

        TalonParamDto paramDto = TalonParamMapper.fromMap(paramMap);
        String logicId = paramDto.getLogicId();

        switch (logicId) {
            case SHINKI:
                return new ShinkiService().run(conn, paramDto);
            case HENKO:
                return new HenkoService().run(conn, paramDto);
            case IRYO:
                return new IryolService().run(conn, paramDto);
            case YOTAKU:
                return new YotakuService().run(conn, paramDto);
            case CHOJU:
                return new ChojuService().run(conn, paramDto);
            case STAY_DOUNAI:
                return new StayDounaiService().run(conn, paramDto);
            case STAY_DOUGAI:
                return new StayDougaiService().run(conn, paramDto);
            case COMMON:
                return new Tkc001Service().run(conn, paramDto);
            default:
                return EventResultDto.error("不明なロジックID: " + logicId);
        }
    }
}
