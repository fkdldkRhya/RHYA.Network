package kro.kr.rhya_network.util;

import java.sql.SQLException;

import kro.kr.rhya_network.utils.db.DatabaseManager;


public class ServiceAccessChecker {
	/**
	 * RHYA.Network 서비스 접근 확인
	 * 
	 * @param type 서비스
	 * 		 - 0 : 우타이테 플레이어
	 * 		 - 1 : 온라인 출석부
	 * @return boolean
	 * @throws SQLException DB 접속 오류
	 * @throws ClassNotFoundException DB 접속 오류
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
			
			// 우타이테 플레이어
			case 0: {
				if (connection.getResultSet().next()) {
					isResult = connection.getResultSet().getInt("service_online_utaite_player") == 0;
				}else {
					isResult = false;
				}
				
				break;
			}
			
			// 온라인 출석부
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
