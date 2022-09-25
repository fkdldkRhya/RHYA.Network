package kro.kr.rhya_network.utaite_player;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class UtaitePlayerTicketManager {
	public void ticketApplication(String uuid) throws ClassNotFoundException, SQLException {
		DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("INSERT INTO utaite_licenses(uuid) VALUE (?)");
		databaseConnection.getPreparedStatement().setString(1, uuid);
		databaseConnection.executeUpdate();
		databaseConnection.allClose();
	}
	
	
	public String ticketApplicationState(String uuid) throws ClassNotFoundException, SQLException {
		DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT * FROM utaite_licenses WHERE uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, uuid);
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			String date = databaseConnection.getResultSet().getString("date");
			databaseConnection.allClose();
			
			return date;
		}
		
		return null;
	}
	

	public boolean isAccessCheck(String user_uuid) throws SQLException, ParseException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT user_access_var, user_access_date from utaite_user_info where user_uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, user_uuid);
		databaseConnection.setResultSet();
		
		boolean isUse = true;
		
		if (databaseConnection.getResultSet().next()) {
			int checker = databaseConnection.getResultSet().getInt("user_access_var");
			String date = databaseConnection.getResultSet().getString("user_access_date");
			
			// 날자 및 변수 확인
			if (checker == 1) {
				if (!date.equals("[null]")) {
					if (date.equals("[unlimited]")) {
						// 사용 허가
						isUse = true;
					}else {
						// 현재 날자
						Date nowDate = new Date();		
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date getdate = simpleDateFormat.parse(date);
						// 날자 확인
						if (getdate.compareTo(nowDate) >= 0) {
							// 사용 허가
							isUse = true;
						}else {
							// 이용 차단
							isUse = false;
						}
					}
				}else {
					// 이용 차단
					isUse = false;
				}
			}else {
				// 이용 차단
				isUse = false;
			}
		}else {
			isUse = false;
		}
		
		databaseConnection.allClose();
		
		return isUse;
	}

	
	public void setAccessUser(String uuid) throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT user_access_var, user_access_date from utaite_user_info where user_uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, uuid);
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			databaseConnection.closeResultSet();
			databaseConnection.closePreparedStatement();
			databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_access_var = 1 WHERE user_uuid = ?;");
			databaseConnection.getPreparedStatement().setString(1, uuid);
			databaseConnection.executeUpdate();
			databaseConnection.allClose();
		}else {
			databaseConnection.closeResultSet();
			databaseConnection.closePreparedStatement();
			databaseConnection.setPreparedStatement("INSERT INTO utaite_user_info VALUE (?, ?, ?, ?, ?)");
			databaseConnection.getPreparedStatement().setString(1, uuid);
			databaseConnection.getPreparedStatement().setString(2, "{}");
			databaseConnection.getPreparedStatement().setString(3, "{\"list\":[]}");
			databaseConnection.getPreparedStatement().setInt(4, 1);
			databaseConnection.getPreparedStatement().setString(5, "[unlimited]");
			databaseConnection.executeUpdate();
			databaseConnection.allClose();
		}
	}
}
