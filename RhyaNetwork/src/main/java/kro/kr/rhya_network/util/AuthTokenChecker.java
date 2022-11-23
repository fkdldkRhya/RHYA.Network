package kro.kr.rhya_network.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kro.kr.rhya_network.database.DatabaseConnection;
import kro.kr.rhya_network.database.DatabaseInfo;

public class AuthTokenChecker {
	// Auth 결과
	public static final String AUTH_RESULT_SUCCESS = "success";
	public static final String AUTH_RESULT_FAIL = "fail";
	
	
	public String[] isAuthUser(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// 쿼리 실행
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				// 로그인 실패
				return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
			}else {
				// 계정 정보 확인
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				// 쿼리 생성 StringBuilder 초기화
				sql.delete(0,sql.length());
				// 쿼리 생성
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// 쿼리 실행
				rs = stat.executeQuery();
				// 결과 비교
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 진행
					String[] login_result = LoginChecker.IsLoginUser(id, password, null, true);
					
					if (login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						
						// 로그인 성공
						return new String[] { AUTH_RESULT_SUCCESS, login_result[1] };
					}else {
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						
						// 로그인 실패
						return new String[] { AUTH_RESULT_FAIL , login_result[1]};
					}
				}else {
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 실패
					return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
				}
			}
		}else {
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			// 로그인 실패
			return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
		}
	}
	
	
	public String[] getAuthInfo(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// 쿼리 실행
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				
				// 로그인 실패
				return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
			}else {
				// 계정 정보 확인
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				String name = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_NAME);
				String date = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN_DATE);
				// 쿼리 생성 StringBuilder 초기화
				sql.delete(0,sql.length());
				// 쿼리 생성
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// 쿼리 실행
				rs = stat.executeQuery();
				// 결과 비교
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 진행
					String[] login_result = LoginChecker.IsLoginUser(id, password, null, true);
					
					if (login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						
						// 로그인 성공
						return new String[] { AUTH_RESULT_SUCCESS ,
								user,
								name,
								date};	
					}else {
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						
						// 로그인 실패
						return new String[] { AUTH_RESULT_FAIL , login_result[1]};
					}
				}else {
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 실패
					return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
				}
			}
		}else {
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			// 로그인 실패
			return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
		}
	}
	
	
	public String[] getMoreAuthInfo(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// 쿼리 생성
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// 쿼리 실행
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				
				// 로그인 실패
				return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
			}else {
				// 데이터
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				// 쿼리 생성 StringBuilder 초기화
				sql.delete(0,sql.length());
				// 쿼리 생성
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// 쿼리 실행
				rs = stat.executeQuery();
				// 결과 비교
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 진행
					return LoginChecker.IsLoginUser(id, password, null, true);
				}else {
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					
					// 로그인 실패
					return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
				}
			}
		}else {
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			// 로그인 실패
			return new String[] { AUTH_RESULT_FAIL , "Auth token이 잘못되었습니다." };
		}
	}
}
