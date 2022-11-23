package kro.kr.rhya_network.utils.online_attendance;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import kro.kr.rhya_network.email.EmailSendDATA;
import kro.kr.rhya_network.email.SendEmail;
import kro.kr.rhya_network.page.PageParameter;
import kro.kr.rhya_network.util.AuthTokenChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;

public class OnlineAttendanceAccessChecker {
	/**
	 * 온라인 출석부 접근 권한 확인
	 * 
	 * @param authToken Auth Token
	 * @return True, False
	 * 
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean isAccessCheck(String authToken) throws ClassNotFoundException, SQLException {
		if (authToken == null) return false;
		
		PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
		AuthTokenChecker authTokenChecker = new AuthTokenChecker();
		String[] result = authTokenChecker.getAuthInfo(authToken);
		// 로그인 확인
		if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
			if (result[2].equals(authTokenParm.SERVICE.get(1))) {
				DatabaseManager.DatabaseConnection db = new DatabaseManager.DatabaseConnection();
				db.init();
				db.connection();
				db.setPreparedStatement("SELECT * FROM online_attendance_account_sync WHERE uuid = ?");
				db.getPreparedStatement().setString(1, result[1]);
				db.setResultSet();

				if (db.getResultSet().next()) {
					int isAccess = db.getResultSet().getInt("isAccess");
					db.allClose();
					
					return isAccess == 1;
				}else {
					db.allClose();
				}
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * 온라인 출석부 계정 연동 인증 함수
	 * 
	 * @param request HttpServletRequest
	 * @param authToken Auth token
	 * @param authCode Sync authorization key
	 * @return
	 * 
	 * 0 : Auth token 값이 Null임
	 * 1 : 계정 연동 코드가 데이터베이스에서 설정되지 않음
	 * 2 : 계정 연동 코드가 일치함
	 * 3 : 계정 연동 코드 일치하지 않음
	 * 4 : 이메일이 이미 전송됨
     * 5 : account_sync_email을 변경하고 메일의 발송에 성공함
	 * 6 : 계정 연동 데이터를 데이터베이스에 삽입하고 메일의 발송에 성공함
	 * 7 : 계정 연동 데이터를 데이터베이스에 삽입하고 메일의 발송에 실패함
	 * 8 : 알 수 없는 오류
	 * 
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws MessagingException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * 
	 */
	public int requestChecker(HttpServletRequest request, String authToken, String authCode) throws ClassNotFoundException, SQLException, FileNotFoundException, MessagingException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		if (authToken == null) return 0;
		
		PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
		AuthTokenChecker authTokenChecker = new AuthTokenChecker();
		String[] result = authTokenChecker.getAuthInfo(authToken);
		// 로그인 확인
		if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
			if (result[2].equals(authTokenParm.SERVICE.get(1))) {
				result = null;
				result = authTokenChecker.getMoreAuthInfo(authToken);
				DatabaseManager.DatabaseConnection db = new DatabaseManager.DatabaseConnection();
				db.init();
				db.connection();
				db.setPreparedStatement("SELECT * FROM online_attendance_account_sync WHERE uuid = ?");
				db.getPreparedStatement().setString(1, result[1]);
				db.setResultSet();
				
				int accountInfo;
				int account_sync_email = -1;
				String teacherUUID = null;
				String accountSyncAuthCode = null;
				
				if (db.getResultSet().next()) {
					int isAccess = db.getResultSet().getInt("isAccess");
					account_sync_email = db.getResultSet().getInt("account_sync_email");
					teacherUUID = db.getResultSet().getString("teacher_uuid");
					accountSyncAuthCode = db.getResultSet().getString("account_sync_code");
					if (accountSyncAuthCode == null) return 1;
					if (isAccess == 1) accountInfo = 2;
					else accountInfo = 1;
				}else {
					accountInfo = 3;
				}

				db.closePreparedStatement();
				db.closeResultSet();

				if (accountInfo != 3) {
					if (authCode != null) {
						if (accountSyncAuthCode.equals(authCode)) {
							db.setPreparedStatement("UPDATE online_attendance_account_sync SET isAccess = 1 WHERE uuid = ?");
							db.getPreparedStatement().setString(1, result[1]);
							db.executeUpdate();
							db.allClose();
							return 2;
						}else {
							return 3;
						}	
					}else {
						if (account_sync_email == 1) {
							db.allClose();
							return 4;
						}

						if (accountSyncAuthCode != null) {
							db.setPreparedStatement("UPDATE online_attendance_account_sync SET account_sync_email = 1 WHERE uuid = ?");
							db.getPreparedStatement().setString(1, result[1]);
							db.executeUpdate();
							db.allClose();
							
							// 이메일 발송
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("[ UUID: ");
							stringBuilder.append(result[1]);
							stringBuilder.append(", ID: ");
							stringBuilder.append(result[3]);
							stringBuilder.append(", NAME: ");
							stringBuilder.append(result[4]);
							stringBuilder.append(" ]");
							String requestAccountInfo = stringBuilder.toString();
							stringBuilder.setLength(0);
							stringBuilder.append("[ UUID: ");
							stringBuilder.append(teacherUUID);
							stringBuilder.append(" ]");
							String teacherAccountInfo = stringBuilder.toString();
							
							EmailSendDATA.AccountSyncRequestForAuthCode emailSendDATA = new EmailSendDATA.AccountSyncRequestForAuthCode();
							SendEmail sendEmail = new SendEmail();
							sendEmail.Send(
									sendEmail.GetProperties(),
									emailSendDATA.Html(
											result[3],
											emailSendDATA.Url(
													request,
													result[1],
													accountSyncAuthCode,
													teacherUUID),
											requestAccountInfo,
											teacherAccountInfo),
									emailSendDATA.Title(result[3]),
									EmailSendDATA.ADMIN_EMAIL);
							
							
							return 5;
						}else {
							db.allClose();
							return 1;
						}
					}
				}else {
					db.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE name = ? AND email_address = ?;");
					db.getPreparedStatement().setString(1, result[4]);
					db.getPreparedStatement().setString(2, result[5]);
					db.setResultSet();
					
					int indedx = 0;
					
					while (db.getResultSet().next()) {
						String tUUID = db.getResultSet().getString("uuid");
						UUID uuid = UUID.randomUUID();
						String aUUID = uuid.toString();
						
						DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();
						connection.init();
						connection.connection();
						connection.setPreparedStatement("INSERT INTO online_attendance_account_sync(uuid,teacher_uuid,account_sync_email,account_sync_code) VALUE ( ?, ?, ?, ? )");
						connection.getPreparedStatement().setString(1, result[1]);
						connection.getPreparedStatement().setString(2, tUUID);
						connection.getPreparedStatement().setInt(3, 1);
						connection.getPreparedStatement().setString(4, aUUID);
						connection.executeUpdate();
						connection.allClose();
						connection = null;
						
						// 이메일 발송
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("[ UUID: ");
						stringBuilder.append(result[1]);
						stringBuilder.append(", ID: ");
						stringBuilder.append(result[3]);
						stringBuilder.append(", NAME: ");
						stringBuilder.append(result[4]);
						stringBuilder.append(" ]");
						String requestAccountInfo = stringBuilder.toString();
						stringBuilder.setLength(0);
						stringBuilder.append("[ UUID: ");
						stringBuilder.append(tUUID);
						stringBuilder.append(" ]");
						String teacherAccountInfo = stringBuilder.toString();
						
						EmailSendDATA.AccountSyncRequestForAuthCode emailSendDATA = new EmailSendDATA.AccountSyncRequestForAuthCode();
						SendEmail sendEmail = new SendEmail();
						sendEmail.Send(
								sendEmail.GetProperties(),
								emailSendDATA.Html(
										result[3],
										emailSendDATA.Url(
												request,
												result[1],
												aUUID,
												tUUID),
										requestAccountInfo,
										teacherAccountInfo),
								emailSendDATA.Title(result[3]),
								EmailSendDATA.ADMIN_EMAIL);
						
						indedx++;
					}

					db.allClose();
					
					if (indedx > 0) {
						return 6;
					}else {
						return 7;	
					}
				}
			}
		}
		
		return 8;
	}
	
	
	
	/**
	 * 
	 * @param authToken
	 * @return
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public OnlineAttendanceTeacherVO getTeacherInfo(String authToken) throws ClassNotFoundException, SQLException {
		if (authToken == null) return null;

		PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
		AuthTokenChecker authTokenChecker = new AuthTokenChecker();
		String[] result = authTokenChecker.getAuthInfo(authToken);
		// 로그인 확인
		if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
			if (result[2].equals(authTokenParm.SERVICE.get(1))) {
				DatabaseManager.DatabaseConnection db = new DatabaseManager.DatabaseConnection();
				db.init();
				db.connection();
				db.setPreparedStatement("SELECT * FROM online_attendance_account_sync WHERE uuid = ?");
				db.getPreparedStatement().setString(1, result[1]);
				db.setResultSet();

				if (db.getResultSet().next()) {
					if (db.getResultSet().getInt("isAccess") == 1) {
						String tUUID = db.getResultSet().getString("teacher_uuid");
						
						db.closeResultSet();
						db.closePreparedStatement();
						db.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE uuid = ?");
						db.getPreparedStatement().setString(1, tUUID);
						db.setResultSet();
						System.out.println("werewr");
						if (db.getResultSet().next()) {
							OnlineAttendanceTeacherVO onlineAttendanceTeacherVO = new OnlineAttendanceTeacherVO(
									db.getResultSet().getString("uuid"),
									db.getResultSet().getString("name"),
									db.getResultSet().getString("name_no_duplication"),
									db.getResultSet().getString("image"),
									db.getResultSet().getString("description"),
									db.getResultSet().getString("department1"),
									db.getResultSet().getString("department2"),
									db.getResultSet().getString("email_address"),
									db.getResultSet().getString("mobile_phone"),
									db.getResultSet().getString("office_phone"),
									db.getResultSet().getString("position"),
									db.getResultSet().getString("subject"),
									db.getResultSet().getInt("school_id"),
									db.getResultSet().getInt("version"));
							
							db.allClose();
							
							return onlineAttendanceTeacherVO;		
						}else {
							db.allClose();
						}
					}else { 
						db.allClose();
					}
					
				}else {
					db.allClose();
				}
			}
		}
		
		return null;
	}
	
}
