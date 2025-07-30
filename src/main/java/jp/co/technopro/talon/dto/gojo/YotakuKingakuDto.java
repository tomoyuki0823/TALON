package jp.co.technopro.talon.dto.gojo;

import java.math.BigDecimal;

/**
 * 預託金・弔慰金 金額DTO。
 * <p>
 * TK_MEMBER テーブルに登録される、本人・配偶者の各種支給金額を保持します。
 */
/**
 * 預託金・弔慰金支給額を格納するDTO。
 */
public class YotakuKingakuDto {

    private BigDecimal honYotaku = BigDecimal.ZERO;
    private BigDecimal haiYotaku = BigDecimal.ZERO;
    private BigDecimal honTyoi = BigDecimal.ZERO;
    private BigDecimal haiTyoi = BigDecimal.ZERO;

    public BigDecimal getHonYotaku() {
        return honYotaku;
    }

    public void setHonYotaku(BigDecimal honYotaku) {
        this.honYotaku = honYotaku != null ? honYotaku : BigDecimal.ZERO;
    }

    public BigDecimal getHaiYotaku() {
        return haiYotaku;
    }

    public void setHaiYotaku(BigDecimal haiYotaku) {
        this.haiYotaku = haiYotaku != null ? haiYotaku : BigDecimal.ZERO;
    }

    public BigDecimal getHonTyoi() {
        return honTyoi;
    }

    public void setHonTyoi(BigDecimal honTyoi) {
        this.honTyoi = honTyoi != null ? honTyoi : BigDecimal.ZERO;
    }

    public BigDecimal getHaiTyoi() {
        return haiTyoi;
    }

    public void setHaiTyoi(BigDecimal haiTyoi) {
        this.haiTyoi = haiTyoi != null ? haiTyoi : BigDecimal.ZERO;
    }

    public boolean isAllZero() {
        return honYotaku.compareTo(BigDecimal.ZERO) == 0 &&
                haiYotaku.compareTo(BigDecimal.ZERO) == 0 &&
                honTyoi.compareTo(BigDecimal.ZERO) == 0 &&
                haiTyoi.compareTo(BigDecimal.ZERO) == 0;
    }
}
