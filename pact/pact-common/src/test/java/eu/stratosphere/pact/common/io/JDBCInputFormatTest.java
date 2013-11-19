/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
* http://www.apache.org/licenses/LICENSE-2.0
 * 
* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.stratosphere.pact.common.io;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactDouble;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

public class JDBCInputFormatTest {

    JDBCInputFormat jdbcInputFormat;
    Configuration config;
    static Connection conn;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.out.println("Before class");
            prepareDerbyDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void prepareDerbyDatabase() throws ClassNotFoundException {
        String dbURL = "jdbc:derby:memory:ebookshop;create=true;user=me;password=mine";
        createConnection(dbURL);
    }

    /*
     Loads JDBC derby driver ; creates(if necessary) and populates database.
     */
    private static void createConnection(String dbURL) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            conn = DriverManager.getConnection(dbURL);
            createTable();
            insertDataToSQLTables();
            conn.close();
        } catch (ClassNotFoundException except) {
            except.printStackTrace();
        } catch (SQLException except) {
            except.printStackTrace();
        }
    }

    private static void createTable() throws SQLException {
        StringBuilder sqlQueryBuilder = new StringBuilder("CREATE TABLE books (");
        sqlQueryBuilder.append("id INT NOT NULL DEFAULT 0,");
        sqlQueryBuilder.append("title VARCHAR(50) DEFAULT NULL,");
        sqlQueryBuilder.append("author VARCHAR(50) DEFAULT NULL,");
        sqlQueryBuilder.append("price FLOAT DEFAULT NULL,");
        sqlQueryBuilder.append("qty INT DEFAULT NULL,");
        sqlQueryBuilder.append("PRIMARY KEY (id))");

        Statement stat = conn.createStatement();
        stat.executeUpdate(sqlQueryBuilder.toString());
        stat.close();

        sqlQueryBuilder = new StringBuilder("CREATE TABLE bookscontent (");
        sqlQueryBuilder.append("id INT NOT NULL DEFAULT 0,");
        sqlQueryBuilder.append("title VARCHAR(50) DEFAULT NULL,");
        sqlQueryBuilder.append("content BLOB(10K) DEFAULT NULL,");
        sqlQueryBuilder.append("PRIMARY KEY (id))");

        stat = conn.createStatement();
        stat.executeUpdate(sqlQueryBuilder.toString());
        stat.close();
    }

    private static void insertDataToSQLTables() throws SQLException {
        StringBuilder sqlQueryBuilder = new StringBuilder("INSERT INTO books (id, title, author, price, qty) VALUES ");
        sqlQueryBuilder.append("(1001, 'Java for dummies', 'Tan Ah Teck', 11.11, 11),");
        sqlQueryBuilder.append("(1002, 'More Java for dummies', 'Tan Ah Teck', 22.22, 22),");
        sqlQueryBuilder.append("(1003, 'More Java for more dummies', 'Mohammad Ali', 33.33, 33),");
        sqlQueryBuilder.append("(1004, 'A Cup of Java', 'Kumar', 44.44, 44),");
        sqlQueryBuilder.append("(1005, 'A Teaspoon of Java', 'Kevin Jones', 55.55, 55)");

        Statement stat = conn.createStatement();
        stat.execute(sqlQueryBuilder.toString());
        stat.close();

        sqlQueryBuilder = new StringBuilder("INSERT INTO bookscontent (id, title, content) VALUES ");
        sqlQueryBuilder.append("(1001, 'Java for dummies', CAST(X'7f454c4602' AS BLOB)),");
        sqlQueryBuilder.append("(1002, 'More Java for dummies', CAST(X'7f454c4602' AS BLOB)),");
        sqlQueryBuilder.append("(1003, 'More Java for more dummies', CAST(X'7f454c4602' AS BLOB)),");
        sqlQueryBuilder.append("(1004, 'A Cup of Java', CAST(X'7f454c4602' AS BLOB)),");
        sqlQueryBuilder.append("(1005, 'A Teaspoon of Java', CAST(X'7f454c4602' AS BLOB))");

        stat = conn.createStatement();
        stat.execute(sqlQueryBuilder.toString());
        stat.close();
    }

    @Before
    public void setUp() {
        configureEmbeddedDerbyConfig();
    }

    @After
    public void tearDown() {
        jdbcInputFormat = null;
    }

    public void configureEmbeddedDerbyConfig() {
        config = new Configuration();
        config.setString("type", "derby");
        config.setString("name", "ebookshop");
        config.setString("username", "me");
        config.setString("password", "mine");
        config.setString("derbydbpath", "memory:ebookshop");
        jdbcInputFormat = new JDBCInputFormat(config, "select * from books");
        jdbcInputFormat.configure(null);
    }

    private void configureForBooksContentTable() {
        jdbcInputFormat = new JDBCInputFormat(config, "select * from bookscontent");
        jdbcInputFormat.configure(null);
    }

    @Test
    public void testJDBCInputFormatSettingPactRecordNormally() throws IOException {
        PactRecord buffRecord;
        List<PactRecord> records = new ArrayList<PactRecord>();
        while (!jdbcInputFormat.reachedEnd()) {
            buffRecord = new PactRecord();
            jdbcInputFormat.nextRecord(buffRecord);
            records.add(buffRecord);
        }

        Assert.assertEquals(5, records.size());

        for (PactRecord prec : records) {
            Assert.assertNotNull(prec);
            Assert.assertEquals(5, prec.getNumFields());
            Assert.assertEquals("Field 0 should be int", PactInteger.class, prec.getField(0, PactInteger.class).getClass());
            Assert.assertEquals("Field 1 should be String", PactString.class, prec.getField(1, PactString.class).getClass());
            Assert.assertEquals("Field 2 should be String", PactString.class, prec.getField(2, PactString.class).getClass());
            Assert.assertEquals("Field 3 should be float", PactDouble.class, prec.getField(3, PactDouble.class).getClass());
            Assert.assertEquals("Field 4 should be int", PactInteger.class, prec.getField(4, PactInteger.class).getClass());
        }
    }

    @Test
    public void testUnsupportedSQLType() throws IOException {
        configureForBooksContentTable();

        PactRecord buffRecord;
        boolean setRecordsSuccessfully = true;
        List<PactRecord> records = new ArrayList<PactRecord>();
        while (!jdbcInputFormat.reachedEnd()) {
            buffRecord = new PactRecord();
            setRecordsSuccessfully = jdbcInputFormat.nextRecord(buffRecord) && setRecordsSuccessfully;
            if (setRecordsSuccessfully) {
                records.add(buffRecord);
            }
        }

        Assert.assertEquals(0, records.size());
        Assert.assertFalse(setRecordsSuccessfully);
    }
    
    @Test
    public void testsetClassForDBTypeApacheDerby() {
        assertTrue(jdbcInputFormat.setClassForDBType("derby"));
    }

    @Test
    public void testsetClassForDBTypeMySQL() {
        assertTrue(jdbcInputFormat.setClassForDBType("mysql"));
    }

    @Test
    public void testsetClassForDBTypePostgres() {
        assertTrue(jdbcInputFormat.setClassForDBType("postgresql"));
    }

    @Test
    public void testsetClassForDBTypeMariaDB() {
        assertTrue(jdbcInputFormat.setClassForDBType("mariadb"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testsetClassForNonAvailableDBType() {
        jdbcInputFormat.setClassForDBType("oracle");
    }
}
