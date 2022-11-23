package kro.kr.rhya_network.admintool;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class ServerMainImageManager {
	/**
	 * 서버 메인 이미지 설정 상태 가져오기
	 * @return 0 - 기본 이미지로 설정, 1 - 랜덤 이미지로 설정
	 * @throws Exception 데이터베이스 접속 오류 및 기타 오류
	 */
	public int getServerMainSate() throws Exception {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT server_background_main_image_manager FROM rhya_network_admin_setting;");
			databaseConnection.setResultSet();
			
			int result = 0;
			
			if (databaseConnection.getResultSet().next()) {
				result = databaseConnection.getResultSet().getInt("server_background_main_image_manager");
			}
			
			databaseConnection.allClose();
			
			return result;
		}catch (Exception ex) {
			throw ex;
		}
	}
	

	
	/**
	 * 서버 메인 이미지 상태 설정
	 * @param value 0 - 기본 이미지로 설정, 1 - 랜덤 이미지로 설정
	 * @throws Exception 데이터베이스 접속 오류 및 기타 오류
	 */
	public void setServerMainSate(int value) throws Exception {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("UPDATE rhya_network_admin_setting SET server_background_main_image_manager = ?;");
			databaseConnection.getPreparedStatement().setInt(1, value);
			databaseConnection.executeUpdate();
			databaseConnection.allClose();
		}catch (Exception ex) {
			throw ex;
		}
	}
}
