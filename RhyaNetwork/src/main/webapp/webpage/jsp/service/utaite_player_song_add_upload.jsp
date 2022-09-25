<%@page import="kro.kr.rhya_network.utaite_player.UtaitePlayerSongAddChecker"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileFilter"%>
<%@ page import="java.io.BufferedInputStream"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.BufferedOutputStream"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="org.apache.commons.io.IOUtils"%>
<%@ page import="com.oreilly.servlet.multipart.DefaultFileRenamePolicy"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>
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

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
// Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
// Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// 클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

// 출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();
final String successMessage = "S";
final String failMessage = "F";
final String keyName_Result = "result";
final String keyName_Message = "message";


try {
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_File_Upload)) {		
		// 데이터베이스 연결
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		
		try {
			// 등록되지 않은 파일 제거
			databaseConnection.setPreparedStatement("SELECT uuid FROM utaite_add_song;");
			databaseConnection.setResultSet();
			ArrayList<String> allUUID = new ArrayList<String>();
			while (databaseConnection.getResultSet().next()) 
				allUUID.add(databaseConnection.getResultSet().getString("uuid"));
			File dir = new File(PathManager.UTAITE_PLAYER_ADD_SONG_ROOT_PATH);
			File files[] = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files != null && files[i] != null) {
					String fileName = files[i].getName();
					if (fileName != null) {
					 	int Idx = fileName.lastIndexOf(".");
						String _fileName = fileName.substring(0, Idx);
						if (!allUUID.contains(_fileName)) {
							files[i].delete();
						}		
					}
				}
			}	
		}catch (Exception ex) {
			ex.printStackTrace();
		}
			
		FileNameToUUIDRenamePolicy fileNameToUUIDRenamePolicy = new FileNameToUUIDRenamePolicy();
		MultipartRequest multi = new MultipartRequest( // MultipartRequest 인스턴스 생성(cos.jar의 라이브러리)
				request, 
				PathManager.UTAITE_PLAYER_ADD_SONG_ROOT_PATH, // 파일을 저장할 디렉토리 지정
				30 * 1024 * 1024, // 첨부파일 최대 용량 설정(bite) / 30MB / 용량 초과 시 예외 발생
				"utf-8", // 인코딩 방식 지정
				fileNameToUUIDRenamePolicy // 중복 파일 처리
		);

		// 사용자 로그인 확인
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		if (login_session != null) {
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				UtaitePlayerSongAddChecker utaitePlayerSongAddChecker = new UtaitePlayerSongAddChecker();
				if (!utaitePlayerSongAddChecker.checkUser(auto_login_result[1])) {
					obj.addProperty(keyName_Result, failMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode("사용자 접근 거부!", "UTF-8"));
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "사용자 접근 거부!"));
					
					return;
				}
			}else {
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, URLEncoder.encode("사용자 로그인 실패!", "UTF-8"));
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "사용자 로그인 실패!"));
				
				return;
			}
		}else {
			if (multi.getParameter("auth") != null) {
				AuthTokenChecker authTokenChecker = new AuthTokenChecker();
				String[] result = authTokenChecker.getMoreAuthInfo(multi.getParameter("auth"));
				
				if (result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
					UtaitePlayerSongAddChecker utaitePlayerSongAddChecker = new UtaitePlayerSongAddChecker();
					if (!utaitePlayerSongAddChecker.checkUser(result[1])) {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, URLEncoder.encode("사용자 접근 거부!", "UTF-8"));
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "사용자 접근 거부!"));
						
						return;
					}
				}else {
					obj.addProperty(keyName_Result, failMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode("사용자 로그인 실패!", "UTF-8"));
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "사용자 로그인 실패!"));
					
					return;
				}
			}else {
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, URLEncoder.encode("사용자 로그인 실패!", "UTF-8"));
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "사용자 로그인 실패!"));
				
				return;
			}
		}

		
		// 파라미터 입력
		String version = multi.getParameter("version");
		if (version != null && version.equals("1")) {
			String songName = multi.getParameter("sname");
			String singerName = multi.getParameter("aname");
			String surl = multi.getParameter("surl");
			
			// Null 확인
			if (songName == null || singerName == null || surl == null) {		
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, URLEncoder.encode("입력값은 Null이 포함되면 안 됩니다.", "UTF-8"));
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "입력값은 Null이 포함되면 안 됩니다."));
				
				databaseConnection.allClose();
				
				return;
			}
			
			songName = URLDecoder.decode(songName, "UTF-8");
			singerName = URLDecoder.decode(singerName, "UTF-8");
			surl = URLDecoder.decode(surl, "UTF-8");
			
			// 길이 확인
			if (songName.length() > 100 || singerName.length() > 50 || surl.length() > 200) {
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, URLEncoder.encode("입력한 값 중 1개 이상의 값이 제한된 길이를 넘어갑니다", "UTF-8"));
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "입력한 값 중 1개 이상의 값이 제한된 길이를 넘어갑니다"));
				
				databaseConnection.allClose();
				
				return;
			}
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { "songName", "singerName", "youtubeURL" },
																	  new String[] { songName, singerName, surl, }));
			// 정규식 확인
			final String emojis_regex = "(?:[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff])";
			songName = songName.replaceAll(emojis_regex, "");
			singerName = singerName.replaceAll(emojis_regex, "");
			surl = surl.replaceAll(emojis_regex, "");
			
			databaseConnection.closePreparedStatement();
			databaseConnection.setPreparedStatement("INSERT INTO utaite_add_song_v2(music_name, music_singer, youtuabe_url) VALUE (?, ?, ?);");
			databaseConnection.getPreparedStatement().setString(1, songName);
			databaseConnection.getPreparedStatement().setString(2, singerName);
			databaseConnection.getPreparedStatement().setString(3, surl);
			databaseConnection.executeUpdate();
			
			obj.addProperty(keyName_Result, successMessage);
			out.println(gson.toJson(obj));
			
			databaseConnection.allClose();
			
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "노래 신청 성공!"));
			
			return;
		}
		
		String songName = multi.getParameter("sname");
		String singerName = multi.getParameter("aname");
		String songWriterName = multi.getParameter("swname");
		String songTag = multi.getParameter("stag");
		String songLyrics = multi.getParameter("slyrics");

		// Null 확인
		if (songName == null || singerName == null || songWriterName == null || songTag == null || songLyrics == null) {		
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("입력값은 Null이 포함되면 안 됩니다.", "UTF-8"));
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "입력값은 Null이 포함되면 안 됩니다."));
			
			databaseConnection.allClose();
			
			return;
		}
		
		// UTF-8 디코딩
		songName = URLDecoder.decode(songName, "UTF-8");
		singerName = URLDecoder.decode(singerName, "UTF-8");
		songWriterName = URLDecoder.decode(songWriterName, "UTF-8");
		songTag = URLDecoder.decode(songTag, "UTF-8");
		songLyrics = URLDecoder.decode(songLyrics, "UTF-8");
		
		// 길이 확인
		if (songName.length() > 100 || singerName.length() > 50 || songWriterName.length() > 30 || songTag.length() > 100) {
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("입력한 값 중 1개 이상의 값이 제한된 길이를 넘어갑니다", "UTF-8"));
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "입력한 값 중 1개 이상의 값이 제한된 길이를 넘어갑니다"));
			
			databaseConnection.allClose();
			
			return;
		}
		
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { "songName", "singerName", "songWriterName", "songTag", "songLyrics" },
																  new String[] { songName, singerName, songWriterName, songTag, "<생략>" }));
		// 정규식 확인
		final String emojis_regex = "(?:[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff])";
		songName = songName.replaceAll(emojis_regex, "");
		singerName = singerName.replaceAll(emojis_regex, "");
		songWriterName = songWriterName.replaceAll(emojis_regex, "");
		songTag = songTag.replaceAll(emojis_regex, "");
		songLyrics = songLyrics.replaceAll(emojis_regex, "");
		
		databaseConnection.closePreparedStatement();
		databaseConnection.setPreparedStatement("INSERT INTO utaite_add_song(uuid, music_name, music_singer, music_songwriter, music_type, music_lyrics) VALUE (?, ?, ?, ?, ?, ?);");
		databaseConnection.getPreparedStatement().setString(1, fileNameToUUIDRenamePolicy.getUUID());
		databaseConnection.getPreparedStatement().setString(2, songName);
		databaseConnection.getPreparedStatement().setString(3, singerName);
		databaseConnection.getPreparedStatement().setString(4, songWriterName);
		databaseConnection.getPreparedStatement().setString(5, songTag);
		databaseConnection.getPreparedStatement().setString(6, songLyrics);
		databaseConnection.executeUpdate();
		
		obj.addProperty(keyName_Result, successMessage);
		out.println(gson.toJson(obj));
		
		databaseConnection.allClose();
		
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "노래 신청 성공!"));
	}else {
		obj.addProperty(keyName_Result, failMessage);
		obj.addProperty(keyName_Message, URLEncoder.encode("JSP 페이지 초기화 중 오류가 발생하였습니다.", "UTF-8"));
		out.println(gson.toJson(obj));
		
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
	}
}catch(Exception ex) {
	obj.addProperty(keyName_Result, failMessage);
	obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 오류가 발생 하였습니다. ".concat(ex.getMessage()), "UTF-8"));
	out.println(gson.toJson(obj));

	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
}
%>