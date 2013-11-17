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

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.testutils.TestConfigUtils;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactString;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class JDBCInputFormatTest {

        JDBCInputFormat jdbcInputFormat;
        Configuration config;

        @BeforeClass
        public static void setUpClass() {

        }

        @AfterClass
        public static void tearDownClass() {
        }

        @Before
        public void setUp() {
                testConfigure_dummy();
        }

        @After
        public void tearDown() {
        }

        public void testConfigure_dummy() {
                config = new Configuration();
                config.setString("type", "mysql");
                config.setString("host", "127.0.0.1");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "1111");
                jdbcInputFormat = new JDBCInputFormat(config, "select * from books;");
        }

        @Test
        public void test_data_retrieve_mysql() throws IOException {
                jdbcInputFormat = new JDBCInputFormat(config, "select * from books;");
                PactRecord r = new PactRecord();

                assertTrue(jdbcInputFormat.nextRecord(r));
                assertEquals("Java for dummies", r.getField(1, PactString.class).getValue());
        }

        @Test
        public void test_data_reachedend_mysql() throws IOException {
                jdbcInputFormat = new JDBCInputFormat(config, "select * from books;");
                PactRecord r = new PactRecord();
                while(jdbcInputFormat.nextRecord(r)){}
                assertTrue(jdbcInputFormat.reachedEnd());
        }

        @Test
        public void test_reached_end_mysql() throws IOException {
                jdbcInputFormat = new JDBCInputFormat(config, "select * from books;");
                assertFalse(jdbcInputFormat.reachedEnd());
        }

        @Test
        public void testsetClassForDBType_derby() {
                assertTrue(jdbcInputFormat.setClassForDBType("derby"));
        }

        @Test
        public void testsetClassForDBType_mysql() {
                assertTrue(jdbcInputFormat.setClassForDBType("mysql"));
        }

        @Test
        public void testsetClassForDBType_postgres() {
                assertTrue(jdbcInputFormat.setClassForDBType("postgresql"));
        }

        @Test
        public void testsetClassForDBType_mariadb() {
                assertTrue(jdbcInputFormat.setClassForDBType("mariadb"));
        }
}