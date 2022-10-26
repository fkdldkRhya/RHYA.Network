package kro.kr.rhya_network.util;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class UserPermissionChecker {
	/**
	 * ����� ���� �������� �Լ�
	 * @param userUUID ����� UUID
	 * @return ���� LEVEL ( -1 : �� �� ���� ���� �� ���� ������ ������ �� ���� )
	 */
	public int getUserPermission(String userUUID) {
		try {
			int result = -1;
			
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM admin_permission WHERE user_uuid = ?;");
			databaseConnection.getPreparedStatement().setString(1, userUUID);
			databaseConnection.setResultSet();
			
			if (databaseConnection.getResultSet().next()) {
				result = databaseConnection.getResultSet().getInt("permission");
			}
			
			databaseConnection.allClose();
			
			return result;
		}catch (Exception ex) {
			ex.printStackTrace();
			
			return -1;
		}
	}
}
