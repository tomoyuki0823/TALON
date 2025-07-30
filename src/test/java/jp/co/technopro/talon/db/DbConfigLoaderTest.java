package jp.co.technopro.talon.db;

import jp.co.technopro.talon.db.common.DbConfig;
import jp.co.technopro.talon.db.common.DbConfigLoader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.naming.*;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbConfigLoaderTest {

    @TempDir
    static Path tempDir;

    private static final String SAMPLE_PROPS = String.join("\n",
            "url=jdbc:h2:mem:testdb",
            "user=testuser",
            "password=testpass",
            "driver=org.h2.Driver"
    );

    @Test
    void testLoadFromClasspath() {
        DbConfig config = DbConfigLoader.loadFromClasspath("test-db.properties");
        assertEquals("jdbc:h2:mem:classpath", config.getUrl());
        assertEquals("user", config.getUser());
        assertEquals("pass", config.getPassword());
        assertEquals("org.h2.Driver", config.getDriver());
    }

    @Test
    void testLoadFromFile() throws IOException {
        File propFile = tempDir.resolve("external-db.properties").toFile();
        try (FileWriter fw = new FileWriter(propFile)) {
            fw.write(SAMPLE_PROPS);
        }

        DbConfig config = DbConfigLoader.loadFromFile(propFile.getAbsolutePath());
        assertEquals("jdbc:h2:mem:testdb", config.getUrl());
        assertEquals("testuser", config.getUser());
        assertEquals("testpass", config.getPassword());
        assertEquals("org.h2.Driver", config.getDriver());
    }

    @Test
    void testLoadFromJndi() throws Exception {
        // Mock DataSource
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConn = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConn);

        // Setup JNDI
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
        DummyInitialContextFactory.setDataSource("jdbc/TalonDb", mockDataSource);

        DbConfig config = DbConfigLoader.load("Gojo");
        assertNotNull(config.getDataSource());
        assertSame(mockDataSource, config.getDataSource());
    }

    /**
     * ダミーInitialContextFactoryの定義（JNDI用）
     */
    public static class DummyInitialContextFactory implements InitialContextFactory {
        private static final Hashtable<String, Object> bindings = new Hashtable<>();

        public static void setDataSource(String name, Object obj) {
            bindings.put(name, obj);
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return new Context() {
                @Override public Object lookup(String name) {
                    return bindings.get(name);
                }

                @Override
                public void bind(Name name, Object obj) throws NamingException {

                }

                // 不要なメソッドは未実装
                @Override public void close() {}
                @Override public Object lookup(javax.naming.Name name) { return null; }
                @Override public void bind(String name, Object obj) {}

                @Override
                public void rebind(Name name, Object obj) throws NamingException {

                }

                @Override public void rebind(String name, Object obj) {}

                @Override
                public void unbind(Name name) throws NamingException {

                }

                @Override public void unbind(String name) {}

                @Override
                public void rename(Name oldName, Name newName) throws NamingException {

                }

                @Override public void rename(String oldName, String newName) {}

                @Override
                public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
                    return null;
                }

                @Override public javax.naming.NamingEnumeration<NameClassPair> list(String name) { return null; }

                @Override
                public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
                    return null;
                }

                @Override public javax.naming.NamingEnumeration<Binding> listBindings(String name) { return null; }

                @Override
                public void destroySubcontext(Name name) throws NamingException {

                }

                @Override public void destroySubcontext(String name) {}

                @Override
                public Context createSubcontext(Name name) throws NamingException {
                    return null;
                }

                @Override public Context createSubcontext(String name) { return null; }

                @Override
                public Object lookupLink(Name name) throws NamingException {
                    return null;
                }

                @Override public Object lookupLink(String name) { return null; }

                @Override
                public NameParser getNameParser(Name name) throws NamingException {
                    return null;
                }

                @Override public NameParser getNameParser(String name) { return null; }

                @Override
                public Name composeName(Name name, Name prefix) throws NamingException {
                    return null;
                }

                @Override public String composeName(String name, String prefix) { return null; }
                @Override public Object addToEnvironment(String propName, Object propVal) { return null; }
                @Override public Object removeFromEnvironment(String propName) { return null; }
                @Override public Hashtable<?, ?> getEnvironment() { return null; }
                @Override public String getNameInNamespace() { return null; }
            };
        }
    }
}
