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

    private String dbTypeStr;
    private String host;
    private Integer port;
    private String dbName;
    private String username;
    private String password;
    private String derbyDBPath;

    private String dbURL = null;

    private boolean need_configure = false;

    public JDBCInputFormat() {
        this.need_configure = true;
    }

    public JDBCInputFormat(String dbURL, String query) {
        this.query = query;
        this.dbURL = dbURL;
    }

    public JDBCInputFormat(Configuration parameters, String query) {
        this.query = query;
        this.dbTypeStr = parameters.getString("type", "mysql");
        this.host = parameters.getString("host", "localhost");
        this.port = parameters.getInteger("port", 3306);
        this.dbName = parameters.getString("name", "");
        this.username = parameters.getString("username", "");
        this.password = parameters.getString("password", "");
        this.derbyDBPath = parameters.getString("derbydbpath", System.getProperty("user.dir" + "/tmp/derby/db;create=true;"));
    }

    private DBTypes getDBType(String dbTypeStr) {
        dbTypeStr = dbTypeStr.toUpperCase().trim();
        DBTypes dbType = DBTypes.valueOf(dbTypeStr);
        return dbType;
    }

    private boolean setClassForDBType(DBTypes dbType) {

        if (dbType == null) {
            return false;
        }

        try {
            switch (dbType) {
                case MYSQL:
                    Class.forName("com.mysql.jdbc.Driver");
                    return true;

                case POSTGRESQL:
                    Class.forName("org.postgresql.Driver");
                    return true;

                case MARIADB:
                    Class.forName("com.mysql.jdbc.Driver");
                    return true;

                case DERBY:
                    Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                    return true;

                default:
                    LOG.info("Database type is not supported yet:\t" + dbType);
                    return false;
            }
        } catch (ClassNotFoundException cnfe) {
            LOG.error("JDBC-Class not found:\t" + cnfe.getLocalizedMessage());
            return false;
        }
    }

    private boolean prepareConnection(String dbURL) {
        try {
            dbConn = DriverManager.getConnection(dbURL);
            return true;

        } catch (SQLException e) {
            LOG.error("Couldn't create db-connection:\t" + e.getMessage());
            return false;
        }
    }

    private boolean prepareConnection(DBTypes dbType, String username, String password) {
        switch (dbType) {
            case MYSQL:
                dbURL = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
                break;
            case POSTGRESQL:
                dbURL = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                break;
            case MARIADB:
                dbURL = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
                break;
            case DERBY:
                dbURL = String.format("jdbc:derby://%s", derbyDBPath);
                break;
        }
        try {
            dbConn = DriverManager.getConnection(dbURL, username, password);
            return true;
        } catch (SQLException e) {
            LOG.error("Couldn't create db-connection:\t" + e.getMessage());
            return false;
        }
    }

    private void retrieveTypeAndFillRecord(int pos, int type, PactRecord record) throws SQLException, NotTransformableSQLFieldException {
        switch (type) {
            case java.sql.Types.NULL:
                record.setField(pos, new PactNull());
                break;
            case java.sql.Types.BOOLEAN:
                record.setField(pos, new PactBoolean(resultSet.getBoolean(pos + 1)));
                break;
            case java.sql.Types.BIT:
                record.setField(pos, new PactBoolean(resultSet.getBoolean(pos + 1)));
                break;
            case java.sql.Types.CHAR:
                record.setField(pos, new PactString(resultSet.getString(pos + 1)));
                break;
            case java.sql.Types.NCHAR:
                record.setField(pos, new PactString(resultSet.getString(pos + 1)));
                break;
            case java.sql.Types.VARCHAR:
                record.setField(pos, new PactString(resultSet.getString(pos + 1)));
                break;
            case java.sql.Types.LONGVARCHAR:
                record.setField(pos, new PactString(resultSet.getString(pos + 1)));
                break;
            case java.sql.Types.LONGNVARCHAR:
                record.setField(pos, new PactString(resultSet.getString(pos + 1)));
                break;
            case java.sql.Types.TINYINT:
                record.setField(pos, new PactShort(resultSet.getShort(pos + 1)));
                break;
            case java.sql.Types.SMALLINT:
                record.setField(pos, new PactShort(resultSet.getShort(pos + 1)));
                break;
            case java.sql.Types.BIGINT:
                record.setField(pos, new PactLong(resultSet.getLong(pos + 1)));
                break;
            case java.sql.Types.INTEGER:
                record.setField(pos, new PactInteger(resultSet.getInt(pos + 1)));
                break;
            case java.sql.Types.FLOAT:
                record.setField(pos, new PactDouble(resultSet.getDouble(pos + 1)));
                break;
            case java.sql.Types.REAL:
                record.setField(pos, new PactFloat(resultSet.getFloat(pos + 1)));
                break;
            case java.sql.Types.DOUBLE:
                record.setField(pos, new PactDouble(resultSet.getDouble(pos + 1)));
                break;
            case java.sql.Types.DECIMAL:
                record.setField(pos, new PactDouble(resultSet.getBigDecimal(pos + 1).doubleValue()));
                break;
            case java.sql.Types.NUMERIC:
                record.setField(pos, new PactDouble(resultSet.getBigDecimal(pos + 1).doubleValue()));
                break;
            case java.sql.Types.DATE:
                record.setField(pos, new PactString(resultSet.getDate(pos + 1).toString()));
                break;
            case java.sql.Types.TIME:
                //				record.setField(pos, new PactString(resultSet.getTime(pos).toString()));
                record.setField(pos, new PactLong(resultSet.getTime(pos + 1).getTime()));
                break;
            case java.sql.Types.TIMESTAMP:
                record.setField(pos, new PactString(resultSet.getTimestamp(pos + 1).toString()));
                break;
            case java.sql.Types.SQLXML:
                record.setField(pos, new PactString(resultSet.getSQLXML(pos + 1).toString()));
                break;
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

    @Override
    public void configure(Configuration parameters) {
        if (need_configure) {
            this.dbTypeStr = parameters.getString("type", "mysql");
            this.host = parameters.getString("host", "localhost");
            this.port = parameters.getInteger("port", 3306);
            this.dbName = parameters.getString("name", "");
            this.username = parameters.getString("username", "");
            this.password = parameters.getString("password", "");
            this.query = parameters.getString("query", "");
            this.derbyDBPath = parameters.getString("derbydbpath", System.getProperty("user.dir" + "/test/resources/derby/db;create=true;"));
        }

        DBTypes dbType = getDBType(dbTypeStr);

        if (setClassForDBType(dbType)) {
            if (dbURL == null) {
                if (prepareConnection(dbType, username, password)) {
                    try {
                        statement = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        resultSet = statement.executeQuery(this.query);
                    } catch (SQLException e) {
                        LOG.error("Couldn't execute query:\t!" + e.getMessage());
                    }
                }
            } else {
                if (prepareConnection(dbURL)) {
                    try {
                        statement = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        resultSet = statement.executeQuery(this.query);

                    } catch (SQLException e) {
                        LOG.error("Couldn't execute query:\t!" + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public boolean reachedEnd() throws IOException {
        try {
            if (resultSet.isLast()) {
                statement.close();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Couldn't evaluate reachedEnd():\t" + e.getMessage());
        } catch (NullPointerException e) {
            LOG.error("Couldn't access resultSet:\t" + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean nextRecord(PactRecord record) throws IOException {
        try {
            resultSet.next();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int column_count = rsmd.getColumnCount();
            record.setNumFields(column_count);

            for (int pos = 0; pos < column_count; pos++) {
                int type = rsmd.getColumnType(pos + 1);
                retrieveTypeAndFillRecord(pos, type, record);
            }
            return true;
        } catch (SQLException e) {
            LOG.error("Couldn't read data:\t" + e.getMessage());
        } catch (NotTransformableSQLFieldException e) {
            LOG.error("Couldn't read data because of unknown column sql-type:\t" + e.getMessage());
        } catch (NullPointerException e) {
            LOG.error("Couldn't access resultSet:\t" + e.getMessage());
        }
        return false;
    }

}
