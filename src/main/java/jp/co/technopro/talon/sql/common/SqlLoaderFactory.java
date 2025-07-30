package jp.co.technopro.talon.sql.common;

import jp.co.technopro.talon.sql.common.CompositeSqlLoader;

import java.util.Locale;

/**
 * SQL定義ファイルのローダーを生成・提供するファクトリクラス。
 */
public class SqlLoaderFactory {

    private static final String COMMON_SQL_PATH = "sql/common/common.xml";

    /**
     * 共通SQL定義のみを返すローダー。
     *
     * @return SqlLoader（sql/common/common.xml）
     */
    public static SqlLoader commonLoader() {
        return new SqlLoader(COMMON_SQL_PATH);
    }

    /**
     * カンパニーコードに応じたSQLローダーを返します。
     * 指定が無効または不明な場合は共通ローダーのみ返します。
     *
     * @param companyCd カンパニーコード（例: "GOJO", "YAZAKI"）
     * @return 対応する SqlLoader（共通SQL + 会社別SQL の合成）
     */
    public static SqlLoader forCompanyWithCommon(String companyCd) {
        if (companyCd == null || companyCd.isBlank()) {
            return commonLoader();
        }

        String lowerCd = companyCd.toLowerCase(Locale.ROOT);
        String companySqlPath = String.format("sql/%s/common.xml", lowerCd);

        return new CompositeSqlLoader(COMMON_SQL_PATH, companySqlPath);
    }
}
