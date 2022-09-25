package kro.kr.rhya_network.security;

import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class IPBlockChecker {
	public static boolean isIPBlock(String ipAddress) {
		try {
			DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.initStringBuilder();
			databaseConnection.connection();
			
			databaseConnection.setPreparedStatement("SELECT ipAddress FROM ip_block;");
			databaseConnection.setResultSet();
			
			while (databaseConnection.getResultSet().next()) {
				if (ipAddress.trim().equals(databaseConnection.getResultSet().getString("ipAddress"))) {
					return false;
				}
			}
			
			databaseConnection.allClose();
			
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
	}
}
