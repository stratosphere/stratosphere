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
	
	@Override
	public void configure(Configuration parameters) {
		String dbType = parameters.getString("type", "mysql");
		String host = parameters.getString("host", "localhost");
		Integer port = parameters.getInteger("port", 3306);
		String username = parameters.getString("username", "");
		String password = parameters.getString("password", "");
		
		if(setClassForDBType(dbType)) {
			String url = String.format("");
		}
	}
	
	private boolean setClassForDBType(String dbType) {
		boolean hasSetClass = false;
		
		try {
			if(dbType.equals(MYSQL_TYPE)) {
				Class.forName("com.mysql.jdbc.Driver");
				hasSetClass = true;
			}
			else if(dbType.equals(POSTGRES_TYPE)) {
				Class.forName("org.postgresql.Driver");
				hasSetClass = true;
			}
			else if(dbType.equals(MARIADB_TYPE)) {
				Class.forName("com.mysql.jdbc.Driver");
				hasSetClass = true;
			}
			else if(dbType.equals(ORACLE_TYPE)) {
				Class.forName("oracle.jdbc.OracleDriver");
				hasSetClass = true;
			}
			else {
				LOG.info("Database type is not supported yet:\t" + dbType);
				hasSetClass = false;
			}
		}
		catch(ClassNotFoundException cnfe) {
			LOG.error("JDBC-Class not found:\t" + cnfe.getLocalizedMessage());
			hasSetClass = false;
		}
		
		return hasSetClass;
	}
	
	private boolean prepareConnection(String dbURL, String username, String password) {
		try {
			dbConn = DriverManager.getConnection(dbURL, username, password);
			return true;
		} 
		catch (SQLException e) {
			LOG.error("Couldn't create db-connection:\t" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean reachedEnd() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean nextRecord(PactRecord record) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
