package jp.co.technopro.talon.logic.Gojo.yotaku;

import java.math.BigDecimal;

public class YotakukinContext {
    public final String honTaisyoku;
    public final String haiTaisyoku;
    public final BigDecimal honYotakukin;
    public final BigDecimal honYotakukinShiharai;
    public final BigDecimal haiYotakukin;
    public final BigDecimal haiYotakukinShiharai;

    public YotakukinContext(String honTaisyoku, String haiTaisyoku,
                            BigDecimal honYotakukin, BigDecimal honYotakukinShiharai,
                            BigDecimal haiYotakukin, BigDecimal haiYotakukinShiharai) {
        this.honTaisyoku = honTaisyoku;
        this.haiTaisyoku = haiTaisyoku;
        this.honYotakukin = honYotakukin;
        this.honYotakukinShiharai = honYotakukinShiharai;
        this.haiYotakukin = haiYotakukin;
        this.haiYotakukinShiharai = haiYotakukinShiharai;
    }
}
