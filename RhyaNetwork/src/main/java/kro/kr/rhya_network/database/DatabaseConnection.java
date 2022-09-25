package kro.kr.rhya_network.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	public Connection connection = null;
	
	public void Connection(String driver, String url, String id, String password) throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		connection = DriverManager.getConnection(url, id, password);
	}
	
	public Connection GetConnection() {
		return connection;
	}
	
	public void Close() throws SQLException {
		connection.close();
	}
}
