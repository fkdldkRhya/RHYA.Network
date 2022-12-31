package kro.kr.rhya_network.utaite_player;

import java.util.ArrayList;

import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

public class UtaitePlayerEQSettingManager {
	// 사용자 UUID
	private String user_uuid = null;
	
	
	
	
	
	/**
	 * 생성자
	 */
	public UtaitePlayerEQSettingManager(String user_uuid) {
		this.user_uuid = user_uuid;
	}
	
	
	
	/**
	 * EQ 설정 생성
	 * @param eq_name EQ 설정 이름
	 * @param eq_date EQ 설정 생성/수정 날짜
	 * @param value1 EQ 값 (Frequency: 60)
	 * @param vlaue2 EQ 값 (Frequency: 170)
	 * @param vlaue3 EQ 값 (Frequency: 310)
	 * @param vlaue4 EQ 값 (Frequency: 600)
	 * @param vlaue5 EQ 값 (Frequency: 1000)
	 * @param vlaue6 EQ 값 (Frequency: 3000)
	 * @param vlaue7 EQ 값 (Frequency: 6000)
	 * @param vlaue8 EQ 값 (Frequency: 12000)
	 * @param vlaue9 EQ 값 (Frequency: 14000)
	 * @param vlaue10 EQ 값 (Frequency: 16000)
	 * @return EQ 아이디 (-1: EQ 데이터 생성 실패)
	 */
	public int createEQValue(
			String eq_name,
			String eq_date,
			double value1,
			double value2,
			double value3,
			double value4,
			double value5,
			double value6,
			double value7,
			double value8,
			double value9,
			double value10) {
		// 예외 처리
		try {
			int randomEQID = (int)(Math.random() * 9999999);
			
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("INSERT INTO utaite_win_eq_setting(user_uuid, eq_id, eq_setting_name, eq_setting_date, eq_value_60, eq_value_170, eq_value_310, eq_value_600, eq_value_1000, eq_value_3000, eq_value_6000, eq_value_12000, eq_value_14000, eq_value_16000) VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			databaseConnection.getPreparedStatement().setString(1, user_uuid);
			databaseConnection.getPreparedStatement().setInt(2, randomEQID);
			databaseConnection.getPreparedStatement().setString(3, eq_name);
			databaseConnection.getPreparedStatement().setString(4, eq_date);
			databaseConnection.getPreparedStatement().setDouble(5, value1);
			databaseConnection.getPreparedStatement().setDouble(6, value2);
			databaseConnection.getPreparedStatement().setDouble(7, value3);
			databaseConnection.getPreparedStatement().setDouble(8, value4);
			databaseConnection.getPreparedStatement().setDouble(9, value5);
			databaseConnection.getPreparedStatement().setDouble(10, value6);
			databaseConnection.getPreparedStatement().setDouble(11, value7);
			databaseConnection.getPreparedStatement().setDouble(12, value8);
			databaseConnection.getPreparedStatement().setDouble(13, value9);
			databaseConnection.getPreparedStatement().setDouble(14, value10);
			databaseConnection.executeUpdate();
			databaseConnection.allClose();
			
			return randomEQID;
		}catch (Exception ex) {
			return -1;
		}
	}
	
	
	
	/**
	 * EQ 설정 수정
	 * @param eq_vlaue_id EQ 설정 정수형 아이디
	 * @param eq_name EQ 설정 이름
	 * @param eq_date EQ 설정 생성/수정 날짜
	 * @param value1 EQ 값 (Frequency: 60)
	 * @param vlaue2 EQ 값 (Frequency: 170)
	 * @param vlaue3 EQ 값 (Frequency: 310)
	 * @param vlaue4 EQ 값 (Frequency: 600)
	 * @param vlaue5 EQ 값 (Frequency: 1000)
	 * @param vlaue6 EQ 값 (Frequency: 3000)
	 * @param vlaue7 EQ 값 (Frequency: 6000)
	 * @param vlaue8 EQ 값 (Frequency: 12000)
	 * @param vlaue9 EQ 값 (Frequency: 14000)
	 * @param vlaue10 EQ 값 (Frequency: 16000)
	 * @return EQ 데이터 수정 성공 여부
	 */
	public boolean editEQValue(
			int eq_vlaue_id,
			String eq_name,
			String eq_date,
			double value1,
			double value2,
			double value3,
			double value4,
			double value5,
			double value6,
			double value7,
			double value8,
			double value9,
			double value10) {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("UPDATE utaite_win_eq_setting SET eq_setting_name = ?, eq_setting_date = ?, eq_value_60 = ?, eq_value_170 = ?, eq_value_310 = ?, eq_value_600 = ?, eq_value_1000 = ?, eq_value_3000 = ?, eq_value_6000 = ?, eq_value_12000 = ?, eq_value_14000 = ?, eq_value_16000 = ? WHERE user_uuid = ? AND eq_id = ?");
			databaseConnection.getPreparedStatement().setString(1, eq_name);
			databaseConnection.getPreparedStatement().setString(2, eq_date);
			databaseConnection.getPreparedStatement().setDouble(3, value1);
			databaseConnection.getPreparedStatement().setDouble(4, value2);
			databaseConnection.getPreparedStatement().setDouble(5, value3);
			databaseConnection.getPreparedStatement().setDouble(6, value4);
			databaseConnection.getPreparedStatement().setDouble(7, value5);
			databaseConnection.getPreparedStatement().setDouble(8, value6);
			databaseConnection.getPreparedStatement().setDouble(9, value7);
			databaseConnection.getPreparedStatement().setDouble(10, value8);
			databaseConnection.getPreparedStatement().setDouble(11, value9);
			databaseConnection.getPreparedStatement().setDouble(12, value10);
			databaseConnection.getPreparedStatement().setString(13, user_uuid);
			databaseConnection.getPreparedStatement().setInt(14, eq_vlaue_id);
			int result = databaseConnection.executeUpdate();
			databaseConnection.allClose();
			
			return result >= 1;
		}catch (Exception ex) {
			return false;
		}
	}
	
	
	
	/**
	 * EQ 설정 제거
	 * @param eq_vlaue_id EQ 설정 정수형 아이디
	 * @return EQ 데이터 제거 성공 여부
	 */
	public boolean deleteEQValue(int eq_vlaue_id) {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("DELETE FROM utaite_win_eq_setting WHERE user_uuid = ? AND eq_id = ?");
			databaseConnection.getPreparedStatement().setString(1, user_uuid);
			databaseConnection.getPreparedStatement().setInt(2, eq_vlaue_id);
			int result = databaseConnection.executeUpdate();
			databaseConnection.allClose();
			
			return result >= 1;
		}catch (Exception ex) {
			return false;
		}
	}
	
	
	
	/**
	 * EQ 설정 데이터 불러오기
	 * @param eq_vlaue_id EQ 설정 정수형 아이디
	 * @return EQ 데이터
	 */
	public UtaitePlayerEQSettingVO getEQValueData(int eq_vlaue_id) {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM utaite_win_eq_setting WHERE user_uuid = ? AND eq_id = ?");
			databaseConnection.getPreparedStatement().setString(1, user_uuid);
			databaseConnection.getPreparedStatement().setInt(2, eq_vlaue_id);
			databaseConnection.setResultSet();
			
			UtaitePlayerEQSettingVO utaitePlayerEQSettingVO = null;
			
			if (databaseConnection.getResultSet().next()) {
				utaitePlayerEQSettingVO = new UtaitePlayerEQSettingVO();
				utaitePlayerEQSettingVO.eq_id = eq_vlaue_id;
				utaitePlayerEQSettingVO.eq_setting_name = databaseConnection.getResultSet().getString("eq_setting_name");
				utaitePlayerEQSettingVO.eq_setting_date = databaseConnection.getResultSet().getString("eq_setting_date");
				utaitePlayerEQSettingVO.eq_value_60 = databaseConnection.getResultSet().getDouble("eq_value_60");
				utaitePlayerEQSettingVO.eq_value_170 = databaseConnection.getResultSet().getDouble("eq_value_170");
				utaitePlayerEQSettingVO.eq_value_310 = databaseConnection.getResultSet().getDouble("eq_value_310");
				utaitePlayerEQSettingVO.eq_value_600 = databaseConnection.getResultSet().getDouble("eq_value_600");
				utaitePlayerEQSettingVO.eq_value_1000 = databaseConnection.getResultSet().getDouble("eq_value_1000");
				utaitePlayerEQSettingVO.eq_value_3000 = databaseConnection.getResultSet().getDouble("eq_value_3000");
				utaitePlayerEQSettingVO.eq_value_6000 = databaseConnection.getResultSet().getDouble("eq_value_6000");
				utaitePlayerEQSettingVO.eq_value_12000 = databaseConnection.getResultSet().getDouble("eq_value_12000");
				utaitePlayerEQSettingVO.eq_value_14000 = databaseConnection.getResultSet().getDouble("eq_value_14000");
				utaitePlayerEQSettingVO.eq_value_16000 = databaseConnection.getResultSet().getDouble("eq_value_16000");
			}
			
			databaseConnection.allClose();

			return utaitePlayerEQSettingVO;
		}catch (Exception ex) {
			return null;
		}
	}
	
	
	
	/**
	 * EQ 설정 데이터 불러오기
	 * @return EQ 모든 데이터
	 */
	public ArrayList<UtaitePlayerEQSettingVO> getEQValueAllData() {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM utaite_win_eq_setting WHERE user_uuid = ?");
			databaseConnection.getPreparedStatement().setString(1, user_uuid);
			databaseConnection.setResultSet();
			
			ArrayList<UtaitePlayerEQSettingVO> list = new ArrayList<UtaitePlayerEQSettingVO>();
			
			while (databaseConnection.getResultSet().next()) {
				UtaitePlayerEQSettingVO utaitePlayerEQSettingVO = new UtaitePlayerEQSettingVO();
				utaitePlayerEQSettingVO.eq_id = databaseConnection.getResultSet().getInt("eq_id");
				utaitePlayerEQSettingVO.eq_setting_name = databaseConnection.getResultSet().getString("eq_setting_name");
				utaitePlayerEQSettingVO.eq_setting_date = databaseConnection.getResultSet().getString("eq_setting_date");
				utaitePlayerEQSettingVO.eq_value_60 = databaseConnection.getResultSet().getDouble("eq_value_60");
				utaitePlayerEQSettingVO.eq_value_170 = databaseConnection.getResultSet().getDouble("eq_value_170");
				utaitePlayerEQSettingVO.eq_value_310 = databaseConnection.getResultSet().getDouble("eq_value_310");
				utaitePlayerEQSettingVO.eq_value_600 = databaseConnection.getResultSet().getDouble("eq_value_600");
				utaitePlayerEQSettingVO.eq_value_1000 = databaseConnection.getResultSet().getDouble("eq_value_1000");
				utaitePlayerEQSettingVO.eq_value_3000 = databaseConnection.getResultSet().getDouble("eq_value_3000");
				utaitePlayerEQSettingVO.eq_value_6000 = databaseConnection.getResultSet().getDouble("eq_value_6000");
				utaitePlayerEQSettingVO.eq_value_12000 = databaseConnection.getResultSet().getDouble("eq_value_12000");
				utaitePlayerEQSettingVO.eq_value_14000 = databaseConnection.getResultSet().getDouble("eq_value_14000");
				utaitePlayerEQSettingVO.eq_value_16000 = databaseConnection.getResultSet().getDouble("eq_value_16000");
				
				list.add(utaitePlayerEQSettingVO);
			}
			
			databaseConnection.allClose();

			return list;
		}catch (Exception ex) {
			return null;
		}
	}
}
