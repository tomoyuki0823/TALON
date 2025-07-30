package jp.co.technopro.talon.util.Gojo;

import jp.co.technopro.talon.dto.common.TalonParamDto;

import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;

public class GojoLogicUtil {

    /**
     * 処理月をDTOから抽出（空文字考慮）
     */
    public static String getShoriTukiFromConditionData(TalonParamDto dto) {
        Object raw = dto.getConditionData().get(MAP_KEY_SHORI_TUKI);
        return (raw instanceof String && !((String) raw).isBlank()) ? (String) raw : null;
    }
}
