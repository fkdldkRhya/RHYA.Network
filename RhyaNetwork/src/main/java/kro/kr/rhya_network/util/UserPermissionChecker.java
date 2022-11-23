package kro.kr.rhya_network.util;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class UserPermissionChecker {
	/**
	 * 사용자 권한 가져오는 함수
	 * @param userUUID 사용자 UUID
	 * @return 권한 LEVEL ( -1 : 알 수 없는 오류 및 권한 정보를 가져올 수 없음 )
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
