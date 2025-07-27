package jp.co.technopro.talon.logic;

import java.sql.Connection;
import java.util.Map;

public class SampleLogic {

    public void testMethod(Connection conn, Map<String, Object> paramMap) {
        System.out.println("SampleLogic.testMethod() 実行 → paramMap = " + paramMap);
        paramMap.put("RESULT", "実行成功");
    }
}
