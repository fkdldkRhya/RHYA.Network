package kro.kr.rhya_network.security;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kro.kr.rhya_network.email.EmailSendDATA;
import kro.kr.rhya_network.email.SendEmail;
import kro.kr.rhya_network.util.IPLocationGet;
import kro.kr.rhya_network.util.LoginChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;

public class IPAccessChecker {
	public final String DB_RESULT_SUCCESS = "success";
	public final String DB_RESULT_INSERT_CHECK_EMAIL = "insert_check_email";
	public final String DB_RESULT_NO_TASK_CHECK_EMAIL = "no_task_check_email";
	public final String DB_RESULT_EXCEPTION = "exception";
	
	
	
	public String isAccessIP(HttpServletRequest request, HttpServletResponse response, String user_id, String user_pw, String user_uuid, String ip) {
		try {
			DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();	
			connection.init();
			connection.connection();
			
			connection.setPreparedStatement("SELECT * FROM rhya_network_info;");
			connection.setResultSet();
			if (connection.getResultSet().next()) {
				if (connection.getResultSet().getInt("ip_checker") == 1) {
					return DB_RESULT_SUCCESS;	
				}
			}
			connection.closeResultSet();
			connection.closePreparedStatement();
			
			connection.setPreparedStatement("SELECT * FROM new_ip_login_block WHERE user_uuid = ? AND ip = ?;");
			connection.getPreparedStatement().setString(1, user_uuid);
			connection.getPreparedStatement().setString(2, ip);
			connection.setResultSet();
			
			if (connection.getResultSet().next()) {
				if (connection.getResultSet().getInt("email") == 1) {
					connection.allClose();
					
					
					
					return DB_RESULT_SUCCESS;
				}else {
					String time = connection.getResultSet().getString("date");
					String key = connection.getResultSet().getString("email_auth");
					String[] result = LoginChecker.IsLoginUser(user_id, user_pw, response, true);
					if (result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
						EmailSendDATA.IPAccessAllow accessAllow = new EmailSendDATA.IPAccessAllow();
						SendEmail sendEmail = new SendEmail();
						sendEmail.Send(
								sendEmail.GetProperties(), 
								accessAllow.Html(result[3], accessAllow.Url(request, user_uuid, key), time, ip, new IPLocationGet().GetLocateByIp(ip)),
								accessAllow.Title(result[3]),
								result[5]);
						
						
						
						return DB_RESULT_NO_TASK_CHECK_EMAIL;
					}else {
						
						
						
						return DB_RESULT_EXCEPTION;
					}
				}
			}else {
				connection.closeResultSet();
				connection.closePreparedStatement();
				connection.setPreparedStatement("INSERT INTO new_ip_login_block (user_uuid, ip) VALUE (?, ?);");
				connection.getPreparedStatement().setString(1, user_uuid);
				connection.getPreparedStatement().setString(2, ip);
				connection.executeUpdate();
				connection.allClose();
				
				
				
				return isAccessIP(request, response, user_id, user_pw, user_uuid, ip);
			}
		}catch (Exception e) {
			// 오류 발생
			e.printStackTrace();
		
			
			
			return DB_RESULT_EXCEPTION;
		}
	}

	

	public void isAccessIPForSet(String user_uuid, String ip) throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();	
		connection.init();
		connection.connection();
		connection.setPreparedStatement("SELECT * FROM new_ip_login_block WHERE user_uuid = ? AND ip = ?;");
		connection.getPreparedStatement().setString(1, user_uuid);
		connection.getPreparedStatement().setString(2, ip);
		connection.setResultSet();
		
		if (connection.getResultSet().next()) {
			if (connection.getResultSet().getInt("email") != 1) {
				connection.closeResultSet();
				connection.closePreparedStatement();
				connection.setPreparedStatement("UPDATE new_ip_login_block SET email = ? WHERE user_uuid = ? AND ip = ?;");
				connection.getPreparedStatement().setInt(1, 1);
				connection.getPreparedStatement().setString(2, user_uuid);
				connection.getPreparedStatement().setString(3, ip);
				connection.executeUpdate();
			}
		}else {
			connection.closeResultSet();
			connection.closePreparedStatement();
			connection.setPreparedStatement("INSERT INTO new_ip_login_block (user_uuid, ip, email) VALUE (?, ?, 1);");
			connection.getPreparedStatement().setString(1, user_uuid);
			connection.getPreparedStatement().setString(2, ip);
			connection.executeUpdate();
		}
		
		connection.allClose();
	}
	
	
	
	public void isAccessIPForSetNeedKey(String user_uuid, String email_auth) throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();	
		connection.init();
		connection.connection();
		connection.setPreparedStatement("SELECT * FROM new_ip_login_block WHERE user_uuid = ? AND email_auth = ?;");
		connection.getPreparedStatement().setString(1, user_uuid);
		connection.getPreparedStatement().setString(2, email_auth);
		connection.setResultSet();
		
		if (connection.getResultSet().next()) {
			if (connection.getResultSet().getInt("email") != 1) {
				connection.closeResultSet();
				connection.closePreparedStatement();
				connection.setPreparedStatement("UPDATE new_ip_login_block SET email = ? WHERE user_uuid = ? AND email_auth = ?;");
				connection.getPreparedStatement().setInt(1, 1);
				connection.getPreparedStatement().setString(2, user_uuid);
				connection.getPreparedStatement().setString(3, email_auth);
				connection.executeUpdate();
			}
		}
		
		connection.allClose();
	}
}
