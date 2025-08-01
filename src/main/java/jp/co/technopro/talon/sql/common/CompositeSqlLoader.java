package jp.co.technopro.talon.sql.common;

/**
 * CompositeSqlLoader は SqlLoader を継承するが、自身の sqlMap は使用せず、
 * {@code commonLoader} および {@code overrideLoader} によってSQLを取得する。
 *
 * 親クラスの初期化には {@code commonPath} を与えるが、これは型整合性維持のためである。
 */

public class CompositeSqlLoader extends SqlLoader {

    private final SqlLoader commonLoader;
    private final SqlLoader overrideLoader;

    public CompositeSqlLoader(String commonPath, String overridePath) {
        // super(null) をやめて、意図が伝わるよう "composite-mode" などのダミー値にする
        super(commonPath);
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

