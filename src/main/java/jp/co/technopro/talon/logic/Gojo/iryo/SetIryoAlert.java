package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.Gojo.GojoDbFlagUtil;
import jp.co.technopro.talon.util.Gojo.GojoMembershipCheckUtil;
import jp.co.technopro.talon.util.Gojo.GojoSoftFlagService;
import jp.co.technopro.talon.util.Gojo.GojoYearMonthCheckUtil;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringUtil;

import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.FLG_ON;
import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.TK_DVS_JGY_KBN_2;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.*;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_COM_M_BANK;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.setTkMemberDto;
import static jp.co.technopro.talon.util.common.DbUtil.getCount;

public class SetIryoAlert extends GojoAbstractLogicBase {
    @Override
    protected EventResultDto executeLogic() {
        TkMemberDto memberDto = setTkMemberDto(conn, getTkNo(), paramDto);

        return setIryoAlert(memberDto);
    }

    private EventResultDto setIryoAlert(TkMemberDto memberDto) {

        setRyoyoNengetuSoft();
        setJgyChk(memberDto);
        setRyoDtChk(memberDto);
        setGinko(memberDto);
        return EventResultDto.ok();
    }

    /**
     * 処理月（syoriTuki）と療養年月（ryoyoNengetu）の月差チェックを行い、
     * しきい値（38か月）以上ならDBにフラグを立てる「ソフトエラー」実装。
     * <p>
     * ・例外は投げず、常に正常系の EventResult を返す。<br>
     * ・入力が未設定/形式不正（yyyyMM 以外）はフラグ対象にしない（スルー）。
     * ・トランザクションは呼び出し側ポリシー（外側）に従う。
     *
     * @return 常に正常（OK）結果
     */
    /**
     * 処理月（syoriTuki）と療養年月（ryoyoNengetu）の月差チェックを行い、
     * しきい値（38か月）以上ならDBにフラグを立てる「ソフトエラー」実装。
     * <p>
     * ・例外は投げず、常に正常系の EventResult を返す。<br>
     * ・入力が未設定/形式不正（yyyyMM 以外）はフラグ対象にしない（スルー）。<br>
     * ・トランザクションは呼び出し側ポリシー（外側）に従う。
     *
     * @return 常に正常（OK）結果
     */
    private EventResultDto setRyoyoNengetuSoft() {

        Map<String, Object> map = paramDto.getTargetData();
        String syoriTuki    = SafeMapAccessUtil.getString(map, MAP_KEY_SHORI_TUKI);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);

        // TRUE=問題なし(フラグ不要) / FALSE=問題あり(フラグ要)
        boolean ok = GojoYearMonthCheckUtil.isDiffLessThanMonths(syoriTuki, ryoyoNengetu, 38);
        if (ok) {
            return EventResultDto.ok();
        }

        // WHERE必須キー（誤更新防止）
        String tk_no = SafeMapAccessUtil.getString(map, MAP_KEY_TK_NO);
        String zoku  = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);
        if (StringUtil.isNullOrEmpty(tk_no) ||
                StringUtil.isNullOrEmpty(zoku)  ||
                StringUtil.isNullOrEmpty(syoriTuki) ||
                StringUtil.isNullOrEmpty(ryoyoNengetu)) {
            return EventResultDto.ok();
        }

        // WHEREは順序固定で
        Map<String, Object> where = new java.util.LinkedHashMap<>();
        where.put(MAP_KEY_TK_NO, tk_no);
        where.put(MAP_KEY_ZOKU, zoku);
        where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
        where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

        // ログなし・例外外出しなし（静かに更新）
        GojoSoftFlagService.updateFlagSilently(
                conn,
                TABLE_TK_IRYO,
                "RYOYO_YM_JIKO_FLG",
                FLG_ON,
                where
        );
        return EventResultDto.ok();
    }


    /**
     * 事業区分に応じて会員・非会員フラグ（KAIIN_HIKAIIN_FLG）を「静かに」更新します。
     * <p>
     * - TK_DVS_JGY_KBN_2 の場合のみ、TABLE_TK_IRYO の該当レコードにフラグ値「1」を更新。<br>
     * - 例外は外へ出さず、画面処理は常に正常継続。<br>
     * - トランザクションの commit/rollback は呼び出し側で管理。
     * </p>
     *
     * @param memberDto 事業区分など会員情報DTO
     * @return 常に {@code EventResultDto.ok()}
     */
    private EventResultDto setJgyChk(TkMemberDto memberDto) {
        if (!TK_DVS_JGY_KBN_2.equals(memberDto.getJgyKbn())) {
            return EventResultDto.ok();
        }
        Map<String, Object> map = paramDto.getTargetData();
        String syoriTuki    = SafeMapAccessUtil.getString(map, MAP_KEY_SHORI_TUKI);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);
        String tk_no        = SafeMapAccessUtil.getString(map, MAP_KEY_TK_NO);
        String zoku         = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);

        if (StringUtil.isNullOrEmpty(tk_no) || StringUtil.isNullOrEmpty(zoku) ||
                StringUtil.isNullOrEmpty(syoriTuki) || StringUtil.isNullOrEmpty(ryoyoNengetu)) {
            return EventResultDto.ok();
        }

        Map<String, Object> where = new java.util.LinkedHashMap<>();
        where.put(MAP_KEY_TK_NO, tk_no);
        where.put(MAP_KEY_ZOKU, zoku);
        where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
        where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

        GojoSoftFlagService.updateFlagSilently(
                conn, TABLE_TK_IRYO, "KAIIN_HIKAIIN_FLG", FLG_ON, where
        );
        return EventResultDto.ok();
    }

    /**
     * 療養年月が在籍期間外かをチェックし、該当時にフラグを静かに更新する（画面は常に正常継続）。
     * <p>
     * ルール：
     * <ul>
     *   <li>加入年月の前月以前 → {@code KANYU_MAE_FLG = FLG_ON}</li>
     *   <li>退会年月の翌月以降 → {@code TAIKAIGO_FLG = FLG_ON}</li>
     *   <li>入力不正（yyyyMM不正や和暦不正など）はフラグ対象にせずスルー</li>
     *   <li>トランザクションは呼び出し側で管理（本メソッドでは commit/rollback しない）</li>
     * </ul>
     * ログは出力しません（TALONログとの二重回避）。
     *
     * @param memberDto 会員情報DTO（加入・退会日の和暦文字列を保持）
     * @return 常に {@link EventResultDto#ok()}
     */
    private EventResultDto setRyoDtChk(TkMemberDto memberDto) {

        Map<String, Object> map = paramDto.getTargetData();
        String zoku = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);

        String taikai_dt = null;
        String kanyu_dt = null;

        switch (zoku) {
            case "0": // 本人
                taikai_dt = memberDto.getHonTaikaiSeinengapi();
                kanyu_dt = memberDto.getHonKanyuSeinengapi();
                break;
            case "1": // 配偶者
                taikai_dt = memberDto.getHaiTaikaiSeinengapi();
                kanyu_dt = memberDto.getHaiKanyubi();
                break;
            default:
                // その他続柄は判定スキップ
                return EventResultDto.ok();
        }

        String syoriTuki = SafeMapAccessUtil.getString(map, MAP_KEY_SHORI_TUKI);
        String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);

        GojoMembershipCheckUtil.Result res =
                GojoMembershipCheckUtil.judgeRyoyoVsKanyuTaikai(ryoyoNengetu, kanyu_dt, taikai_dt);

        // 入力不正は安全にスルー（フラグ更新なし）
        if (res == GojoMembershipCheckUtil.Result.INPUT_INVALID) {
            return EventResultDto.ok();
        }

        // WHERE必須キー（誤更新防止）
        String tkNo = memberDto.getTkNo();
        if (StringUtil.isNullOrEmpty(tkNo) || StringUtil.isNullOrEmpty(zoku) || StringUtil.isNullOrEmpty(syoriTuki) || StringUtil.isNullOrEmpty(ryoyoNengetu)) {
            return EventResultDto.ok();
        }

        // WHEREは順序を固定しておくとデバッグが楽
        Map<String, Object> where = new java.util.LinkedHashMap<>();
        where.put(MAP_KEY_TK_NO, tkNo);
        where.put(MAP_KEY_ZOKU, zoku);
        where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
        where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

        if (res == GojoMembershipCheckUtil.Result.BEFORE_KANYU_PREV) {
            GojoSoftFlagService.updateFlagSilently(
                    conn, TABLE_TK_IRYO, "KANYU_MAE_FLG", FLG_ON, where
            );
        } else if (res == GojoMembershipCheckUtil.Result.AFTER_TAIKAI_NEXT) {
            GojoSoftFlagService.updateFlagSilently(
                    conn, TABLE_TK_IRYO, "TAIKAIGO_FLG", FLG_ON, where
            );
        }
        return EventResultDto.ok();
    }

    /**
     * 銀行／支店コードのマスタ存在チェック（ソフトエラー）。
     * <p>
     * ・COM_M_BANK（{@code TABLE_COM_M_BANK}）に {@code (BANK_CD, SHITEN_CD)} が存在しない場合のみ
     *   申請側（{@code TABLE_TK_IRYO}）にフラグを静かに立てる。<br>
     * ・入力が未設定／空文字の場合はスルー（フラグ更新なし）。<br>
     * ・例外は握りつぶし、画面処理は常に正常継続（commit/rollback は呼び出し側で管理）。<br>
     * ・TALON のログと重複させないため、ここではログ出力しない。
     * </p>
     *
     * フラグ列名の例：{@code GINKO_MST_NG_FLG}（未一致時に "1" をセット）
     *
     * @param memberDto 会員情報DTO（銀行コード・支店コードを保持）
     * @return 常に {@link EventResultDto#ok()}
     */
    private EventResultDto setGinko(TkMemberDto memberDto) {

        // 入力取得
        final String ginkouCd = memberDto.getGinkouCd();
        final String shitenCd = memberDto.getShitenCd();

        // 未入力は安全にスルー（フラグ更新なし）
        if (StringUtil.isNullOrEmpty(ginkouCd) || StringUtil.isNullOrEmpty(shitenCd)) {
            return EventResultDto.ok();
        }

        try {
            // マスタ存在件数
            int cnt = getCount(
                    conn,
                    TABLE_COM_M_BANK,
                    Map.of(MAP_KEY_BANK_CD, ginkouCd, MAP_KEY_SHITEN_CD, shitenCd),
                    paramDto.getCompanyCode()
            );

            // 見つからなければ “静かに” フラグON（列名は環境に合わせて変更してください）
            if (cnt == 0) {
                Map<String, Object> map = paramDto.getTargetData();
                String syoriTuki    = SafeMapAccessUtil.getString(map, MAP_KEY_SHORI_TUKI);
                String ryoyoNengetu = SafeMapAccessUtil.getString(map, MAP_KEY_RYOYO_NENGETU);
                String zoku         = SafeMapAccessUtil.getString(map, MAP_KEY_ZOKU);

                // WHERE必須キー（誤更新防止）
                String tkNo = memberDto.getTkNo();
                if (!StringUtil.isNullOrEmpty(tkNo)
                        && !StringUtil.isNullOrEmpty(zoku)
                        && !StringUtil.isNullOrEmpty(syoriTuki)
                        && !StringUtil.isNullOrEmpty(ryoyoNengetu)) {

                    Map<String, Object> where = new java.util.LinkedHashMap<>();
                    where.put(MAP_KEY_TK_NO, tkNo);
                    where.put(MAP_KEY_ZOKU, zoku);
                    where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
                    where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

                    // ★フラグ列名はプロジェクト規約に合わせて変更（例：GINKO_MASTER_FUSEIGO_FLG）
                    GojoSoftFlagService.updateFlagSilently(
                            conn, TABLE_TK_IRYO, "GINKO_MASTER_FUSEIGO_FLG", FLG_ON, where
                    );
                }
            }

        } catch (Exception ignore) {
            // 失敗しても処理は継続（TALONログと二重化しないため、ここでは何もしない）
        }

        return EventResultDto.ok();
    }

}
