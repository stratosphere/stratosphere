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
import eu.stratosphere.pact.common.type.PactRecord;
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
        }

        @After
        public void tearDown() {
        }

        @Test
        public void testsetClassForDBType_mysql() {
                config = new Configuration();
                config.setString("type", "mysql");
                config.setString("host", "localhost");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "");
                jdbcInputFormat = new JDBCInputFormat(config, "");
                assertTrue(jdbcInputFormat.setClassForDBType("mysql"));
        }

        @Test
        public void testsetClassForDBType_postgres() {
                config = new Configuration();
                config.setString("type", "postgresql");
                config.setString("host", "localhost");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "");
                jdbcInputFormat = new JDBCInputFormat(config, "");
                assertTrue(jdbcInputFormat.setClassForDBType("postgresql"));
        }

        @Test
        public void testsetClassForDBType_mariadb() {
                config = new Configuration();
                config.setString("type", "mariadb");
                config.setString("host", "localhost");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "");
                jdbcInputFormat = new JDBCInputFormat(config, "");
                assertTrue(jdbcInputFormat.setClassForDBType("mariadb"));
        }

        @Test
        public void testsetClassForDBType_oracle() {
                config = new Configuration();
                config.setString("type", "oracle");
                config.setString("host", "localhost");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "");
                jdbcInputFormat = new JDBCInputFormat(config, "");
                assertTrue(jdbcInputFormat.setClassForDBType("oracle"));
        }

        /**
         * Test of reachedEnd method, of class JDBCInputFormat.
         */
        @Test
        public void testReachedEnd() throws Exception {
        }

        /**
         * Test of nextRecord method, of class JDBCInputFormat.
         */
        @Test
        public void testNextRecord() throws Exception {

        }

}
