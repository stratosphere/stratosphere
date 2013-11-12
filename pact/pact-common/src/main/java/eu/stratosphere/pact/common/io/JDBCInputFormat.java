package eu.stratosphere.pact.common.io;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactBoolean;
import eu.stratosphere.pact.common.type.base.PactDouble;
import eu.stratosphere.pact.common.type.base.PactFloat;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactLong;
import eu.stratosphere.pact.common.type.base.PactNull;
import eu.stratosphere.pact.common.type.base.PactShort;
import eu.stratosphere.pact.common.type.base.PactString;

public class JDBCInputFormat extends GenericInputFormat {

        private enum DBTypes {

                MYSQL,
                POSTGRESQL,
                MARIADB,
                ORACLE,
                DERBY
        }

        public static class NotTransformableSQLFieldException extends Exception {

                public NotTransformableSQLFieldException(String message) {
                        super(message);
                }
        }

        private static final Log LOG = LogFactory.getLog(JDBCInputFormat.class);

        private Connection dbConn;
        private Statement statement;
        private ResultSet resultSet;
        private String query;

        public JDBCInputFormat(Configuration parameters, String query) {

                this.query = query;
                configure(parameters);
        }

        @Override
        public void configure(Configuration parameters) {
                String dbTypeStr = parameters.getString("type", "mysql");
                String host = parameters.getString("host", "localhost");
                Integer port = parameters.getInteger("port", 3306);
                String dbName = parameters.getString("name", "");
                String username = parameters.getString("username", "");
                String password = parameters.getString("password", "");
                String derbyDBPath = parameters.getString("derbydbpath", System.getProperty("user.dir" + "/test/resources/derby/db;create=true;"));

                DBTypes dbType = getDBType(dbTypeStr);

                if (setClassForDBType(dbType)) {
                        String url = "";

                        switch (dbType) {
                                case MYSQL:
                                        url = "jdbc:mysql://%s:%i/%s".format(host, port, dbName);
                                        break;

                                case POSTGRESQL:
                                        url = "jdbc:postgresql://%s:%i/%s".format(host, port, dbName);
                                        break;

                                case MARIADB:
                                        url = "jdbc:mysql://%s:%i/%s".format(host, port, dbName);
                                        break;

                                case ORACLE:
                                        //needs drivertype, asumsed >thin< for now
                                        url = "jdbc:oracle:thin:@%s:%i:%s".format(host, port, dbName);
                                        break;

                                case DERBY:
                                        url = String.format("jdbc:derby://%s", derbyDBPath);
                                        break;
                        }
                        if (prepareConnection(url, username, password)) {
                                try {
                                        statement = dbConn.createStatement();
                                        resultSet = statement.executeQuery(this.query);

                                } catch (SQLException e) {
                                        LOG.error("Couldn't execute query:\t!" + e.getMessage());
                                }
                        }
                }
        }

        private DBTypes getDBType(String dbTypeStr) {
                if (dbTypeStr != null) {
                        dbTypeStr = dbTypeStr.toUpperCase().trim();
                        DBTypes dbType = DBTypes.valueOf(dbTypeStr);
                        return dbType;
                } else {
                        return null;
                }
        }

        public boolean setClassForDBType(String dbTypeIdentifier) {
                DBTypes dbType = getDBType(dbTypeIdentifier);
                return setClassForDBType(dbType);
        }

        public boolean setClassForDBType(DBTypes dbType) {
                boolean hasSetClass = false;

                if (dbType == null) {
                        return false;
                }

                try {
                        switch (dbType) {
                                case MYSQL:
                                        Class.forName("com.mysql.jdbc.Driver");
                                        hasSetClass = true;
                                        break;

                                case POSTGRESQL:
                                        Class.forName("org.postgresql.Driver");
                                        hasSetClass = true;
                                        break;

                                case ORACLE:
                                        Class.forName("oracle.jdbc.OracleDriver");
                                        hasSetClass = true;
                                        break;

                                case DERBY:
                                        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                                        hasSetClass = true;
                                        break;

                                default:
                                        LOG.info("Database type is not supported yet:\t" + dbType);
                                        hasSetClass = false;
                        }
                } catch (ClassNotFoundException cnfe) {
                        LOG.error("JDBC-Class not found:\t" + cnfe.getLocalizedMessage());
                        hasSetClass = false;
                }

                return hasSetClass;
        }

        private boolean prepareConnection(String dbURL, String username, String password) {
                try {
                        dbConn = DriverManager.getConnection(dbURL, username, password);
                        return true;
                } catch (SQLException e) {
                        LOG.error("Couldn't create db-connection:\t" + e.getMessage());
                        return false;
                }
        }

        @Override
        public boolean reachedEnd() throws IOException {
                try {
                        return resultSet.isAfterLast();
                } catch (SQLException e) {
                        LOG.error("Couldn't evaluate reacedEnd():\t" + e.getMessage());
                }
                return false;
        }

        @Override
        public boolean nextRecord(PactRecord record) throws IOException {
                try {
                        resultSet.next();
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int column_count = rsmd.getColumnCount();

                        for (int pos = 0; pos < column_count; pos++) {
                                int type = rsmd.getColumnType(pos);
                                retrieveTypeAndFillRecord(pos, type, record);
                        }
                        return true;
                } catch (SQLException e) {
                        LOG.error("Couldn't read data:\t" + e.getMessage());
                } catch (NotTransformableSQLFieldException e) {
                        LOG.error("Couldn't read data because of unknown column sql-type:\t" + e.getMessage());
                }
                return false;
        }

        private void retrieveTypeAndFillRecord(int pos, int type, PactRecord record) throws SQLException, NotTransformableSQLFieldException {
                switch (type) {
                        case java.sql.Types.NULL:
                                record.setField(pos, new PactNull());
                                break;
                        case java.sql.Types.BOOLEAN:
                                record.setField(pos, new PactBoolean(resultSet.getBoolean(pos)));
                                break;
                        case java.sql.Types.BIT:
                                record.setField(pos, new PactBoolean(resultSet.getBoolean(pos)));
                                break;
                        case java.sql.Types.CHAR:
                                record.setField(pos, new PactString(resultSet.getString(pos)));
                                break;
                        case java.sql.Types.NCHAR:
                                record.setField(pos, new PactString(resultSet.getString(pos)));
                                break;
                        case java.sql.Types.VARCHAR:
                                record.setField(pos, new PactString(resultSet.getString(pos)));
                                break;
                        case java.sql.Types.LONGVARCHAR:
                                record.setField(pos, new PactString(resultSet.getString(pos)));
                                break;
                        case java.sql.Types.LONGNVARCHAR:
                                record.setField(pos, new PactString(resultSet.getString(pos)));
                                break;
                        case java.sql.Types.TINYINT:
                                record.setField(pos, new PactShort(resultSet.getShort(pos)));
                                break;
                        case java.sql.Types.SMALLINT:
                                record.setField(pos, new PactShort(resultSet.getShort(pos)));
                                break;
                        case java.sql.Types.BIGINT:
                                record.setField(pos, new PactLong(resultSet.getLong(pos)));
                                break;
                        case java.sql.Types.INTEGER:
                                record.setField(pos, new PactInteger(resultSet.getInt(pos)));
                                break;
                        case java.sql.Types.FLOAT:
                                record.setField(pos, new PactDouble(resultSet.getDouble(pos)));
                                break;
                        case java.sql.Types.REAL:
                                record.setField(pos, new PactFloat(resultSet.getFloat(pos)));
                                break;
                        case java.sql.Types.DOUBLE:
                                record.setField(pos, new PactDouble(resultSet.getDouble(pos)));
                                break;
                        case java.sql.Types.DECIMAL:
                                record.setField(pos, new PactDouble(resultSet.getBigDecimal(pos).doubleValue()));
                                break;
                        case java.sql.Types.NUMERIC:
                                record.setField(pos, new PactDouble(resultSet.getBigDecimal(pos).doubleValue()));
                                break;
                        case java.sql.Types.DATE:
                                record.setField(pos, new PactString(resultSet.getDate(pos).toString()));
                                break;
                        case java.sql.Types.TIME:
//				record.setField(pos, new PactString(resultSet.getTime(pos).toString()));
                                record.setField(pos, new PactLong(resultSet.getTime(pos).getTime()));
                                break;
                        case java.sql.Types.TIMESTAMP:
                                record.setField(pos, new PactString(resultSet.getTimestamp(pos).toString()));
                                break;
                        case java.sql.Types.SQLXML:
                                record.setField(pos, new PactString(resultSet.getSQLXML(pos).toString()));
                        default:
                                throw new NotTransformableSQLFieldException("Unknown sql-type [" + type + "]on column [" + pos + "]");

//			case java.sql.Types.BINARY:
//			case java.sql.Types.VARBINARY:
//			case java.sql.Types.LONGVARBINARY:
//			case java.sql.Types.ARRAY:
//			case java.sql.Types.JAVA_OBJECT:
//			case java.sql.Types.BLOB:
//			case java.sql.Types.CLOB:
//			case java.sql.Types.NCLOB:
//			case java.sql.Types.DATALINK:
//			case java.sql.Types.DISTINCT:
//			case java.sql.Types.OTHER:
//			case java.sql.Types.REF:
//			case java.sql.Types.ROWID:
//			case java.sql.Types.STRUCT:
                }
        }

}
