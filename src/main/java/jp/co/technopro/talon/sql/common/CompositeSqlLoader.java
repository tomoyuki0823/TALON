package jp.co.technopro.talon.sql.common;

/**
 * 2つのSQLローダーを組み合わせ、会社別SQLを優先的に取得する合成ローダー。
 */
public class CompositeSqlLoader extends SqlLoader {

    private final SqlLoader commonLoader;
    private final SqlLoader overrideLoader;

    public CompositeSqlLoader(String commonPath, String overridePath) {
        super(null); // 基底クラスの初期化不要
        this.commonLoader = new SqlLoader(commonPath);
        this.overrideLoader = new SqlLoader(overridePath);
    }

    @Override
    public String get(String id) {
        return overrideLoader.contains(id) ? overrideLoader.get(id) : commonLoader.get(id);
    }

    @Override
    public boolean contains(String id) {
        return overrideLoader.contains(id) || commonLoader.contains(id);
    }
}
