package jp.co.technopro.talon.logic.Gojo;

import jp.co.technopro.talon.consts.Gojo.GojoMessagesConst;
import jp.co.technopro.talon.dto.common.EventResultDto;
import jp.co.technopro.talon.dto.common.TalonParamDto;
import jp.co.technopro.talon.logic.Gojo.choju.ChojuInitLogic;
import jp.co.technopro.talon.util.common.DbUtil;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import java.sql.*;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class IwaiKinTest {

    private HashMap<String, Object> createShoriTuki(String value){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("SHORI_TUKI", value);
        return map;
    }

    @Test
    public void emptyTest(){
        ChojuInitLogic logic = new ChojuInitLogic();
        Connection conn = mock(Connection.class);
        TalonParamDto paramDto = mock(TalonParamDto.class);

        //値詰め
        when(paramDto.getCompanyCode()).thenReturn("GOJO");
        when(paramDto.getConditionData()).thenReturn(Maps.newHashMap());
        EventResultDto result = logic.run(conn, paramDto);
        assertEquals(GojoMessagesConst.MSG_ERROR_EMPTY, result.getMessage());
        assertTrue(!result.isSuccess());
    }

    @Test
    public void nullTest(){
        ChojuInitLogic logic = new ChojuInitLogic();
        Connection conn = mock(Connection.class);
        TalonParamDto paramDto = mock(TalonParamDto.class);

        //値詰め
        when(paramDto.getCompanyCode()).thenReturn("GOJO");
        when(paramDto.getConditionData()).thenReturn(createShoriTuki(null));
        EventResultDto result = logic.run(conn, paramDto);
        assertEquals(GojoMessagesConst.MSG_ERROR_EMPTY, result.getMessage());
        assertTrue(!result.isSuccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {"323", "356", "09722", "srwekih", "eiaotrt"})
    public void invalidTest(String value){
        ChojuInitLogic logic = new ChojuInitLogic();
        Connection conn = mock(Connection.class);
        TalonParamDto paramDto = mock(TalonParamDto.class);

        //値詰め
        when(paramDto.getCompanyCode()).thenReturn("GOJO");
        when(paramDto.getConditionData()).thenReturn(createShoriTuki(value));
        EventResultDto result = logic.run(conn, paramDto);
        assertTrue(result.getMessage().endsWith(GojoMessagesConst.MSG_ERROR_INVALID));
        assertTrue(!result.isSuccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {"199009", "201203"})
    public void validNoCountTest(String value){
        ChojuInitLogic logic = new ChojuInitLogic();

        //通常モック
        Connection conn = mock(Connection.class);
        TalonParamDto paramDto = mock(TalonParamDto.class);

        //静的モック
        MockedStatic<DbUtil> db = mockStatic(DbUtil.class);

        //値詰め
        when(paramDto.getCompanyCode()).thenReturn("GOJO");
        when(paramDto.getConditionData()).thenReturn(createShoriTuki(value));
        db.when(() -> {DbUtil.getCount(eq(conn), anyString(), any(), any());}).thenReturn(1);


        EventResultDto result = logic.run(conn, paramDto);
        db.close();
        assertEquals(GojoMessagesConst.MSG_OK_ALREADY, result.getMessage());
        assertTrue(result.isSuccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {"199009", "201203", "202409"})
    public void validTest(String value){

        ChojuInitLogic logic = new ChojuInitLogic();
        TalonParamDto paramDto = mock(TalonParamDto.class);
        when(paramDto.getCompanyCode()).thenReturn("GOJO");
        when(paramDto.getConditionData()).thenReturn(createShoriTuki(value));

        try(Connection conn = DriverManager.getConnection("jdbc:sqlserver://172.31.6.72\\SQLEXPRESS:1433;database=TALON;encrypt=false", "sa", "knight")){
            EventResultDto result = logic.run(conn, paramDto);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}
