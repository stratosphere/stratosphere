package eu.stratosphere.pact.common.io;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.type.PactRecord;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCInputFormat extends GenericInputFormat {

    public static final String MYSQL_TYPE = "mysql";
    public static final String POSTGRES_TYPE = "postgres";
    public static final String MARIADB_TYPE = "mariadb";
    public static final String ORACLE_TYPE = "oracle";

    private static final Log LOG = LogFactory.getLog(JDBCInputFormat.class);

    private Connection dbConn;
    private Statement statement;
    private ResultSet resultSet;
    private String query;

    public JDBCInputFormat(Configuration parameters, String query) {
        configure(parameters);
        this.query = query;
    }

    private enum DBTypes {

        mysql,
        postgresql,
        mariadb,
        oracle
    }

    @Override
    public void configure(Configuration parameters) {
        String dbType = parameters.getString("type", "mysql");
        String host = parameters.getString("host", "localhost");
        Integer port = parameters.getInteger("port", 3306);
        String dbName = parameters.getString("database", "");
        String username = parameters.getString("username", "");
        String password = parameters.getString("password", "");

        if (setClassForDBType(dbType)) {
            String url = "";
            DBTypes type = DBTypes.valueOf(dbType);
            switch (type) {
                case mysql:
                    url = String.format("jdbc:mysql://%s:%i/%s", host, port, dbName);

                case postgresql:
                    url = String.format("jdbc:postgresql://%s:%i/%s", host, port, dbName);

                case mariadb:
                    url = String.format("jdbc:mysql://%s:%i/%s", host, port, dbName);

                case oracle:
                    //needs drivertype, asumsed >thin< for now
                    url = String.format("jdbc:oracle:thin:@%s:%i:%s", host, port, dbName);

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

    private boolean setClassForDBType(String dbType) {
        boolean hasSetClass = false;

        try {
            if (dbType.equals(MYSQL_TYPE)) {
                Class.forName("com.mysql.jdbc.Driver");
                hasSetClass = true;
            } else if (dbType.equals(POSTGRES_TYPE)) {
                Class.forName("org.postgresql.Driver");
                hasSetClass = true;
            } else if (dbType.equals(MARIADB_TYPE)) {
                Class.forName("com.mysql.jdbc.Driver");
                hasSetClass = true;
            } else if (dbType.equals(ORACLE_TYPE)) {
                Class.forName("oracle.jdbc.OracleDriver");
                hasSetClass = true;
            } else {
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
        int currentIndex;
        int lastIndex;

        try {
            currentIndex = resultSet.getRow();
            resultSet.afterLast();
            lastIndex = resultSet.getRow();
            if (lastIndex == currentIndex) {
                return true;
            } else {
                resultSet.absolute(currentIndex);
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Couldn't evaluate reacedEnd():\t" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean nextRecord(PactRecord record) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

}
