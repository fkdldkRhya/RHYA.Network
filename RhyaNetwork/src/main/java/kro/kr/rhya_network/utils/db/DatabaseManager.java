package kro.kr.rhya_network.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseManager {
	// 데이터베이스 서버 정보
	final String db_driver = "com.mysql.cj.jdbc.Driver";
	final String db_url = "jdbc:mysql://192.168.0.19:3306/rhya_network_server?serverTimezone=UTC";
	final String db_id = "RHYA_NETWORK";
	final String db_pw = "QC-CN7U$fu=Hx>aWgXmz=h^ZW{A/4URZ";
	
	// 데이터베이스 접속 관리
	public class DatabaseConnection {
		// 데이터베이스 변수
		Connection connection = null;
		PreparedStatement preparableStatement = null;
		ResultSet resultSet = null;
		// SQL 생성 변수
		public StringBuilder stringBuilder = null;
		
		// 초기화
		public void init() {
			stringBuilder = new StringBuilder();
		}
		
		// 데이터베이스 접속
		public void connection() throws ClassNotFoundException, SQLException {
			Class.forName(db_driver);
			connection = DriverManager.getConnection(db_url, db_id, db_pw);
		}
		
		
		// 데이터베이스 Connection
		public Connection getConnection() {
			return connection;
		}
		
		// 데이터베이스 Connection 닫기
		public void closeConntection() throws SQLException {
			connection.close();
		}
		
		// 데이터베이스 PreparedStatement 설정
		public void setPreparedStatement(String sql) throws SQLException {
			preparableStatement = connection.prepareStatement(sql);
		}
		
		// 데이터베이스 PreparedStatement 가져오기
		public PreparedStatement getPreparedStatement() {
			return preparableStatement;
		}
		
		// 데이터베이스 PreparedStatement 닫기
		public void closePreparedStatement() throws SQLException {
			preparableStatement.close();
		}
		
		// 데이터 베이스 쿼리 실행 - Update, Insert 등
		public int executeUpdate() throws SQLException {
			return preparableStatement.executeUpdate();
		}
		
		// 데이터베이스 ResultSet 설정
		public void setResultSet() throws SQLException {
			resultSet = preparableStatement.executeQuery();
		}
		
		// 데이터베이스 ResultSet 가져오기
		public ResultSet getResultSet() {
			return resultSet;
		}
		
		// 데이터베이스 ResultSet 닫기
		public void closeResultSet() throws SQLException {
			resultSet.close();
		}
		
		// StringBuilder 초기화
		public void initStringBuilder() {
			stringBuilder.delete(0, stringBuilder.length());
		}
		
		// 전체 닫기
		public void allClose() throws SQLException {
			if (connection != null) connection.close();
			if (preparableStatement != null) connection.close();
			if (resultSet != null) resultSet.close();
		}
	}
}
