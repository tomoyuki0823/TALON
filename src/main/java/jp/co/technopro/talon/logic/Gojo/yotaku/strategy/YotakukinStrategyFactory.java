package jp.co.technopro.talon.logic.Gojo.yotaku.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 与託金パターンコードに対応する戦略クラスを管理するファクトリクラス。
 * 各 ptanCd（"1"～"10"）に対応する Strategy 実装を事前にマップ登録しておき、
 * 実行時に適切な戦略インスタンスを返す。
 */
public class YotakukinStrategyFactory {

    private static final Map<String, YotakukinPatternStrategy> strategyMap = new HashMap<>();

    static {
        strategyMap.put("1", new Pattern1Strategy());
        strategyMap.put("2", new Pattern2Strategy());
        strategyMap.put("3", new Pattern3Strategy());
        strategyMap.put("4", new Pattern4Strategy());
        strategyMap.put("5", new Pattern5Strategy());
        strategyMap.put("6", new Pattern6Strategy());
        strategyMap.put("7", new Pattern7Strategy());
        strategyMap.put("8", new Pattern8Strategy());
        strategyMap.put("9", new Pattern9Strategy());
        strategyMap.put("10", new Pattern10Strategy());
    }

    /**
     * 指定されたパターンコードに対応する戦略インスタンスを取得します。
     *
     * @param ptnCd パターンコード（例: "1", "2", ...）
     * @return 対応する戦略インスタンス（存在しない場合は空の Optional）
     */
    public static Optional<YotakukinPatternStrategy> get(String ptnCd) {
        return Optional.ofNullable(strategyMap.get(ptnCd));
    }
}
