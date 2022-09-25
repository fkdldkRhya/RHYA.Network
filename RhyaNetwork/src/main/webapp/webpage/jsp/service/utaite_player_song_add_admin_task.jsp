<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="kro.kr.rhya_network.utaite_player.UtaiteMusicVO"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.io.File"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="java.io.FileFilter"%>
<%@ page import="java.io.BufferedInputStream"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.BufferedOutputStream"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.SelfXSSFilter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaSHA512"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>
<%@ page import="kro.kr.rhya_network.util.PathManager"%>
<%@ page import="kro.kr.rhya_network.util.JSPUtilsInitTask"%>
<%@ page import="kro.kr.rhya_network.utils.db.DatabaseManager"%>
<%@ page import="kro.kr.rhya_network.utils.upload.FileNameToUUIDRenamePolicy"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<%
// Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
// Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// 클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

//출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();
final String successMessage = "S";
final String failMessage = "F";
final String keyName_Result = "result";
final String keyName_Message = "message";

try {
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_Admin_Task)) {
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		if (login_session != null) {
			// 자동 로그인
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			
			if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				// 권한 확인
				DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
				databaseConnection.init();
				databaseConnection.connection();
				databaseConnection.setPreparedStatement("SELECT * FROM admin_permission WHERE user_uuid = ?;");
				databaseConnection.getPreparedStatement().setString(1, auto_login_result[1]);
				databaseConnection.setResultSet();
				boolean isHavePermission = false;
				if (databaseConnection.getResultSet().next()) {
					if (databaseConnection.getResultSet().getInt("permission") >= 3) {
						isHavePermission = true;
					}
				}
				
				if (!isHavePermission) {
					rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "권한 부족, LEVEL 3 이상의 권한이 필요합니다."));
					
					obj.addProperty(keyName_Result, failMessage);
					obj.addProperty(keyName_Message, "권한 부족, LEVEL 3 이상의 권한이 필요합니다.");
					out.println(gson.toJson(obj));
				
					databaseConnection.allClose();
				  	
					return;
				}
				
				ArrayList<UtaiteMusicVO> utaiteMusicVOs = new ArrayList<UtaiteMusicVO>();
				
				databaseConnection.closeResultSet();
				databaseConnection.closePreparedStatement();
				databaseConnection.setPreparedStatement("SELECT * FROM utaite_add_song;");
				databaseConnection.setResultSet();
				while (databaseConnection.getResultSet().next()) {
					UtaiteMusicVO musicVO = new UtaiteMusicVO();
					musicVO.setMusic_uuid(databaseConnection.getResultSet().getString("uuid"));
					musicVO.setMusic_name(databaseConnection.getResultSet().getString("music_name"));
					musicVO.setMusic_singer(databaseConnection.getResultSet().getString("music_singer_uuid"));
					musicVO.setMusic_songwriter(databaseConnection.getResultSet().getString("music_songwriter"));
					musicVO.setMusic_type(databaseConnection.getResultSet().getString("music_type"));
					musicVO.setMusic_lyrics(databaseConnection.getResultSet().getString("music_lyrics"));
					
					utaiteMusicVOs.add(musicVO);
				}
				
				databaseConnection.closeResultSet();
				databaseConnection.closePreparedStatement();
		
				databaseConnection.stringBuilder.setLength(0);
				databaseConnection.stringBuilder.append(PathManager.UTAITE_PLAYER_IMAGE_ROOT_PATH);
				databaseConnection.stringBuilder.append(File.separator);
				databaseConnection.stringBuilder.append("song");
				databaseConnection.stringBuilder.append(File.separator);

				String imagePath = databaseConnection.stringBuilder.toString();
				databaseConnection.stringBuilder.setLength(0);
				
				for (UtaiteMusicVO utaiteMusicVO : utaiteMusicVOs) {
					// 파일 확인
					File orgFile_mp3 = new File(PathManager.UTAITE_PLAYER_ADD_SONG_ROOT_PATH, utaiteMusicVO.getMusic_uuid().concat(".mp3"));
					File orgFile_image = new File(PathManager.UTAITE_PLAYER_ADD_SONG_ROOT_PATH, utaiteMusicVO.getMusic_uuid().concat(".png"));
					File targetFile_mp3 = new File(PathManager.UTAITE_PLAYER_MANAGER_MP3_PATH, utaiteMusicVO.getMusic_uuid().concat(".mp3"));
					File targetFile_image = new File(imagePath, utaiteMusicVO.getMusic_uuid().concat(".png"));
					
					if (orgFile_mp3.exists() && orgFile_image.exists() && utaiteMusicVO.getMusic_singer() != null) {
						// 파일 이동
						FileUtils.moveFile(orgFile_mp3, targetFile_mp3);
						FileUtils.moveFile(orgFile_image, targetFile_image);
						
						// 이동 확인
						if (targetFile_mp3.exists() && targetFile_image.exists()) {
							try {
								// 데이터 삽입
								databaseConnection.setPreparedStatement("INSERT INTO utaite_list(music_name, music_lyrics, music_singer, music_songwriter, music_image, music_mp3, music_type) VALUE (?,?,?,?,?,?,?)");
								databaseConnection.getPreparedStatement().setString(1, utaiteMusicVO.getMusic_name());
								databaseConnection.getPreparedStatement().setString(2, utaiteMusicVO.getMusic_lyrics());
								databaseConnection.getPreparedStatement().setString(3, utaiteMusicVO.getMusic_singer());
								databaseConnection.getPreparedStatement().setString(4, utaiteMusicVO.getMusic_songwriter());
								
								databaseConnection.stringBuilder.setLength(0);
								databaseConnection.stringBuilder.append("https://rhya-network.kro.kr/RhyaNetwork/webpage/jsp/utils/utaite_player_image.jsp?type=1&uuid=");
								databaseConnection.stringBuilder.append(utaiteMusicVO.getMusic_uuid());
								
								databaseConnection.getPreparedStatement().setString(5, databaseConnection.stringBuilder.toString());
								
								databaseConnection.getPreparedStatement().setString(6, utaiteMusicVO.getMusic_uuid());
								databaseConnection.getPreparedStatement().setString(7, utaiteMusicVO.getMusic_type());
								databaseConnection.executeUpdate();
								
								databaseConnection.closePreparedStatement();
								
								if (orgFile_mp3.exists())
									orgFile_mp3.delete();
								if (orgFile_image.exists())
									orgFile_image.delete();
								
								databaseConnection.setPreparedStatement("DELETE FROM utaite_add_song WHERE uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, utaiteMusicVO.getMusic_uuid());
								databaseConnection.executeUpdate();
								databaseConnection.closePreparedStatement();
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "노래 PUSH 성공!", utaiteMusicVO.getMusic_uuid()));
							}catch (Exception ex) {
								ex.printStackTrace();
								
								if (targetFile_mp3.exists())
									targetFile_mp3.delete();
								if (targetFile_image.exists())
									targetFile_image.delete();
								
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "노래 PUSH 실패!", utaiteMusicVO.getMusic_uuid()));
							}
						}else {
							rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv8(clientIP, "파일 이동 실패!", utaiteMusicVO.getMusic_uuid()));
						}
					}else {
						rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "파일이 없거나 아티스트가 설정되지 않습니다.", utaiteMusicVO.getMusic_uuid()));
					}
				}
				
				obj.addProperty(keyName_Result, successMessage);
				out.println(gson.toJson(obj));
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "노래 PUSH 완료!"));
				
				databaseConnection.allClose();
			}else {
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인이 되어있지 않습니다."));
				
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, "로그인이 되어있지 않습니다.");
				out.println(gson.toJson(obj));
			}
		}else {
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인이 되어있지 않습니다."));
			
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, "로그인이 되어있지 않습니다.");
			out.println(gson.toJson(obj));
		}
	}else {
		obj.addProperty(keyName_Result, failMessage);
		obj.addProperty(keyName_Message, "JSP 페이지 초기화 중 오류가 발생하였습니다.");
		out.println(gson.toJson(obj));
		
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
	}
}catch(Exception ex) {
	obj.addProperty(keyName_Result, failMessage);
	obj.addProperty(keyName_Message, "알 수 없는 오류 발생!");
	out.println(gson.toJson(obj));
	
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	
	return;
}
%>