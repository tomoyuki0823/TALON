package jp.co.technopro.talon.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbUtilTest {

    @Test
    void testDetectDialect_sqlserver() throws Exception {
        Connection mockConn = mock(Connection.class);
        DatabaseMetaData mockMeta = mock(DatabaseMetaData.class);
        when(mockConn.getMetaData()).thenReturn(mockMeta);
        when(mockMeta.getDatabaseProductName()).thenReturn("Microsoft SQL Server");

        DbUtil.Dialect dialect = DbUtil.detectDialect(mockConn);
        assertEquals(DbUtil.Dialect.SQLSERVER, dialect);
    }

    @Test
    void testDetectDialect_unsupported() throws Exception {
        Connection mockConn = mock(Connection.class);
        DatabaseMetaData mockMeta = mock(DatabaseMetaData.class);
        when(mockConn.getMetaData()).thenReturn(mockMeta);
        when(mockMeta.getDatabaseProductName()).thenReturn("DB2");

        assertThrows(UnsupportedOperationException.class, () -> {
            DbUtil.detectDialect(mockConn);
        });
    }

    @Test
    void testSelect_resultMapping() throws Exception {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMeta = mock(ResultSetMetaData.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.getMetaData()).thenReturn(mockMeta);
        when(mockMeta.getColumnCount()).thenReturn(2);
        when(mockMeta.getColumnLabel(1)).thenReturn("ID");
        when(mockMeta.getColumnLabel(2)).thenReturn("NAME");

        when(mockRs.next()).thenReturn(true).thenReturn(false);
        when(mockRs.getObject(1)).thenReturn(1);
        when(mockRs.getObject(2)).thenReturn("Test");

        List<Map<String, Object>> result = DbUtil.select(mockConn, "SELECT * FROM TEST");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("ID"));
        assertEquals("Test", result.get(0).get("NAME"));
    }
}
