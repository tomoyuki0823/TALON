package jp.co.technopro.talon.logic.Gojo.iryo;

import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.gojo.TkMemberDto;
import jp.co.technopro.talon.logic.Gojo.common.GojoAbstractLogicBase;
import jp.co.technopro.talon.util.Gojo.GojoSoftFlagService;
import jp.co.technopro.talon.util.common.SafeMapAccessUtil;
import jp.co.technopro.talon.util.common.StringUtil;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static jp.co.technopro.talon.consts.Gojo.GojoCodeValuesConst.FLG_ON;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_BANK_CD;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_RYOYO_NENGETU;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHITEN_CD;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_SHORI_TUKI;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_TK_NO;
import static jp.co.technopro.talon.consts.Gojo.GojoMapKeyConst.MAP_KEY_ZOKU;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_COM_M_BANK;
import static jp.co.technopro.talon.consts.Gojo.GojoTableNameConst.TABLE_TK_IRYO;
import static jp.co.technopro.talon.util.Gojo.GojoDbUtil.setTkMemberDto;
import static jp.co.technopro.talon.util.common.DbUtil.getCount;

/**
 * 銀行マスタ一致チェック（ソフトエラー）ロジック。
 *
 * <p>申請データ（TK_IRYO）に紐づく会員の「銀行コード・支店コード」が
 * マスタ（COM_M_BANK）に存在するかを確認し、<b>存在しない場合のみ</b>
 * 申請側へ不整合フラグを“静かに”立てます。画面処理は常に正常継続します。</p>
 *
 * <h3>仕様</h3>
 * <ul>
 *   <li>入力未設定／空白はスキップ（フラグ更新なし）</li>
 *   <li>マスタに (BANK_CD, SHITEN_CD) が存在しない場合のみ、
 *       TK_IRYO に {@code GINKO_MASTER_FUSEIGO_FLG = "1"} を更新</li>
 *   <li>WHERE 必須: {@code TK_NO, ZOKU, SHORI_TUKI, RYOYO_NENGETU}（いずれか欠落時は更新しない）</li>
 *   <li>トランザクションは呼び出し側で管理</li>
 *   <li>DB例外（{@link SQLException}）は握り、処理は継続</li>
 * </ul>
 *
 * @since 1.0
 */
public class IryoSetGinko extends GojoAbstractLogicBase {

    /** 銀行マスタ不整合フラグ列（プロジェクト規約に合わせて変更可） */
    private static final String COL_GINKO_MST_NG_FLG = "GINKO_MASTER_FUSEIGO_FLG";

    /**
     * 業務ロジック本体。
     * <p>必要時のみ会員情報を取得し、銀行／支店コードのマスタ存在チェックを実施します。
     * 会員情報取得に失敗（{@code null}）しても処理は正常継続（静かにスキップ）。</p>
     * @return 常に {@link EventResultDto#ok()}
     */
    @Override
    protected EventResultDto executeLogic() {
        final TkMemberDto memberDto = setTkMemberDto(conn, getTkNo(), paramDto);
        if (memberDto == null) {
            return EventResultDto.ok();
        }
        return setGinko(memberDto);
    }

    /**
     * 銀行／支店コードのマスタ存在チェック（ソフトエラー）。
     * <p>入力（銀行コード・支店コード）を取り出し、未入力ならスキップ。
     * 会社コード未設定時も安全側でスキップします。マスタ不一致時に、申請（TK_IRYO）の
     * 該当レコードへ「静かに」不整合フラグを立てます。</p>
     *
     * @param memberDto 会員情報DTO（銀行コード・支店コード・TK_NO を保持）
     * @return 常に {@link EventResultDto#ok()}
     */
    private EventResultDto setGinko(final TkMemberDto memberDto) {

        logInfoMethodStart();

        if (paramDto.isTlnIsDelete()) return EventResultDto.ok();
        if (paramDto.isTlnIsUpdate()) return EventResultDto.ok();
        // 入力取得（null→""へ正規化し trim）
        final String ginkouCd = trimOrEmpty(memberDto.getGinkouCd());
        final String shitenCd = trimOrEmpty(memberDto.getShitenCd());

        // 未入力は安全にスルー（フラグ更新なし）
        if (StringUtil.isNullOrEmpty(ginkouCd) || StringUtil.isNullOrEmpty(shitenCd)) {
            return EventResultDto.ok();
        }

        // companyCode 未設定はスルー（静かに継続）
        final String companyCd = trimOrEmpty(paramDto.getCompanyCode());
        if (StringUtil.isNullOrEmpty(companyCd)) {
            return EventResultDto.ok();
        }

        // マスタ存在件数を確認（複合インデックス推奨）
        final int cnt = getCount(
                conn,
                TABLE_COM_M_BANK,
                Map.of(MAP_KEY_BANK_CD, ginkouCd, MAP_KEY_SHITEN_CD, shitenCd), // null 不許可（上で正規化済み）
                companyCd
        );

        // 見つからなければ “静かに” フラグ ON
        if (cnt == 0) {
            final Map<String, Object> target = getTargetData(); // null-safe
            final String syoriTuki    = trimOrEmpty(SafeMapAccessUtil.getString(target, MAP_KEY_SHORI_TUKI));
            final String ryoyoNengetu = trimOrEmpty(SafeMapAccessUtil.getString(target, MAP_KEY_RYOYO_NENGETU));
            final String zoku         = trimOrEmpty(SafeMapAccessUtil.getString(target, MAP_KEY_ZOKU));
            final String tkNo         = trimOrEmpty(memberDto.getTkNo());

            // WHERE 必須が欠ける場合は誤更新防止のため更新しない
            if (!StringUtil.isNullOrEmpty(tkNo)
                    && !StringUtil.isNullOrEmpty(zoku)
                    && !StringUtil.isNullOrEmpty(syoriTuki)
                    && !StringUtil.isNullOrEmpty(ryoyoNengetu)) {

                final Map<String, Object> where = new LinkedHashMap<>();
                where.put(MAP_KEY_TK_NO, tkNo);
                where.put(MAP_KEY_ZOKU, zoku);
                where.put(MAP_KEY_SHORI_TUKI, syoriTuki);
                where.put(MAP_KEY_RYOYO_NENGETU, ryoyoNengetu);

                GojoSoftFlagService.updateFlagSilently(
                        conn, TABLE_TK_IRYO, COL_GINKO_MST_NG_FLG, FLG_ON, where
                );
            }
        }

        return EventResultDto.ok();
    }

    /** null を空文字に正規化し、前後空白を除去するローカルヘルパー */
    private static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
