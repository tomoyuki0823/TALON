package jp.co.technopro.talon.logic.Gojo.yotaku.strategy;

import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;

import java.math.BigDecimal;
import java.util.Map;

public class Pattern4Strategy implements YotakukinPatternStrategy {

    @Override
    public void apply(Map<String, Object> map, YotakukinContext ctx) {

        if ("91".equals(ctx.honTaisyoku) || "90".equals(ctx.honTaisyoku)) {
            map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
        } else {
            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
            if (!"0".equals(ctx.haiTaisyoku) && "99".equals(ctx.haiTaisyoku)) {
                if (ctx.honYotakukin.compareTo(ctx.haiYotakukin) > 0) {
                    map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                    map.put("HAI_YOTAKUKIN_KINGAKU", ctx.haiYotakukin.subtract(ctx.haiYotakukinShiharai));
                } else {
                    map.put("HON_YOTAKUKIN_KINGAKU", ctx.honYotakukin.subtract(ctx.honYotakukinShiharai));
                    map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                }
            } else {
                map.put("HON_YOTAKUKIN_KINGAKU", ctx.honYotakukin.subtract(ctx.honYotakukinShiharai));
                map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
            }
        }
    }
}
