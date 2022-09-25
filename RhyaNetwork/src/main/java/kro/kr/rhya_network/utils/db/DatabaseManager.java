package kro.kr.rhya_network.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseManager {
	// �����ͺ��̽� ���� ����
	final String db_driver = "com.mysql.cj.jdbc.Driver";
	final String db_url = "jdbc:mysql://192.168.0.19:3306/rhya_network_server?serverTimezone=UTC";
	final String db_id = "RHYA_NETWORK";
	final String db_pw = "QC-CN7U$fu=Hx>aWgXmz=h^ZW{A/4URZ";
	
	// �����ͺ��̽� ���� ����
	public class DatabaseConnection {
		// �����ͺ��̽� ����
		Connection connection = null;
		PreparedStatement preparableStatement = null;
		ResultSet resultSet = null;
		// SQL ���� ����
		public StringBuilder stringBuilder = null;
		
		// �ʱ�ȭ
		public void init() {
			stringBuilder = new StringBuilder();
		}
		
		// �����ͺ��̽� ����
		public void connection() throws ClassNotFoundException, SQLException {
			Class.forName(db_driver);
			connection = DriverManager.getConnection(db_url, db_id, db_pw);
		}
		
		
		// �����ͺ��̽� Connection
		public Connection getConnection() {
			return connection;
		}
		
		// �����ͺ��̽� Connection �ݱ�
		public void closeConntection() throws SQLException {
			connection.close();
		}
		
		// �����ͺ��̽� PreparedStatement ����
		public void setPreparedStatement(String sql) throws SQLException {
			preparableStatement = connection.prepareStatement(sql);
		}
		
		// �����ͺ��̽� PreparedStatement ��������
		public PreparedStatement getPreparedStatement() {
			return preparableStatement;
		}
		
		// �����ͺ��̽� PreparedStatement �ݱ�
		public void closePreparedStatement() throws SQLException {
			preparableStatement.close();
		}
		
		// ������ ���̽� ���� ���� - Update, Insert ��
		public int executeUpdate() throws SQLException {
			return preparableStatement.executeUpdate();
		}
		
		// �����ͺ��̽� ResultSet ����
		public void setResultSet() throws SQLException {
			resultSet = preparableStatement.executeQuery();
		}
		
		// �����ͺ��̽� ResultSet ��������
		public ResultSet getResultSet() {
			return resultSet;
		}
		
		// �����ͺ��̽� ResultSet �ݱ�
		public void closeResultSet() throws SQLException {
			resultSet.close();
		}
		
		// StringBuilder �ʱ�ȭ
		public void initStringBuilder() {
			stringBuilder.delete(0, stringBuilder.length());
		}
		
		// ��ü �ݱ�
		public void allClose() throws SQLException {
			if (connection != null) connection.close();
			if (preparableStatement != null) connection.close();
			if (resultSet != null) resultSet.close();
		}
	}
}
