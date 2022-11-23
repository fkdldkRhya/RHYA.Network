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
	// 로그인 결과
	public static final String LOGIN_RESULT_SUCCESS = "success";
	public static final String LOGIN_RESULT_FAIL = "fail";
	public static final String LOGIN_RESULT_NOMESSAGE = "no_message";
	// Session 이름
	public static final String LOGIN_SESSION_NAME = "_LOGIN_DATA_";
	// 토큰 유효기간
	public static final int LOGIN_TOKEN_TIME = 24;
	
	
	// 로그인 함수
	public static String[] IsLoginUser(String id, String pw, HttpServletResponse rsp, boolean isNoCreateToken) throws SQLException, ClassNotFoundException {
		// 변수 선언
		String user_uuid = "";
		String[] result = null;
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
		sql.append(" = ? AND ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		sql.append(" = ?;");
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, id);
		stat.setString(2, pw);
		// 쿼리 실행
		rs = stat.executeQuery();
		if (rs.next()) {
			// 쿼리 생성 StringBuilder 초기화
			sql.delete(0,sql.length());
			// 쿼리 생성
			sql.append("SELECT * FROM ");
			sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
			sql.append(" WHERE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
			sql.append(" = ?;");
			// 데이터베이스 접속
			stat = cont.GetConnection().prepareStatement(sql.toString());
			stat.setString(1, rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID));
			// 계정 아이디
			user_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
			// 쿼리 실행
			rs = stat.executeQuery();
		}else {
			// 로그인 실패
			result = new String[] { LOGIN_RESULT_FAIL , "아이디 또는 비밀번호가 잘못되었습니다." };
		}
		
		// 쿼리 결과
		if (rs.next()) {
			// 차단 확인
			if (rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_BLOCKED) == 0) {
				// 계정 활성화 확인
				if (rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION) != 1) {
					// 로그인 실패
					result = new String[] { LOGIN_RESULT_FAIL , "계정이 활성화되어있지 않습니다. 메일로 발송된 계정 활성화 링크를 눌러주시길 바랍니다." };
				}
			}else {
				// 로그인 실패
				StringBuilder sb = new StringBuilder();
				sb.append("해당 계정은 관리자에 의해 이용이 제한되어있습니다.");
				sb.append("<br>");
				sb.append("사유: ");
				sb.append(rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_BLOCKED_REASON));
				
				result = new String[] { LOGIN_RESULT_FAIL , sb.toString() };
				
				sb = null;
			}
		}else {
			// 로그인 실패
			result = new String[] { LOGIN_RESULT_FAIL , "아이디 또는 비밀번호가 잘못되었습니다." };
		}
		
		// 작업 전환
		if (result == null) {
			// 사용자 정보
			String userName = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_NAME);
			String userEmail = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_EMAIL);
			String userBirthday = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_BIRTHDAY);
			String userRegDate = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_REGDATE);
			// 쿼리 생성 StringBuilder 초기화
			sql.delete(0,sql.length());
			String uuid = null;
			// Token 생성 여부 확인
			if (isNoCreateToken) {
				// 데이터 설정
				uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
				// 데이터 확인
				if (uuid == null || uuid.replace(" ", "").equals("")) {
					// 데이터 생성
					UUID uuid_ = UUID.randomUUID();
					uuid = uuid_.toString();
					// 쿼리 생성
					sql.append("UPDATE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
					sql.append(" SET ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
					sql.append(" = ?, ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
					sql.append(" = ? WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
					sql.append("= ?;");
					// 데이터베이스 접속
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, uuid);
					stat.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					stat.setString(3, user_uuid);
				}else {
					// 쿼리 생성
					sql.append("UPDATE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
					sql.append(" SET ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
					sql.append(" = ? WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
					sql.append("= ?;");
					// 데이터베이스 접속
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					stat.setString(2, user_uuid);	
				}
			}else {
				// 쿼리 생성
				sql.append("UPDATE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
				sql.append(" SET ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
				sql.append(" = ?, ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
				sql.append(" = ? WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
				sql.append("= ?;");
				// 데이터 생성
				UUID uuid_ = UUID.randomUUID();
				uuid = uuid_.toString();
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, uuid_.toString());
				stat.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				stat.setString(3, user_uuid);
			}
			// 쿼리 실행
			stat.executeUpdate();
			// 연결 해제
			rs.close();
			stat.close();
			cont.Close();
			
			// Null 확인
			if (rsp != null) {
				// 쿠키 생성 확인
				// ==========================================================================
				// 쿠키 생성
			    Cookie c1 = new Cookie("AutoLogin_UserUUID", user_uuid);
			    // 쿠키에 경로 추가
			    c1.setPath("/");
			    // 쿠키에 설명을 추가
			    c1.setComment("UserUUID");
			    // 쿠키 유효기간을 설정
			    c1.setMaxAge(60);
				// 쿠키 생성
			    Cookie c2 = new Cookie("AutoLogin_TokenUUID", uuid.toString());
			    // 쿠키에 경로 추가
			    c2.setPath("/");
			    // 쿠키에 설명을 추가
			    c2.setComment("UserUUID");
			    // 쿠키 유효기간을 설정
			    c2.setMaxAge(60);
			    // 응답헤더에 쿠키를 추가
			    rsp.addCookie(c1);
			    rsp.addCookie(c2);
				// ==========================================================================	
			}
			
			// 사용자 권한 가져오기
			UserPermissionChecker userPermissionChecker = new UserPermissionChecker();
			int permissionResult = userPermissionChecker.getUserPermission(user_uuid);
			
			// 로그인 성공
			result = new String[] { LOGIN_RESULT_SUCCESS , user_uuid, uuid.toString(), id, userName, userEmail, userBirthday, userRegDate, pw, String.valueOf(permissionResult) };
			
			// 데이터 반환
			return result;
		}else {
			// 연결 해제
			rs.close();
			stat.close();
			cont.Close();
			
			// 데이터 반환
			return result;
		}
	}
	
	
	// 자동 로그인
	public static String[] IsAutoLogin(String TokenKey, String UserUUID, HttpServletResponse rsp, boolean isNoCreateToken) throws ClassNotFoundException, SQLException, ParseException {
		// 변수 선언
		String getID = null;
		String getPW = null;
		String getTokenKey = null;
		String getTokenDate = null;
		String[] result = null;
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		sql.append(" = ?;");
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, UserUUID);
		// 쿼리 실행
		rs = stat.executeQuery();
		// 결과 처리
		if (rs.next()) {
			getID = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
			getPW = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		}
		// 쿼리 생성 StringBuilder 초기화
		sql.delete(0,sql.length());
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" = ?;");
		// 데이터베이스 접속
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, UserUUID);
		// 쿼리 실행
		rs = stat.executeQuery();
		// 결과 처리
		if (rs.next()) {
			getTokenKey = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
			getTokenDate = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE);
		}
		// Null 확인
		if (getTokenDate != null && getTokenKey != null && getID != null && getPW != null) {
			// 토큰값 확인
			if (getTokenKey.equals(TokenKey)) {
				// 토큰 생성 날자 확인
				if (DateTimeChecker.isTime_H(getTokenDate, LOGIN_TOKEN_TIME)) {
					// 연결 해제
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 진행 및 결과 반환
					return IsLoginUser(getID, getPW, rsp, isNoCreateToken);
				}else {
					// 연결 해제
					rs.close();
					stat.close();
					cont.Close();
					// 결과 생성
					result = new String[] { LOGIN_RESULT_NOMESSAGE };
					
					// 결과 반환
					return result;
				}
			}else {
				// 연결 해제
				rs.close();
				stat.close();
				cont.Close();
				// 결과 생성
				result = new String[] { LOGIN_RESULT_FAIL , "토큰값 또는 계정 아이디가 잘못되었습니다." };
				
				// 결과 반환
				return result;
			}
		}else {
			// 연결 해제
			rs.close();
			stat.close();
			cont.Close();
			// 결과 생성
			result = new String[] { LOGIN_RESULT_FAIL , "토큰값 또는 계정 아이디가 잘못되었습니다." };
			
			// 결과 반환
			return result;
		}
	}
	
	
	// JSP 자동 로그인 함수
	public static void AutoLoginTask(RhyaLogger rl, HttpSession session, HttpServletRequest req, HttpServletResponse rsp, boolean thisPageLoginOnly, boolean urlNext, String url, boolean isNoCreateToken) throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, SQLException, ParseException, IOException {
		String[] login_session;
		
		// 쿠키 데이터 확인
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
					// 자동 로그인 확인
					login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
				}
			}else {
				// 자동 로그인 확인
				login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
			}
		}catch (Exception e) {
			// TODO: handle exception
			
			// 자동 로그인 확인
			login_session = (String[]) session.getAttribute(LOGIN_SESSION_NAME);
		}

		// Null 값 확인
		if (login_session != null) {
			// 기존 세션 제거
			session.removeAttribute(LOGIN_SESSION_NAME);
			// 자동 로그인
			String[] auto_login_result = IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), rsp, isNoCreateToken);
			// 자동 로그인 결과 확인
			if (auto_login_result[0].equals(LOGIN_RESULT_SUCCESS)) {
				// 데이터 암호화
				String enc_value_1 = RhyaAES.AES_Encode(auto_login_result[1]);
				String enc_value_2 = RhyaAES.AES_Encode(auto_login_result[2]);
				// 세션 등록
				session.setAttribute(LOGIN_SESSION_NAME, new String[] { enc_value_1, enc_value_2 } );
				/*
				// 쿠키 생성
				// ==========================================================================
			    Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", enc_value_1);
			    // 쿠키에 경로 추가
			    c3.setPath("/");
			    // 쿠키에 설명을 추가
			    c3.setComment("UserUUID");
			    // 쿠키 유효기간을 설정
			    c3.setMaxAge(60 * 60 * 24 * 7);
				// 쿠키 생성
			    Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", enc_value_2);
			    // 쿠키에 경로 추가
			    c4.setPath("/");
			    // 쿠키에 설명을 추가
			    c4.setComment("TokenUUID");
			    // 쿠키 유효기간을 설정
			    c4.setMaxAge(60 * 60 * 24 * 7);
			    // 응답헤더에 쿠키를 추가
			    rsp.addCookie(c3);
			    rsp.addCookie(c4);		
			    */
				// 쿠키 생성 관리자
				PageParameter.SignIn signinV = new PageParameter.SignIn();
				CookieGenerator cookieGen = new CookieGenerator();
				// 쿠키 생성
				cookieGen.createCookie(rsp, rl, GetClientIPAddress.getClientIp(req), signinV.COOKIE_NAME_USER, auto_login_result[1], "/", signinV.COOKIE_NAME_USER, 60);
			    cookieGen.createCookie(rsp, rl, GetClientIPAddress.getClientIp(req), signinV.COOKIE_NAME_TOKEN, auto_login_result[2], "/", signinV.COOKIE_NAME_TOKEN, 60);
				// ==========================================================================
				// 로그 작성
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(GetClientIPAddress.getClientIp(req),"자동 로그인 성공 : USER_UUID = ", auto_login_result[1]));
				// 페이지 이동 확인
				if (urlNext) {
					// 페이지 이동
					rsp.sendRedirect(url);
				}
			}else {
				// 자동 로그인 실패
				if (thisPageLoginOnly) {
					// 로그 작성
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(req),"자동 로그인 실패 : RESULT != LOGIN_RESULT_SUCCESS"));
					// 자동 로그인 실패 --> 로그인 페이지 이동
					rsp.sendRedirect(JspPageInfo.GetJspPageURL(req, 0));	
				}
			}
		}else {
			// 자동 로그인 실패
			if (thisPageLoginOnly) {
				// 로그 작성
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(req),"자동 로그인 실패 : LOGIN_SESSION == null"));
				// 자동 로그인 실패 --> 로그인 페이지 이동
				rsp.sendRedirect(JspPageInfo.GetJspPageURL(req, 0));	
			}
		}
	}
}
