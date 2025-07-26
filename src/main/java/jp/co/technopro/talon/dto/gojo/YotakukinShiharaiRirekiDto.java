package jp.co.technopro.talon.dto.gojo;


/**
 * 預託金・長寿祝金・弔慰金の支払履歴 DTO
 * 本人・配偶者ごとの支払予定日、支払金額、再発行日等を含む
 */

public class YotakukinShiharaiRirekiDto {

    /** 特別会員番号 */
    private String tkNo;

    // ===== 本人側支払情報 =====

    /** 本人：長寿祝金（77歳）支払予定日_支部 */
    private String honTyoju77ShiharaiYoteiSdt;

    /** 本人：長寿祝金（77歳）支払予定日_本部 */
    private String honTyoju77ShiharaiYoteiWdt;

    /** 本人：長寿祝金（77歳）支払金額 */
    private Integer tyoju77HonShiharaiKingaku;

    /** 本人：長寿祝金（88歳）支払予定日_支部 */
    private String honTyoju88ShiharaiYoteiSdt;

    /** 本人：長寿祝金（88歳）支払予定日_本部 */
    private String honTyoju88ShiharaiYoteiWdt;

    /** 本人：長寿祝金（88歳）支払金額 */
    private Integer tyoju88HonShiharaiKingaku;

    /** 本人：弔慰金支払日_支部 */
    private String honTyoikinShiharaiSdt;

    /** 本人：弔慰金支払日_本部 */
    private String honTyoikinShiharaiWdt;

    /** 本人：弔慰金金額 */
    private Integer honTyoikin;

    /** 本人：預託金支払日_支部 */
    private String honYotakukinShiharaiSdt;

    /** 本人：預託金支払日_本部 */
    private String honYotakukinShiharaiWdt;

    /** 本人：預託金金額 */
    private Integer honYotakukin;

    /** 本人：再発行日_支部 */
    private String honSaihakkouSdt;

    /** 本人：再発行日_本部 */
    private String honSaihakkouWdt;


    // ===== 配偶者側支払情報 =====

    /** 配偶者：長寿祝金（77歳）支払予定日_支部 */
    private String haiTyoju77ShiharaiYoteiSdt;

    /** 配偶者：長寿祝金（77歳）支払予定日_本部 */
    private String haiTyoju77ShiharaiYoteiWdt;

    /** 配偶者：長寿祝金（77歳）支払金額 */
    private Integer tyoju77HaiShiharaiKingaku;

    /** 配偶者：長寿祝金（88歳）支払予定日_支部 */
    private String haiTyoju88ShiharaiYoteiSdt;

    /** 配偶者：長寿祝金（88歳）支払予定日_本部 */
    private String haiTyoju88ShiharaiYoteiWdt;

    /** 配偶者：長寿祝金（88歳）支払金額 */
    private Integer tyoju88HaiShiharaiKingaku;

    /** 配偶者：弔慰金支払日_支部 */
    private String haiTyoukikinSdt;

    /** 配偶者：弔慰金支払日_本部 */
    private String haiTyoukikinWdt;

    /** 配偶者：弔慰金金額 */
    private Integer haiTyoikin;

    /** 配偶者：預託金支払日_支部 */
    private String haiYotakukinShiharaiSdt;

    /** 配偶者：預託金支払日_本部 */
    private String haiYotakukinShiharaiWdt;

    /** 配偶者：預託金金額 */
    private Integer haiYotakukin;

    /** 配偶者：再発行日_支部 */
    private String haiSaihakkouSdt;

    /** 配偶者：再発行日_本部 */
    private String haiSaihakkouWdt;

    public String getHonTyoju77ShiharaiYoteiSdt() {
        return honTyoju77ShiharaiYoteiSdt;
    }

    public void setHonTyoju77ShiharaiYoteiSdt(String honTyoju77ShiharaiYoteiSdt) {
        this.honTyoju77ShiharaiYoteiSdt = honTyoju77ShiharaiYoteiSdt;
    }

    public String getTkNo() {
        return tkNo;
    }

    public void setTkNo(String tkNo) {
        this.tkNo = tkNo;
    }

    public String getHonTyoju77ShiharaiYoteiWdt() {
        return honTyoju77ShiharaiYoteiWdt;
    }

    public void setHonTyoju77ShiharaiYoteiWdt(String honTyoju77ShiharaiYoteiWdt) {
        this.honTyoju77ShiharaiYoteiWdt = honTyoju77ShiharaiYoteiWdt;
    }

    public Integer getTyoju77HonShiharaiKingaku() {
        return tyoju77HonShiharaiKingaku;
    }

    public void setTyoju77HonShiharaiKingaku(Integer tyoju77HonShiharaiKingaku) {
        this.tyoju77HonShiharaiKingaku = tyoju77HonShiharaiKingaku;
    }

    public String getHonTyoju88ShiharaiYoteiSdt() {
        return honTyoju88ShiharaiYoteiSdt;
    }

    public void setHonTyoju88ShiharaiYoteiSdt(String honTyoju88ShiharaiYoteiSdt) {
        this.honTyoju88ShiharaiYoteiSdt = honTyoju88ShiharaiYoteiSdt;
    }

    public String getHonTyoju88ShiharaiYoteiWdt() {
        return honTyoju88ShiharaiYoteiWdt;
    }

    public void setHonTyoju88ShiharaiYoteiWdt(String honTyoju88ShiharaiYoteiWdt) {
        this.honTyoju88ShiharaiYoteiWdt = honTyoju88ShiharaiYoteiWdt;
    }

    public Integer getTyoju88HonShiharaiKingaku() {
        return tyoju88HonShiharaiKingaku;
    }

    public void setTyoju88HonShiharaiKingaku(Integer tyoju88HonShiharaiKingaku) {
        this.tyoju88HonShiharaiKingaku = tyoju88HonShiharaiKingaku;
    }

    public String getHonTyoikinShiharaiSdt() {
        return honTyoikinShiharaiSdt;
    }

    public void setHonTyoikinShiharaiSdt(String honTyoikinShiharaiSdt) {
        this.honTyoikinShiharaiSdt = honTyoikinShiharaiSdt;
    }

    public String getHonTyoikinShiharaiWdt() {
        return honTyoikinShiharaiWdt;
    }

    public void setHonTyoikinShiharaiWdt(String honTyoikinShiharaiWdt) {
        this.honTyoikinShiharaiWdt = honTyoikinShiharaiWdt;
    }

    public Integer getHonTyoikin() {
        return honTyoikin;
    }

    public void setHonTyoikin(Integer honTyoikin) {
        this.honTyoikin = honTyoikin;
    }

    public String getHonYotakukinShiharaiSdt() {
        return honYotakukinShiharaiSdt;
    }

    public void setHonYotakukinShiharaiSdt(String honYotakukinShiharaiSdt) {
        this.honYotakukinShiharaiSdt = honYotakukinShiharaiSdt;
    }

    public String getHonYotakukinShiharaiWdt() {
        return honYotakukinShiharaiWdt;
    }

    public void setHonYotakukinShiharaiWdt(String honYotakukinShiharaiWdt) {
        this.honYotakukinShiharaiWdt = honYotakukinShiharaiWdt;
    }

    public Integer getHonYotakukin() {
        return honYotakukin;
    }

    public void setHonYotakukin(Integer honYotakukin) {
        this.honYotakukin = honYotakukin;
    }

    public String getHonSaihakkouSdt() {
        return honSaihakkouSdt;
    }

    public void setHonSaihakkouSdt(String honSaihakkouSdt) {
        this.honSaihakkouSdt = honSaihakkouSdt;
    }

    public String getHonSaihakkouWdt() {
        return honSaihakkouWdt;
    }

    public void setHonSaihakkouWdt(String honSaihakkouWdt) {
        this.honSaihakkouWdt = honSaihakkouWdt;
    }

    public String getHaiTyoju77ShiharaiYoteiSdt() {
        return haiTyoju77ShiharaiYoteiSdt;
    }

    public void setHaiTyoju77ShiharaiYoteiSdt(String haiTyoju77ShiharaiYoteiSdt) {
        this.haiTyoju77ShiharaiYoteiSdt = haiTyoju77ShiharaiYoteiSdt;
    }

    public String getHaiTyoju77ShiharaiYoteiWdt() {
        return haiTyoju77ShiharaiYoteiWdt;
    }

    public void setHaiTyoju77ShiharaiYoteiWdt(String haiTyoju77ShiharaiYoteiWdt) {
        this.haiTyoju77ShiharaiYoteiWdt = haiTyoju77ShiharaiYoteiWdt;
    }

    public Integer getTyoju77HaiShiharaiKingaku() {
        return tyoju77HaiShiharaiKingaku;
    }

    public void setTyoju77HaiShiharaiKingaku(Integer tyoju77HaiShiharaiKingaku) {
        this.tyoju77HaiShiharaiKingaku = tyoju77HaiShiharaiKingaku;
    }

    public String getHaiTyoju88ShiharaiYoteiSdt() {
        return haiTyoju88ShiharaiYoteiSdt;
    }

    public void setHaiTyoju88ShiharaiYoteiSdt(String haiTyoju88ShiharaiYoteiSdt) {
        this.haiTyoju88ShiharaiYoteiSdt = haiTyoju88ShiharaiYoteiSdt;
    }

    public String getHaiTyoju88ShiharaiYoteiWdt() {
        return haiTyoju88ShiharaiYoteiWdt;
    }

    public void setHaiTyoju88ShiharaiYoteiWdt(String haiTyoju88ShiharaiYoteiWdt) {
        this.haiTyoju88ShiharaiYoteiWdt = haiTyoju88ShiharaiYoteiWdt;
    }

    public Integer getTyoju88HaiShiharaiKingaku() {
        return tyoju88HaiShiharaiKingaku;
    }

    public void setTyoju88HaiShiharaiKingaku(Integer tyoju88HaiShiharaiKingaku) {
        this.tyoju88HaiShiharaiKingaku = tyoju88HaiShiharaiKingaku;
    }

    public String getHaiTyoukikinSdt() {
        return haiTyoukikinSdt;
    }

    public void setHaiTyoukikinSdt(String haiTyoukikinSdt) {
        this.haiTyoukikinSdt = haiTyoukikinSdt;
    }

    public String getHaiTyoukikinWdt() {
        return haiTyoukikinWdt;
    }

    public void setHaiTyoukikinWdt(String haiTyoukikinWdt) {
        this.haiTyoukikinWdt = haiTyoukikinWdt;
    }

    public Integer getHaiTyoikin() {
        return haiTyoikin;
    }

    public void setHaiTyoikin(Integer haiTyoikin) {
        this.haiTyoikin = haiTyoikin;
    }

    public String getHaiYotakukinShiharaiSdt() {
        return haiYotakukinShiharaiSdt;
    }

    public void setHaiYotakukinShiharaiSdt(String haiYotakukinShiharaiSdt) {
        this.haiYotakukinShiharaiSdt = haiYotakukinShiharaiSdt;
    }

    public String getHaiYotakukinShiharaiWdt() {
        return haiYotakukinShiharaiWdt;
    }

    public void setHaiYotakukinShiharaiWdt(String haiYotakukinShiharaiWdt) {
        this.haiYotakukinShiharaiWdt = haiYotakukinShiharaiWdt;
    }

    public Integer getHaiYotakukin() {
        return haiYotakukin;
    }

    public void setHaiYotakukin(Integer haiYotakukin) {
        this.haiYotakukin = haiYotakukin;
    }

    public String getHaiSaihakkouSdt() {
        return haiSaihakkouSdt;
    }

    public void setHaiSaihakkouSdt(String haiSaihakkouSdt) {
        this.haiSaihakkouSdt = haiSaihakkouSdt;
    }

    public String getHaiSaihakkouWdt() {
        return haiSaihakkouWdt;
    }

    public void setHaiSaihakkouWdt(String haiSaihakkouWdt) {
        this.haiSaihakkouWdt = haiSaihakkouWdt;
    }
}
