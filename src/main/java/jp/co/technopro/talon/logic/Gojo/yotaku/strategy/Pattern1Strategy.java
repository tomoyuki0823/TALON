package jp.co.technopro.talon.logic.Gojo.yotaku.strategy;

import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;

import java.math.BigDecimal;
import java.util.Map;

public class Pattern1Strategy implements YotakukinPatternStrategy {

    @Override
    public void apply(Map<String, Object> map, YotakukinContext ctx) {

        map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
        map.put("HON_TYOIKIN_KINGAKU",
                ("90".equals(ctx.honTaisyoku) || "91".equals(ctx.honTaisyoku))
                        ? ctx.honYotakukin.subtract(ctx.honYotakukinShiharai) : BigDecimal.ZERO);

        map.put("HAI_YOTAKUKIN_KINGAKU",
                "99".equals(ctx.haiTaisyoku)
                        ? ctx.haiYotakukin.subtract(ctx.haiYotakukinShiharai) : BigDecimal.ZERO);
        map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
    }
}
