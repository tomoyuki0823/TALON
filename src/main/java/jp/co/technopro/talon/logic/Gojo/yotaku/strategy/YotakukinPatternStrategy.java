package jp.co.technopro.talon.logic.Gojo.yotaku.strategy;

import jp.co.technopro.talon.logic.Gojo.yotaku.YotakukinContext;

/**
 * 預託金戦略のインタフェース。
 */
public interface YotakukinPatternStrategy {
    void apply(java.util.Map<String, Object> map, YotakukinContext ctx);
}
