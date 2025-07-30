package jp.co.technopro.talon.dto.gojo;

public class TkMemberDto {

    /** 現職時会員番号 */
    private String no;

    /** 特別会員番号 */
    private String tkNo;

    /** 本人続柄 */
    private String honZoku;

    /** 職名 */
    private String shokuMei;

    /** 職名名称 */
    private String syokumeiNm;

    /** 配偶者加入 */
    private String haiKanyuUmu;

    /** 配偶者番号 */
    private String haiNinteiNo;

    /** 不明者 */
    private String humeisha;

    /** 支部非加入 */
    private String sibuHikanyu;

    /** 支部コード */
    private String sibuCd;

    /** 支部名 */
    private String sibuNm;

    /** 市町村コード */
    private String sichosonCd;

    /** 市町村名 */
    private String sityosonNm;

    /** 本人氏名 */
    private String honSimei;

    /** 本人カナ氏名 */
    private String honKanaSimei;

    /** 本人性別 */
    private String honSeibetu;

    /** 本人生年月日 */
    private String honSeinengapi;

    /** 現職本人死亡日 */
    private String honSibobi;

    /** 本人加入時年齢 */
    private String honKanyuNenrei;

    /** 本人加入日 */
    private String honKanyuSeinengapi;

    /** 本人退会予定日 */
    private String honTaikaiYoteibi;

    /** 本人退会日 */
    private String honTaikaiSeinengapi;

    /** 本人退会事由 */
    private String honTaikaiJiyu;

    /** 事業区分 */
    private String jgyKbn;

    /** 本人預託金 */
    private String honYotakukin;

    /** 本人生きがい事業拠出金 */
    private String honIkigai;

    /** 本人医療費給付拠出金 */
    private String honIryohi;

    /** 退会時所属 */
    private String shozokuMei;

    /** 配偶者漢字氏名 */
    private String haiSimei;

    /** 配偶者カナ氏名 */
    private String haiKanaSimei;

    /** 配偶者性別 */
    private String haiSeibetu;

    /** 配偶者生年月日 */
    private String haiSeinengapi;

    /** 配偶者続柄 */
    private String haiZoku;

    /** 配偶者加入時年齢 */
    private String haiKanyuNenrei;

    /** 配偶者加入日 */
    private String haiKanyubi;

    /** 配偶者退会予定日 */
    private String haiTaikaiYoteibi;

    /** 配偶者退会日 */
    private String haiTaikaiSeinengapi;

    /** 配偶者退会事由 */
    private String haiTaikaiJiyu;

    /** 扶養認定 */
    private String haiNintei;

    /** 配偶者預託金 */
    private String haiYotakukin;

    /** 配偶者生きがい事業拠出金 */
    private String haiIkigai;

    /** 配偶者医療費給付拠出金 */
    private String haiIryohi;

    /** 非認定配偶者氏名 */
    private String hininHaiSimei;

    /** 非認定配偶者カナ氏名 */
    private String hininHaiKanaSimei;

    /** 非認定配偶者性別 */
    private String hininHaiSeibetu;

    /** 非認定配偶者生年月日 */
    private String hininHaiSeinengappi;

    /** 非認定配偶者加入日 */
    private String hininHaiKanyubi;

    /** 非認定配偶者退会日 */
    private String hininHaiTaikaiSeinengapi;

    /** 非認定配偶者退会事由 */
    private String hininHaiTaikaiJiyu;

    /** 郵便番号 */
    private String yubinNo;

    /** 住所1 */
    private String jusho1;

    /** 住所2 */
    private String jusho2;

    /** 電話番号1 */
    private String tel;

    /** 電話番号2 */
    private String tel2;

    /** 銀行コード */
    private String ginkouCd;

    /** 銀行名 */
    private String ginkouNm;

    /** 支店コード */
    private String shitenCd;

    /** 支店名 */
    private String shitenNm;

    /** 郵振 */
    private String yufuri;

    /** 種別 */
    private String shubetu;

    /** 口座番号 */
    private String kouzaNo;

    /** 口座名義人 */
    private String kouzameigi;

    /** 共済番号 */
    private String kyosaiNo;

    /** コメント1 */
    private String cmt1;

    /** コメント2 */
    private String cmt2;

    /** 特記事項 */
    private String cmt3;

    /** 登録日時 */
    private String createdDate;

    /** 登録者 */
    private String createdBy;

    /** 登録プログラム */
    private String createdPrgNm;

    /** 更新日時 */
    private String updatedDate;

    /** 更新者 */
    private String updatedBy;

    /** 更新プログラム */
    private String updatedPrgNm;

    /** 更新回数 */
    private String modifyCount;

    /** 本人退職コード */
    private String honTaisyokuCd;

    /** 配偶者退職コード */
    private String haiTaisyokuCd;

    private YotakukinShiharaiRirekiDto yotakukinShiharaiRirekiDto;

    private String shoriTuki;

    public String getHaiKanyuUmu() {
        return haiKanyuUmu;
    }

    public void setHaiKanyuUmu(String haiKanyuUmu) {
        this.haiKanyuUmu = haiKanyuUmu;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getTkNo() {
        return tkNo;
    }

    public void setTkNo(String tkNo) {
        this.tkNo = tkNo;
    }

    public String getHonZoku() {
        return honZoku;
    }

    public void setHonZoku(String honZoku) {
        this.honZoku = honZoku;
    }

    public String getShokuMei() {
        return shokuMei;
    }

    public void setShokuMei(String shokuMei) {
        this.shokuMei = shokuMei;
    }

    public String getSyokumeiNm() {
        return syokumeiNm;
    }

    public void setSyokumeiNm(String syokumeiNm) {
        this.syokumeiNm = syokumeiNm;
    }

    public String getSibuCd() {
        return sibuCd;
    }

    public void setSibuCd(String sibuCd) {
        this.sibuCd = sibuCd;
    }

    public String getSibuNm() {
        return sibuNm;
    }

    public void setSibuNm(String sibuNm) {
        this.sibuNm = sibuNm;
    }

    public String getSichosonCd() {
        return sichosonCd;
    }

    public void setSichosonCd(String sichosonCd) {
        this.sichosonCd = sichosonCd;
    }

    public String getSityosonNm() {
        return sityosonNm;
    }

    public void setSityosonNm(String sityosonNm) {
        this.sityosonNm = sityosonNm;
    }

    public String getHonSimei() {
        return honSimei;
    }

    public void setHonSimei(String honSimei) {
        this.honSimei = honSimei;
    }

    public String getHonKanaSimei() {
        return honKanaSimei;
    }

    public void setHonKanaSimei(String honKanaSimei) {
        this.honKanaSimei = honKanaSimei;
    }

    public String getHonSeibetu() {
        return honSeibetu;
    }

    public void setHonSeibetu(String honSeibetu) {
        this.honSeibetu = honSeibetu;
    }

    public String getHonSeinengapi() {
        return honSeinengapi;
    }

    public void setHonSeinengapi(String honSeinengapi) {
        this.honSeinengapi = honSeinengapi;
    }

    public String getHonSibobi() {
        return honSibobi;
    }

    public void setHonSibobi(String honSibobi) {
        this.honSibobi = honSibobi;
    }

    public String getHonKanyuNenrei() {
        return honKanyuNenrei;
    }

    public void setHonKanyuNenrei(String honKanyuNenrei) {
        this.honKanyuNenrei = honKanyuNenrei;
    }

    public String getHonKanyuSeinengapi() {
        return honKanyuSeinengapi;
    }

    public void setHonKanyuSeinengapi(String honKanyuSeinengapi) {
        this.honKanyuSeinengapi = honKanyuSeinengapi;
    }

    public String getHonTaikaiYoteibi() {
        return honTaikaiYoteibi;
    }

    public void setHonTaikaiYoteibi(String honTaikaiYoteibi) {
        this.honTaikaiYoteibi = honTaikaiYoteibi;
    }

    public String getHonTaikaiSeinengapi() {
        return honTaikaiSeinengapi;
    }

    public void setHonTaikaiSeinengapi(String honTaikaiSeinengapi) {
        this.honTaikaiSeinengapi = honTaikaiSeinengapi;
    }

    public String getHonTaikaiJiyu() {
        return honTaikaiJiyu;
    }

    public void setHonTaikaiJiyu(String honTaikaiJiyu) {
        this.honTaikaiJiyu = honTaikaiJiyu;
    }

    public String getJgyKbn() {
        return jgyKbn;
    }

    public void setJgyKbn(String jgyKbn) {
        this.jgyKbn = jgyKbn;
    }

    public String getHonYotakukin() {
        return honYotakukin;
    }

    public void setHonYotakukin(String honYotakukin) {
        this.honYotakukin = honYotakukin;
    }

    public String getHonIkigai() {
        return honIkigai;
    }

    public void setHonIkigai(String honIkigai) {
        this.honIkigai = honIkigai;
    }

    public String getHonIryohi() {
        return honIryohi;
    }

    public void setHonIryohi(String honIryohi) {
        this.honIryohi = honIryohi;
    }

    public String getShozokuMei() {
        return shozokuMei;
    }

    public void setShozokuMei(String shozokuMei) {
        this.shozokuMei = shozokuMei;
    }

    public String getHaiSimei() {
        return haiSimei;
    }

    public void setHaiSimei(String haiSimei) {
        this.haiSimei = haiSimei;
    }

    public String getHaiKanaSimei() {
        return haiKanaSimei;
    }

    public void setHaiKanaSimei(String haiKanaSimei) {
        this.haiKanaSimei = haiKanaSimei;
    }

    public String getSibuHikanyu() {
        return sibuHikanyu;
    }

    public void setSibuHikanyu(String sibuHikanyu) {
        this.sibuHikanyu = sibuHikanyu;
    }

    public String getHumeisha() {
        return humeisha;
    }

    public void setHumeisha(String humeisha) {
        this.humeisha = humeisha;
    }

    public String getHaiNinteiNo() {
        return haiNinteiNo;
    }

    public void setHaiNinteiNo(String haiNinteiNo) {
        this.haiNinteiNo = haiNinteiNo;
    }

    public String getHaiSeibetu() {
        return haiSeibetu;
    }

    public void setHaiSeibetu(String haiSeibetu) {
        this.haiSeibetu = haiSeibetu;
    }

    public String getHaiSeinengapi() {
        return haiSeinengapi;
    }

    public void setHaiSeinengapi(String haiSeinengapi) {
        this.haiSeinengapi = haiSeinengapi;
    }

    public String getHaiZoku() {
        return haiZoku;
    }

    public void setHaiZoku(String haiZoku) {
        this.haiZoku = haiZoku;
    }

    public String getHaiKanyuNenrei() {
        return haiKanyuNenrei;
    }

    public void setHaiKanyuNenrei(String haiKanyuNenrei) {
        this.haiKanyuNenrei = haiKanyuNenrei;
    }

    public String getHaiKanyubi() {
        return haiKanyubi;
    }

    public void setHaiKanyubi(String haiKanyubi) {
        this.haiKanyubi = haiKanyubi;
    }

    public String getHaiTaikaiYoteibi() {
        return haiTaikaiYoteibi;
    }

    public void setHaiTaikaiYoteibi(String haiTaikaiYoteibi) {
        this.haiTaikaiYoteibi = haiTaikaiYoteibi;
    }

    public String getHaiTaikaiSeinengapi() {
        return haiTaikaiSeinengapi;
    }

    public void setHaiTaikaiSeinengapi(String haiTaikaiSeinengapi) {
        this.haiTaikaiSeinengapi = haiTaikaiSeinengapi;
    }

    public String getHaiTaikaiJiyu() {
        return haiTaikaiJiyu;
    }

    public void setHaiTaikaiJiyu(String haiTaikaiJiyu) {
        this.haiTaikaiJiyu = haiTaikaiJiyu;
    }

    public String getHaiNintei() {
        return haiNintei;
    }

    public void setHaiNintei(String haiNintei) {
        this.haiNintei = haiNintei;
    }

    public String getHaiYotakukin() {
        return haiYotakukin;
    }

    public void setHaiYotakukin(String haiYotakukin) {
        this.haiYotakukin = haiYotakukin;
    }

    public String getHaiIkigai() {
        return haiIkigai;
    }

    public void setHaiIkigai(String haiIkigai) {
        this.haiIkigai = haiIkigai;
    }

    public String getHaiIryohi() {
        return haiIryohi;
    }

    public void setHaiIryohi(String haiIryohi) {
        this.haiIryohi = haiIryohi;
    }

    public String getHininHaiSimei() {
        return hininHaiSimei;
    }

    public void setHininHaiSimei(String hininHaiSimei) {
        this.hininHaiSimei = hininHaiSimei;
    }

    public String getHininHaiKanaSimei() {
        return hininHaiKanaSimei;
    }

    public void setHininHaiKanaSimei(String hininHaiKanaSimei) {
        this.hininHaiKanaSimei = hininHaiKanaSimei;
    }

    public String getHininHaiSeibetu() {
        return hininHaiSeibetu;
    }

    public void setHininHaiSeibetu(String hininHaiSeibetu) {
        this.hininHaiSeibetu = hininHaiSeibetu;
    }

    public String getHininHaiSeinengappi() {
        return hininHaiSeinengappi;
    }

    public void setHininHaiSeinengappi(String hininHaiSeinengappi) {
        this.hininHaiSeinengappi = hininHaiSeinengappi;
    }

    public String getHininHaiKanyubi() {
        return hininHaiKanyubi;
    }

    public void setHininHaiKanyubi(String hininHaiKanyubi) {
        this.hininHaiKanyubi = hininHaiKanyubi;
    }

    public String getHininHaiTaikaiSeinengapi() {
        return hininHaiTaikaiSeinengapi;
    }

    public void setHininHaiTaikaiSeinengapi(String hininHaiTaikaiSeinengapi) {
        this.hininHaiTaikaiSeinengapi = hininHaiTaikaiSeinengapi;
    }

    public String getHininHaiTaikaiJiyu() {
        return hininHaiTaikaiJiyu;
    }

    public void setHininHaiTaikaiJiyu(String hininHaiTaikaiJiyu) {
        this.hininHaiTaikaiJiyu = hininHaiTaikaiJiyu;
    }

    public String getYubinNo() {
        return yubinNo;
    }

    public void setYubinNo(String yubinNo) {
        this.yubinNo = yubinNo;
    }

    public String getJusho1() {
        return jusho1;
    }

    public void setJusho1(String jusho1) {
        this.jusho1 = jusho1;
    }

    public String getJusho2() {
        return jusho2;
    }

    public void setJusho2(String jusho2) {
        this.jusho2 = jusho2;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTel2() {
        return tel2;
    }

    public void setTel2(String tel2) {
        this.tel2 = tel2;
    }

    public String getGinkouCd() {
        return ginkouCd;
    }

    public void setGinkouCd(String ginkouCd) {
        this.ginkouCd = ginkouCd;
    }

    public String getGinkouNm() {
        return ginkouNm;
    }

    public void setGinkouNm(String ginkouNm) {
        this.ginkouNm = ginkouNm;
    }

    public String getShitenCd() {
        return shitenCd;
    }

    public void setShitenCd(String shitenCd) {
        this.shitenCd = shitenCd;
    }

    public String getShitenNm() {
        return shitenNm;
    }

    public void setShitenNm(String shitenNm) {
        this.shitenNm = shitenNm;
    }

    public String getYufuri() {
        return yufuri;
    }

    public void setYufuri(String yufuri) {
        this.yufuri = yufuri;
    }

    public String getShubetu() {
        return shubetu;
    }

    public void setShubetu(String shubetu) {
        this.shubetu = shubetu;
    }

    public String getKouzaNo() {
        return kouzaNo;
    }

    public void setKouzaNo(String kouzaNo) {
        this.kouzaNo = kouzaNo;
    }

    public String getKouzameigi() {
        return kouzameigi;
    }

    public void setKouzameigi(String kouzameigi) {
        this.kouzameigi = kouzameigi;
    }

    public String getKyosaiNo() {
        return kyosaiNo;
    }

    public void setKyosaiNo(String kyosaiNo) {
        this.kyosaiNo = kyosaiNo;
    }

    public String getCmt1() {
        return cmt1;
    }

    public void setCmt1(String cmt1) {
        this.cmt1 = cmt1;
    }

    public String getCmt2() {
        return cmt2;
    }

    public void setCmt2(String cmt2) {
        this.cmt2 = cmt2;
    }

    public String getCmt3() {
        return cmt3;
    }

    public void setCmt3(String cmt3) {
        this.cmt3 = cmt3;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedPrgNm() {
        return createdPrgNm;
    }

    public void setCreatedPrgNm(String createdPrgNm) {
        this.createdPrgNm = createdPrgNm;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedPrgNm() {
        return updatedPrgNm;
    }

    public void setUpdatedPrgNm(String updatedPrgNm) {
        this.updatedPrgNm = updatedPrgNm;
    }

    public String getModifyCount() {
        return modifyCount;
    }

    public void setModifyCount(String modifyCount) {
        this.modifyCount = modifyCount;
    }

    public YotakukinShiharaiRirekiDto getYotakukinShiharaiRirekiDto() {
        return yotakukinShiharaiRirekiDto;
    }

    public void setYotakukinShiharaiRirekiDto(YotakukinShiharaiRirekiDto yotakukinShiharaiRirekiDto) {
        this.yotakukinShiharaiRirekiDto = yotakukinShiharaiRirekiDto;
    }

    public String getHonTaisyokuCd() {
        return honTaisyokuCd;
    }

    public void setHonTaisyokuCd(String honTaisyokuCd) {
        this.honTaisyokuCd = honTaisyokuCd;
    }

    public String getHaiTaisyokuCd() {
        return haiTaisyokuCd;
    }

    public void setHaiTaisyokuCd(String haiTaisyokuCd) {
        this.haiTaisyokuCd = haiTaisyokuCd;
    }

    public String getShoriTuki() {
        return shoriTuki;
    }

    public void setShoriTuki(String shoriTuki) {
        this.shoriTuki = shoriTuki;
    }
}
