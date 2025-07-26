package jp.co.technopro.talon.logic.Gojo.yotaku.strategy;

import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;

import java.math.BigDecimal;
import java.util.Map;

public class Pattern7Strategy implements YotakukinPatternStrategy {

    @Override
    public void apply(Map<String, Object> map, YotakukinContext ctx) {

        if ("91".equals(ctx.honTaisyoku) || "92".equals(ctx.honTaisyoku)) {
            if ("99".equals(ctx.haiTaisyoku)) {
                map.put("HON_YOTAKUKIN_KINGAKU", ctx.honYotakukin.subtract(ctx.honYotakukinShiharai));
                map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                map.put("HAI_YOTAKUKIN_KINGAKU", ctx.haiYotakukin.subtract(ctx.haiYotakukinShiharai));
                int kanyuBi = ((Number) map.getOrDefault("HAI_KANYUBI", 0)).intValue();
                int jsk = ((Number) map.getOrDefault("HAI_TYOIKIN_JSK", 0)).intValue();
                if (kanyuBi >= 4180401 && jsk > 0) {
                    map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.valueOf(5000));
                } else {
                    map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                }
            } else {
                map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
                map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
                map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
            }
        } else {
            map.put("HON_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HON_TYOIKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HAI_YOTAKUKIN_KINGAKU", BigDecimal.ZERO);
            map.put("HAI_TYOIKIN_KINGAKU", BigDecimal.ZERO);
        }
    }
}
