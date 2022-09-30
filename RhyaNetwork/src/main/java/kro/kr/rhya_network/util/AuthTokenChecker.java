package kro.kr.rhya_network.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kro.kr.rhya_network.database.DatabaseConnection;
import kro.kr.rhya_network.database.DatabaseInfo;

public class AuthTokenChecker {
	// Auth ���
	public static final String AUTH_RESULT_SUCCESS = "success";
	public static final String AUTH_RESULT_FAIL = "fail";
	
	
	public String[] isAuthUser(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// ���� ����
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				// �α��� ����
				return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
			}else {
				// ���� ���� Ȯ��
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				// ���� ���� StringBuilder �ʱ�ȭ
				sql.delete(0,sql.length());
				// ���� ����
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// �����ͺ��̽� ����
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// ���� ����
				rs = stat.executeQuery();
				// ��� ��
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					String[] login_result = LoginChecker.IsLoginUser(id, password, null, true);
					
					if (login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						
						// �α��� ����
						return new String[] { AUTH_RESULT_SUCCESS, login_result[1] };
					}else {
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						
						// �α��� ����
						return new String[] { AUTH_RESULT_FAIL , login_result[1]};
					}
				}else {
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
				}
			}
		}else {
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			// �α��� ����
			return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
		}
	}
	
	
	public String[] getAuthInfo(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// ���� ����
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				
				// �α��� ����
				return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
			}else {
				// ���� ���� Ȯ��
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				String name = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_NAME);
				String date = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN_DATE);
				// ���� ���� StringBuilder �ʱ�ȭ
				sql.delete(0,sql.length());
				// ���� ����
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// �����ͺ��̽� ����
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// ���� ����
				rs = stat.executeQuery();
				// ��� ��
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					String[] login_result = LoginChecker.IsLoginUser(id, password, null, true);
					
					if (login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						
						// �α��� ����
						return new String[] { AUTH_RESULT_SUCCESS ,
								user,
								name,
								date};	
					}else {
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						
						// �α��� ����
						return new String[] { AUTH_RESULT_FAIL , login_result[1]};
					}
				}else {
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
				}
			}
		}else {
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			// �α��� ����
			return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
		}
	}
	
	
	public String[] getMoreAuthInfo(String authToken) throws ClassNotFoundException, SQLException {
		DatabaseConnection cont = new DatabaseConnection();
		PreparedStatement stat = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder();
		String id = null;
		String password = null;
		// ���� ����
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, authToken);
		// ���� ����
		rs = stat.executeQuery();
		if (rs.next()) {
			if (!rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN).equals(authToken)) {
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				
				// �α��� ����
				return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
			}else {
				// ������
				String user = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
				// ���� ���� StringBuilder �ʱ�ȭ
				sql.delete(0,sql.length());
				// ���� ����
				sql.append("SELECT * FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
				sql.append(" = ?;");
				// �����ͺ��̽� ����
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user);
				// ���� ����
				rs = stat.executeQuery();
				// ��� ��
				if (rs.next()) {
					id = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
					password = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					return LoginChecker.IsLoginUser(id, password, null, true);
				}else {
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					
					// �α��� ����
					return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
				}
			}
		}else {
			// ���� ����
			rs.close();
			stat.close();
			cont.Close();
			// �α��� ����
			return new String[] { AUTH_RESULT_FAIL , "Auth token�� �߸��Ǿ����ϴ�." };
		}
	}
}
