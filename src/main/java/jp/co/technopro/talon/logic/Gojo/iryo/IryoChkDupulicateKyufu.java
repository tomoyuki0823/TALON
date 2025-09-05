package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;

public class IryoChkDupulicateKyufu extends GojoAbstractLogicBase {

    @Override
    protected EventResultDto executeLogic() {
        //　ここが一番最初に実行される

        return chkDupulicateKyufu();
    }

    private EventResultDto chkDupulicateKyufu() {


        return EventResultDto.ok();

    }
}
