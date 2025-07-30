package jp.co.technopro.talon.logic.common;

import org.junit.jupiter.api.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class NashornExecuteEventLogicTest {

    @Test
    void testExecuteEventLogicViaNashorn() throws Exception {
        // === Nashorn 準備 ===
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        // === TALON モック（最低限）===
        engine.put("TALON", new Object() {
            public Map<String, Object> getUserInfoMap() {
                return Map.of("FUNC_ID", "TK_SHINKI_05");
            }

            public Map<String, Object> getConditionData() {
                return new HashMap<>();
            }

            public Map<String, Object> getTargetData() {
                return Map.of("TK_NO", "10425", "SHORI_TUKI", "202507");
            }

            public String getButtonName() { return "BTN1"; }
            public String getEvent() { return "YOTAKU_YOTEI"; }
            public boolean isInsert() { return true; }
            public boolean isUpdate() { return false; }
            public boolean isDelete() { return false; }

            public Map<String, Object> getUserSessions(String key) {
                return new HashMap<>();
            }

            public Object getLogger() {
                return new Object() {
                    public void writeError(String msg) {
                        System.err.println("Logger Error: " + msg);
                    }
                };
            }

            public Object getDbConfig() {
                return new Object() {
                    public Object getDataSource() {
                        return new Object() {
                            public Connection getConnection() {
                                return mock(Connection.class); // DB接続はMock
                            }
                        };
                    }
                };
            }
        });

        // === JavaScriptの読み込み（callEventLogicByIdが定義されたファイル）===
        engine.eval(new FileReader("src/main/resources/scripts/event-caller.js"));

        // === 呼び出し ===
        Invocable invocable = (Invocable) engine;
        invocable.invokeFunction("callEventLogicById", "TK_SHIKI_05");
    }
}
