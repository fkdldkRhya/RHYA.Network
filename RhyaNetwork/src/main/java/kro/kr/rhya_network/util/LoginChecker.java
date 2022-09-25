package kro.kr.rhya_network.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kro.kr.rhya_network.database.DatabaseConnection;
import kro.kr.rhya_network.database.DatabaseInfo;
import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.page.PageParameter;
import kro.kr.rhya_network.security.RhyaAES;

public class LoginChecker {
	// �α��� ���
	public static final String LOGIN_RESULT_SUCCESS = "success";
	public static final String LOGIN_RESULT_FAIL = "fail";
	public static final String LOGIN_RESULT_NOMESSAGE = "no_message";
	// Session �̸�
	public static final String LOGIN_SESSION_NAME = "_LOGIN_DATA_";
	// ��ū ��ȿ�Ⱓ
	public static final int LOGIN_TOKEN_TIME = 24;
	
	
	// �α��� �Լ�
	public static String[] IsLoginUser(String id, String pw, HttpServletResponse rsp, boolean isNoCreateToken) throws SQLException, ClassNotFoundException {
		// ���� ����
		String user_uuid = "";
		String[] result = null;
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
		sql.append(" = ? AND ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, id);
		stat.setString(2, pw);
		// ���� ����
		rs = stat.executeQuery();
		if (rs.next()) {
			// ���� ���� StringBuilder �ʱ�ȭ
			sql.delete(0,sql.length());
			// ���� ����
			sql.append("SELECT * FROM ");
			sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
			sql.append(" WHERE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
			sql.append(" = ?;");
			// �����ͺ��̽� ����
			stat = cont.GetConnection().prepareStatement(sql.toString());
			stat.setString(1, rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID));
			// ���� ���̵�
			user_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
			// ���� ����
			rs = stat.executeQuery();
		}else {
			// �α��� ����
			result = new String[] { LOGIN_RESULT_FAIL , "���̵� �Ǵ� ��й�ȣ�� �߸��Ǿ����ϴ�." };
		}
		
		// ���� ���
		if (rs.next()) {
			// ���� Ȯ��
			if (rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_BLOCKED) == 0) {
				// ���� Ȱ��ȭ Ȯ��
				if (rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION) != 1) {
					// �α��� ����
					result = new String[] { LOGIN_RESULT_FAIL , "������ Ȱ��ȭ�Ǿ����� �ʽ��ϴ�. ���Ϸ� �߼۵� ���� Ȱ��ȭ ��ũ�� �����ֽñ� �ٶ��ϴ�." };
				}
			}else {
				// �α��� ����
				StringBuilder sb = new StringBuilder();
				sb.append("�ش� ������ �����ڿ� ���� �̿��� ���ѵǾ��ֽ��ϴ�.");
				sb.append("<br>");
				sb.append("����: ");
				sb.append(rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_BLOCKED_REASON));
				
				result = new String[] { LOGIN_RESULT_FAIL , sb.toString() };
				
				sb = null;
			}
		}else {
			// �α��� ����
			result = new String[] { LOGIN_RESULT_FAIL , "���̵� �Ǵ� ��й�ȣ�� �߸��Ǿ����ϴ�." };
		}
		
		// �۾� ��ȯ
		if (result == null) {
			// ����� ����
			String userName = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_NAME);
			String userEmail = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_EMAIL);
			String userBirthday = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_BIRTHDAY);
			String userRegDate = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_REGDATE);
			// ���� ���� StringBuilder �ʱ�ȭ
			sql.delete(0,sql.length());
			String uuid = null;
			// Token ���� ���� Ȯ��
			if (isNoCreateToken) {
				// ������ ����
				uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
				// ������ Ȯ��
				if (uuid == null || uuid.replace(" ", "").equals("")) {
					// ������ ����
					UUID uuid_ = UUID.randomUUID();
					uuid = uuid_.toString();
					// ���� ����
					sql.append("UPDATE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
					sql.append(" SET ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
					sql.append(" = ?, ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
					sql.append(" = ? WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
					sql.append("= ?;");
					// �����ͺ��̽� ����
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, uuid);
					stat.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					stat.setString(3, user_uuid);
				}else {
					// ���� ����
					sql.append("UPDATE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
					sql.append(" SET ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
					sql.append(" = ? WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
					sql.append("= ?;");
					// �����ͺ��̽� ����
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					stat.setString(2, user_uuid);	
				}
			}else {
				// ���� ����
				sql.append("UPDATE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
				sql.append(" SET ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
				sql.append(" = ?, ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
				sql.append(" = ? WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
				sql.append("= ?;");
				// ������ ����
				UUID uuid_ = UUID.randomUUID();
				uuid = uuid_.toString();
				// �����ͺ��̽� ����
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, uuid_.toString());
				stat.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				stat.setString(3, user_uuid);
			}
			// ���� ����
			stat.executeUpdate();
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			
			// Null Ȯ��
			if (rsp != null) {
				// ��Ű ���� Ȯ��
				// ==========================================================================
				// ��Ű ����
			    Cookie c1 = new Cookie("AutoLogin_UserUUID", user_uuid);
			    // ��Ű�� ��� �߰�
			    c1.setPath("/");
			    // ��Ű�� ������ �߰�
			    c1.setComment("UserUUID");
			    // ��Ű ��ȿ�Ⱓ�� ����
			    c1.setMaxAge(60);
				// ��Ű ����
			    Cookie c2 = new Cookie("AutoLogin_TokenUUID", uuid.toString());
			    // ��Ű�� ��� �߰�
			    c2.setPath("/");
			    // ��Ű�� ������ �߰�
			    c2.setComment("UserUUID");
			    // ��Ű ��ȿ�Ⱓ�� ����
			    c2.setMaxAge(60);
			    // ��������� ��Ű�� �߰�
			    rsp.addCookie(c1);
			    rsp.addCookie(c2);
				// ==========================================================================	
			}
			
			// �α��� ����
			result = new String[] { LOGIN_RESULT_SUCCESS , user_uuid, uuid.toString(), id, userName, userEmail, userBirthday, userRegDate, pw };
			
			// ������ ��ȯ
			return result;
		}else {
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			
			// ������ ��ȯ
			return result;
		}
	}
	
	
	// �ڵ� �α���
	public static String[] IsAutoLogin(String TokenKey, String UserUUID, HttpServletResponse rsp, boolean isNoCreateToken) throws ClassNotFoundException, SQLException, ParseException {
		// ���� ����
		String getID = null;
		String getPW = null;
		String getTokenKey = null;
		String getTokenDate = null;
		String[] result = null;
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, UserUUID);
		// ���� ����
		rs = stat.executeQuery();
		// ��� ó��
		if (rs.next()) {
			getID = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
			getPW = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		}
		// ���� ���� StringBuilder �ʱ�ȭ
		sql.delete(0,sql.length());
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, UserUUID);
		// ���� ����
		rs = stat.executeQuery();
		// ��� ó��
		if (rs.next()) {
			getTokenKey = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
			getTokenDate = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
		}
		// Null Ȯ��
		if (getTokenDate != null && getTokenKey != null && getID != null && getPW != null) {
			// ��ū�� Ȯ��
			if (getTokenKey.equals(TokenKey)) {
				// ��ū ���� ���� Ȯ��
				if (DateTimeChecker.isTime_H(getTokenDate, LOGIN_TOKEN_TIME)) {
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ���� �� ��� ��ȯ
					return IsLoginUser(getID, getPW, rsp, isNoCreateToken);
				}else {
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					// ��� ����
					result = new String[] { LOGIN_RESULT_NOMESSAGE };
					
					// ��� ��ȯ
					return result;
				}
			}else {
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				// ��� ����
				result = new String[] { LOGIN_RESULT_FAIL , "��ū�� �Ǵ� ���� ���̵� �߸��Ǿ����ϴ�." };
				
				// ��� ��ȯ
				return result;
			}
		}else {
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			// ��� ����
			result = new String[] { LOGIN_RESULT_FAIL , "��ū�� �Ǵ� ���� ���̵� �߸��Ǿ����ϴ�." };
			
			// ��� ��ȯ
			return result;
		}
	}
	
	
	// JSP �ڵ� �α��� �Լ�
	public static void AutoLoginTask(RhyaLogger rl, HttpSession session, HttpServletRequest req, HttpServletResponse rsp, boolean thisPageLoginOnly, boolean urlNext, String url, boolean isNoCreateToken) throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, SQLException, ParseException, IOException {
		String[] login_session;
		
		// ��Ű ������ Ȯ��
		try {
			String uuid = null;
			String token = null;
			
			Cookie[] cookies = req.getCookies();
			if (cookies != null) {
				for(Cookie cookie:cookies) {
					if (cookie.getName().equals("RhyaAutoLoginCookie_UserUUID")) {
						uuid = cookie.getValue();
					}else if (cookie.getName().equals("RhyaAutoLoginCookie_ToeknUUID")) {
						token = cookie.getValue();
					}
				}
				
				if (uuid != null && token != null) {
					login_session = new String[] { uuid, token };
				}else {
					// �ڵ� �α��� Ȯ��
					login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
				}
			}else {
				// �ڵ� �α��� Ȯ��
				login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
			}
		}catch (Exception e) {
			// TODO: handle exception
			
			// �ڵ� �α��� Ȯ��
			login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
		}

		// Null �� Ȯ��
		if (login_session != null) {
			// ���� ���� ����
			session.removeAttribute(LOGIN_SESSION_NAME);
			// �ڵ� �α���
			String[] auto_login_result = IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), rsp, isNoCreateToken);
			// �ڵ� �α��� ��� Ȯ��
			if (auto_login_result[0].equals(LOGIN_RESULT_SUCCESS)) {
				// ������ ��ȣȭ
				String enc_value_1 = RhyaAES.AES_Encode(auto_login_result[1]);
				String enc_value_2 = RhyaAES.AES_Encode(auto_login_result[2]);
				// ���� ���
				session.setAttribute(LOGIN_SESSION_NAME, new String[] { enc_value_1, enc_value_2 } );
				/*
				// ��Ű ����
				// ==========================================================================
			    Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", enc_value_1);
			    // ��Ű�� ��� �߰�
			    c3.setPath("/");
			    // ��Ű�� ������ �߰�
			    c3.setComment("UserUUID");
			    // ��Ű ��ȿ�Ⱓ�� ����
			    c3.setMaxAge(60 * 60 * 24 * 7);
				// ��Ű ����
			    Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", enc_value_2);
			    // ��Ű�� ��� �߰�
			    c4.setPath("/");
			    // ��Ű�� ������ �߰�
			    c4.setComment("TokenUUID");
			    // ��Ű ��ȿ�Ⱓ�� ����
			    c4.setMaxAge(60 * 60 * 24 * 7);
			    // ��������� ��Ű�� �߰�
			    rsp.addCookie(c3);
			    rsp.addCookie(c4);		
			    */
				// ��Ű ���� ������
				PageParameter.SignIn signinV = new PageParameter.SignIn();
				CookieGenerator cookieGen = new CookieGenerator();
				// ��Ű ����
				cookieGen.createCookie(rsp, rl, GetClientIPAddress.getClientIp(req), signinV.COOKIE_NAME_USER, auto_login_result[1], "/", signinV.COOKIE_NAME_USER, 60);
			    cookieGen.createCookie(rsp, rl, GetClientIPAddress.getClientIp(req), signinV.COOKIE_NAME_TOKEN, auto_login_result[2], "/", signinV.COOKIE_NAME_TOKEN, 60);
				// ==========================================================================
				// �α� �ۼ�
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(GetClientIPAddress.getClientIp(req),"�ڵ� �α��� ���� : USER_UUID = ", auto_login_result[1]));
				// ������ �̵� Ȯ��
				if (urlNext) {
					// ������ �̵�
					rsp.sendRedirect(url);
				}
			}else {
				// �ڵ� �α��� ����
				if (thisPageLoginOnly) {
					// �α� �ۼ�
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(req),"�ڵ� �α��� ���� : RESULT != LOGIN_RESULT_SUCCESS"));
					// �ڵ� �α��� ���� --> �α��� ������ �̵�
					rsp.sendRedirect(JspPageInfo.GetJspPageURL(req, 0));	
				}
			}
		}else {
			// �ڵ� �α��� ����
			if (thisPageLoginOnly) {
				// �α� �ۼ�
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(req),"�ڵ� �α��� ���� : LOGIN_SESSION == null"));
				// �ڵ� �α��� ���� --> �α��� ������ �̵�
				rsp.sendRedirect(JspPageInfo.GetJspPageURL(req, 0));	
			}
		}
	}
}
