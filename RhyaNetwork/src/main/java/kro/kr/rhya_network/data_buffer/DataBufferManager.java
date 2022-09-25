package kro.kr.rhya_network.data_buffer;

import java.sql.SQLException;
import java.util.UUID;

import kro.kr.rhya_network.utils.db.DatabaseManager;

public class DataBufferManager {
	public String createBuffer(String initValue) throws SQLException, ClassNotFoundException {
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();
		connection.init();
		connection.connection();
		connection.setPreparedStatement("INSERT INTO data_buffer(request_code,data) VALUE ( ?,? )");
		connection.getPreparedStatement().setString(1, uuidStr);
		connection.getPreparedStatement().setString(2, initValue);
		connection.executeUpdate();
		connection.allClose();
		
		return uuidStr;
	}
	
	public String addBuffer(String requestCode, int nowIndex, String value) throws ClassNotFoundException, SQLException {
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();
		connection.init();
		connection.connection();
		connection.setPreparedStatement("SELECT * FROM data_buffer WHERE request_code = ?;");
		connection.getPreparedStatement().setString(1, requestCode);
		connection.setResultSet();
		
		String result = null;
		int index = 0;
		
		while (connection.getResultSet().next()) {
			index = connection.getResultSet().getInt("index");
			
			if (index == nowIndex) {
				String data = connection.getResultSet().getString("data");
				StringBuilder sb = new StringBuilder();
				sb.append(data);
				sb.append(value);
				
				result = sb.toString();
			}
		}
		
		connection.closePreparedStatement();
		connection.closeResultSet();
		
		// 변경사항 저장
		if (result != null) {
			index = index + 1;
			connection.setPreparedStatement("UPDATE data_buffer SET data_buffer.data = ?, data_buffer.index = ? WHERE data_buffer.request_code = ?;");
			connection.getPreparedStatement().setString(1, result);
			connection.getPreparedStatement().setInt(2, index);
			connection.getPreparedStatement().setString(3, requestCode);
			connection.executeUpdate();
		}
		
		connection.allClose();
		
		return result;
	}
	
	
	public String getBuffer(String requestCode) throws ClassNotFoundException, SQLException {
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();
		connection.init();
		connection.connection();
		connection.setPreparedStatement("SELECT * FROM data_buffer WHERE request_code = ?;");
		connection.getPreparedStatement().setString(1, requestCode);
		connection.setResultSet();
		
		String result = null;
	
		while (connection.getResultSet().next()) {
			result = connection.getResultSet().getString("data");
		}
		
		connection.allClose();
		
		return result;
	}
}
