package jp.co.technopro.talon.logic.Gojo.stayDougai;

import jp.co.technopro.talon.dto.common.BlockDataDto;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.logic.common.ExecutableLogic;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;

import java.sql.Connection;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;

public class StayDougaiService extends GojoAbstractLogicBase {


    private BlockDataDto block1;
    private BlockDataDto block2;

    @Override
    protected EventResultDto executeLogic() {
        
    
        return chkSyukukakuKaisu();
    }
    
    private EventResultDto chkSyukukakuKaisu() {
        
        String tkNo = getTkNo();
        String shorituki = getShoriTuki();
        
        BlockDataDto block1 = paramDto.getBlock1();
        Map<String,Object> blockMap =  block1.getCardData();

        SafeMapAccessUtil.getMap(blockMap, MAP_KEY_SHORI_TUKI);

        

        
        return EventResultDto.ok();
    }
}