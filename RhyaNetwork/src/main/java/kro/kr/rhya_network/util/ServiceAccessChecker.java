package kro.kr.rhya_network.util;

import java.sql.SQLException;

import kro.kr.rhya_network.utils.db.DatabaseManager;


public class ServiceAccessChecker {
	/**
	 * RHYA.Network ���� ���� Ȯ��
	 * 
	 * @param type ����
	 * 		 - 0 : ��Ÿ���� �÷��̾�
	 * 		 - 1 : �¶��� �⼮��
	 * @return boolean
	 * @throws SQLException DB ���� ����
	 * @throws ClassNotFoundException DB ���� ����
	 */
	public boolean isAccessService(int type) throws ClassNotFoundException, SQLException {
		DatabaseManager.DatabaseConnection connection = new DatabaseManager.DatabaseConnection();
		connection.init();
		connection.connection();
		connection.setPreparedStatement("SELECT * FROM rhya_network_info;");
		connection.setResultSet();
		
		boolean isResult = false;
		
		switch (type) {
			default: isResult = false;
			
			// ��Ÿ���� �÷��̾�
			case 0: {
				if (connection.getResultSet().next()) {
					isResult = connection.getResultSet().getInt("service_online_utaite_player") == 0;
				}else {
					isResult = false;
				}
				
				break;
			}
			
			// �¶��� �⼮��
			case 1: {
				if (connection.getResultSet().next()) {
					isResult = connection.getResultSet().getInt("service_online_online_attendance") == 0;
				}else {
					isResult = false;
				}
				
				break;
			}
		}
		
		connection.allClose();
		
		return isResult;
	}
}
