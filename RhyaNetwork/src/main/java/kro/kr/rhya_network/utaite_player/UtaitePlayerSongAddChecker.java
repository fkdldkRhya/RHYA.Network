package kro.kr.rhya_network.utaite_player;

import java.sql.SQLException;

import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class UtaitePlayerSongAddChecker {
	public boolean checkUser(String user_uuid) throws SQLException, ClassNotFoundException {
		DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT * FROM utaite_add_song_checker WHERE user_uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, user_uuid);
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			int isAccess = databaseConnection.getResultSet().getInt("isAccess");
			databaseConnection.allClose();
			
			return isAccess == 1;
		}else {
			return true;
		}
	}
}
