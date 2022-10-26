package kro.kr.rhya_network.admintool;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class ServerMainImageManager {
	/**
	 * ���� ���� �̹��� ���� ���� ��������
	 * @return 0 - �⺻ �̹����� ����, 1 - ���� �̹����� ����
	 * @throws Exception �����ͺ��̽� ���� ���� �� ��Ÿ ����
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
	 * ���� ���� �̹��� ���� ����
	 * @param value 0 - �⺻ �̹����� ����, 1 - ���� �̹����� ����
	 * @throws Exception �����ͺ��̽� ���� ���� �� ��Ÿ ����
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
