package kro.kr.rhya_network.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import kro.kr.rhya_network.data_buffer.DataBufferManager;
import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.page.PageParameter;
import kro.kr.rhya_network.util.AuthTokenChecker;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.util.PathManager;
import kro.kr.rhya_network.util.ServiceAccessChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

/**
 * Servlet implementation class UtaitePlayerManager
 */
@WebServlet("/utaite_player_manager")
public class UtaitePlayerManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// MP3 파일 저장 경로
	private final String mp3SaveRootPath = PathManager.UTAITE_PLAYER_MANAGER_MP3_PATH;
	// JSON 반환 결과
	private final String successMessage = "success";
	private final String failMessage = "fail";
	
	
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UtaitePlayerManager() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		// Rhya 로거 변수 선언
		RhyaLogger rl = new RhyaLogger();
		// Rhya 로거 설정
		rl.JspName = request.getServletPath();
		rl.LogConsole = true;
		rl.LogFile = true;
		
		
		// 클라이언트 IP
		String clientIP = GetClientIPAddress.getClientIp(request);
		
		
		// JSON 결과
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		// JSON 변수
		Gson gson = new Gson();
		JsonObject obj = new JsonObject();

		
		// DB 관리자 선언
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		
		
		// 전체 예외 처리
		try {
			// 페이지 초기화
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Utaite_Player)) { // 페이지 초기화 성공
				// DB 접속
				try {
					databaseConnection.init();
					databaseConnection.connection();
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					databaseConnection = null;
					
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "데이터베이스 연결 중 오류 발생! ", e.toString()));
				}
				
				
				// Null 확인
				if (databaseConnection != null) {
					// Main 파라미터 추출
					int inputMode = Integer.parseInt(request.getParameter("mode"));
					// 로그 출력
					rl.Log(RhyaLogger.Type.Debug, rl.CreateLogTextv8(clientIP, "클라이언트가 해당 파라미터로 접속함 Mode:", Integer.toString(inputMode)));
					
					// 페이지 접근 확인
					if (!new ServiceAccessChecker().isAccessService(0)) {
						// 로그 출력
						rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서비스 접근 차단됨!"));
						
						obj.addProperty(keyName_Result, "service_access_block");
						
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						databaseConnection.allClose();
						
						return;
					}
					
					// 파라미터 구분
					switch (inputMode) {
						/**
						 * 알 수 없는 명령어
						 */
						default: {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "입력한 명령을 분석할 수 없습니다. Mode:", Integer.toString(inputMode)));
							
							obj.addProperty(keyName_Result, failMessage);
							obj.addProperty(keyName_Message, URLEncoder.encode("알 수 명령입니다. mode 파라미터를 확인해 주세요.", "UTF-8"));
							
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 0
						 * 
						 * 설명 :
						 * 		사용자 정보 가져오기
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 0: {
							// Auth token 확인
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // 로그인 성공
								// 데이터 존재 여부 확인
								databaseConnection.setPreparedStatement("SELECT * FROM utaite_user_info WHERE user_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.setResultSet();
								if (!databaseConnection.getResultSet().next()) {
									// 데이터 삽입
									databaseConnection.closeResultSet();
									databaseConnection.closePreparedStatement();
									databaseConnection.setPreparedStatement("INSERT INTO utaite_user_info VALUES (?, ?, ?, ?, ?);");
									databaseConnection.getPreparedStatement().setString(1, result[1]);
									databaseConnection.getPreparedStatement().setString(2, "{}");
									databaseConnection.getPreparedStatement().setString(3, "{\"list\": []}");
									databaseConnection.getPreparedStatement().setInt(4, 0);
									databaseConnection.getPreparedStatement().setString(5, "[null]");
									databaseConnection.executeUpdate();
									// JSON 데이터 설정 [ 기본 ]
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("uuid", URLEncoder.encode(result[1], "UTF-8"));
									obj.addProperty("play_list", URLEncoder.encode("{}", "UTF-8"));
									obj.addProperty("subscribe_list", URLEncoder.encode("{\"list\": []}", "UTF-8"));
									obj.addProperty("access_var", 0);
									obj.addProperty("access_date", URLEncoder.encode("[null]", "UTF-8"));
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 정보 출력 성공! (신규 사용자) Auth Token:", authToken));
								}else {
									// JSON 데이터 설정
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("uuid", URLEncoder.encode(databaseConnection.getResultSet().getString("user_uuid"), "UTF-8"));
									obj.addProperty("play_list", URLEncoder.encode(databaseConnection.getResultSet().getString("user_play_list"), "UTF-8"));
									obj.addProperty("subscribe_list", URLEncoder.encode(databaseConnection.getResultSet().getString("user_subscribe_list"), "UTF-8"));
									obj.addProperty("access_var", databaseConnection.getResultSet().getInt("user_access_var"));
									obj.addProperty("access_date", URLEncoder.encode(databaseConnection.getResultSet().getString("user_access_date"), "UTF-8"));
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 정보 출력 성공! Auth Token:", authToken));
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 정보 출력 실패! [로그인 실패] Auth Token:", authToken));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 1
						 * 
						 * 설명 :
						 * 		노래 정보 출력
						 * 
						 * 파라미터 :
						 * 	    - 이 중 선택해서 입력
						 *      ------------------------------
						 * 		new         --> 신규 노래 출력
						 * 		suuid       --> 특정 노래 출력
						 * 		all         --> 모든 노래 출력
						 * 		version     --> 모든 노래 버전 출력
						 * 		------------------------------
						 * 		auth        --> Auth Token
						 */
						case 1: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// 파라미터 추출
								String newSong = request.getParameter("new");
								String suuid = request.getParameter("suuid");
								String all = request.getParameter("all");
								String version = request.getParameter("version");
								
								// 파라미터 분류
								if (newSong != null) { // 최신 날자순
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list ORDER BY music_date DESC;");
									databaseConnection.setResultSet();

									// 노래 리스트
									// -----------------------------------------------
									int index = 1;
									while (databaseConnection.getResultSet().next()) {
										JsonObject obj1 = new JsonObject();
										obj1.addProperty(keyName_Result, successMessage);
										String uuid = databaseConnection.getResultSet().getString("music_uuid");
										obj1.addProperty("uuid", uuid);
										obj1.addProperty("version", databaseConnection.getResultSet().getInt("update_version"));
										obj.add(Integer.toString(index), obj1);
										index ++;
										
										if (index > 30) {
											break;
										}
									}
									if (index == 1) {
										JsonObject obj1 = new JsonObject();
										obj1.addProperty(keyName_Result, successMessage);
										obj1.addProperty("uuid", "[null]");
										obj1.addProperty("version", "[null]");
										obj.add(Integer.toString(index), obj1);
									}
									// -----------------------------------------------
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "최근에 추가된 노래 리스트 출력 성공! Auth Token:", authToken));
								}else if (suuid != null) { // 특정 노래
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list WHERE music_uuid = ?;");
									databaseConnection.getPreparedStatement().setString(1, suuid);
									databaseConnection.setResultSet();
									
									// 노래 리스트
									// -----------------------------------------------
									if (databaseConnection.getResultSet().next()) {
										obj.addProperty(keyName_Result, successMessage);
										obj.addProperty("uuid", databaseConnection.getResultSet().getString("music_uuid"));
										obj.addProperty("name", URLEncoder.encode(databaseConnection.getResultSet().getString("music_name"), "UTF-8"));
										obj.addProperty("time", URLEncoder.encode(databaseConnection.getResultSet().getString("music_time"), "UTF-8"));
										obj.addProperty("lyrics", URLEncoder.encode(databaseConnection.getResultSet().getString("music_lyrics"), "UTF-8"));
										obj.addProperty("lyrics_sub", URLEncoder.encode(databaseConnection.getResultSet().getString("music_lyrics_sub"), "UTF-8"));
										
										String[] resultSinger = getSingerNameAndImage(databaseConnection.getResultSet().getString("music_singer"));
										obj.addProperty("singeruuid", databaseConnection.getResultSet().getString("music_singer"));
										if (resultSinger == null) {
											obj.addProperty("singer", URLEncoder.encode("[null]", "UTF-8"));
											obj.addProperty("singerimage", URLEncoder.encode("[null]", "UTF-8"));
										}else {
											obj.addProperty("singer", URLEncoder.encode(resultSinger[0], "UTF-8"));
											obj.addProperty("singerimage", URLEncoder.encode(resultSinger[1], "UTF-8"));
										}
										
										obj.addProperty("songwriter", URLEncoder.encode(databaseConnection.getResultSet().getString("music_songwriter"), "UTF-8"));
										obj.addProperty("image", URLEncoder.encode(databaseConnection.getResultSet().getString("music_image"), "UTF-8"));
										obj.addProperty("mp3", URLEncoder.encode(databaseConnection.getResultSet().getString("music_mp3"), "UTF-8"));
										obj.addProperty("type", URLEncoder.encode(databaseConnection.getResultSet().getString("music_type"), "UTF-8"));
										obj.addProperty("date", URLEncoder.encode(databaseConnection.getResultSet().getString("music_date"), "UTF-8"));
										obj.addProperty("version", databaseConnection.getResultSet().getInt("update_version"));
									}else {
										obj.addProperty(keyName_Result, successMessage);
										obj.addProperty("uuid", "[null]");
										obj.addProperty("name", "[null]");
										obj.addProperty("time", "[null]");
										obj.addProperty("lyrics", "[null]");
										obj.addProperty("lyrics_sub", "[null]");
										obj.addProperty("singer",  "[null]");
										obj.addProperty("singeruuid",  "[null]");
										obj.addProperty("singerimage",  "[null]");
										obj.addProperty("songwriter", "[null]");
										obj.addProperty("image", "[null]");
										obj.addProperty("mp3", "[null]");
										obj.addProperty("type", "[null]");
										obj.addProperty("date", "[null]");
										obj.addProperty("version", "[null]");
									}
									// -----------------------------------------------
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "특정 노래 정보 출력 성공! Auth Token:", authToken));
								}else if (all != null) {
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list;");
									databaseConnection.setResultSet();

									// 노래 리스트
									// -----------------------------------------------
									int index = 1;
									while (databaseConnection.getResultSet().next()) {
										JsonObject obj1 = new JsonObject();
										obj1.addProperty(keyName_Result, successMessage);
										String uuid = databaseConnection.getResultSet().getString("music_uuid");
										obj1.addProperty("uuid", uuid);
										obj1.addProperty("name", URLEncoder.encode(databaseConnection.getResultSet().getString("music_name"), "UTF-8"));
										obj1.addProperty("time", URLEncoder.encode(databaseConnection.getResultSet().getString("music_time"), "UTF-8"));
										obj1.addProperty("lyrics", URLEncoder.encode(databaseConnection.getResultSet().getString("music_lyrics"), "UTF-8"));
										obj1.addProperty("lyrics_sub", URLEncoder.encode(databaseConnection.getResultSet().getString("music_lyrics_sub"), "UTF-8"));
										
										String[] resultSinger = getSingerNameAndImage(databaseConnection.getResultSet().getString("music_singer"));
										obj1.addProperty("singeruuid", databaseConnection.getResultSet().getString("music_singer"));
										if (resultSinger == null) {
											obj1.addProperty("singer", URLEncoder.encode("[null]", "UTF-8"));
											obj1.addProperty("singerimage", URLEncoder.encode("[null]", "UTF-8"));
										}else {
											obj1.addProperty("singer", URLEncoder.encode(resultSinger[0], "UTF-8"));
											obj1.addProperty("singerimage", URLEncoder.encode(resultSinger[1], "UTF-8"));
										}
										
										obj1.addProperty("songwriter", URLEncoder.encode(databaseConnection.getResultSet().getString("music_songwriter"), "UTF-8"));
										obj1.addProperty("image", URLEncoder.encode(databaseConnection.getResultSet().getString("music_image"), "UTF-8"));
										obj1.addProperty("mp3", URLEncoder.encode(databaseConnection.getResultSet().getString("music_mp3"), "UTF-8"));
										obj1.addProperty("type", URLEncoder.encode(databaseConnection.getResultSet().getString("music_type"), "UTF-8"));
										obj1.addProperty("date", URLEncoder.encode(databaseConnection.getResultSet().getString("music_date"), "UTF-8"));
										obj1.addProperty("version", databaseConnection.getResultSet().getInt("update_version"));
										obj.add(Integer.toString(index), obj1);
										index ++;
									}
									
									if (index == 1) {
										JsonObject obj1 = new JsonObject();
										obj1.addProperty(keyName_Result, successMessage);
										obj1.addProperty("uuid", "[null]");
										obj1.addProperty("name", "[null]");
										obj1.addProperty("time", "[null]");
										obj1.addProperty("lyrics", "[null]");
										obj1.addProperty("lyrics_sub", "[null]");
										obj1.addProperty("singer",  "[null]");
										obj1.addProperty("singeruuid",  "[null]");
										obj1.addProperty("singerimage",  "[null]");
										obj1.addProperty("songwriter", "[null]");
										obj1.addProperty("image", "[null]");
										obj1.addProperty("mp3", "[null]");
										obj1.addProperty("type", "[null]");
										obj1.addProperty("date", "[null]");
										obj1.addProperty("version", "[null]");
										obj.add(Integer.toString(index), obj1);
									}
									// -----------------------------------------------
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "모든 노래 정보 출력 성공! Auth Token:", authToken));
								}else if (version != null) { // 버전 확인
									databaseConnection.setPreparedStatement("SELECT music_uuid, update_version FROM utaite_list;");
									databaseConnection.setResultSet();

									// 노래 리스트
									// -----------------------------------------------
									int index = 1;
									while (databaseConnection.getResultSet().next()) {
										JsonObject obj1 = new JsonObject();
										obj1.addProperty(keyName_Result, successMessage);
										obj1.addProperty("uuid", databaseConnection.getResultSet().getString("music_uuid"));
										obj1.addProperty("version", databaseConnection.getResultSet().getInt("update_version"));
										obj.add(Integer.toString(index), obj1);
										index ++;
									}
									// -----------------------------------------------
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "모든 노래 버전 정보 출력 성공! Auth Token:", authToken));
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "입력한 명령을 분석할 수 없습니다. [노래 정보 출력] Auth Token: ", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "모든 노래 버전 정보 출력 실패! [로그인 실패]  Auth Token:", authToken));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 2
						 * 
						 * 설명 :
						 * 		노래 정보 용량 출력
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 2: {
							databaseConnection.setPreparedStatement("SELECT table_name, round(data_length/(1024*1024),2) as 'DATA_SIZE(MB)' FROM information_schema.TABLES where table_schema = 'rhya_network_server' AND TABLE_NAME = 'utaite_list';");
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("size", databaseConnection.getResultSet().getString("DATA_SIZE(MB)"));
							}else {
								obj.addProperty(keyName_Result, failMessage);
								obj.addProperty("size", "0");
							}
							
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "노래 정보 용량 출력 성공!"));
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 3
						 * 
						 * 설명 :
						 * 		사용자 데이터 설정
						 * 
						 * 파라미터 :
						 * 		index       --> 데이터 종류
						 * 		value       --> 데이터
						 * 		auth        --> Auth Token
						 */
						case 3: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // 로그인 성공
								// 파라미터 추출
								int index = Integer.parseInt(request.getParameter("index"));
								String value = request.getParameter("value");	
								// SQL
								String runSQL = "UPDATE utaite_user_info SET <KEY> = ? WHERE user_uuid = ?;";
								if (index == 0) runSQL = runSQL.replace("<KEY>", "user_play_list");
								else runSQL = runSQL.replace("<KEY>", "user_subscribe_list");
								
								// JSON 형식 확인
								try {
									JsonParser parser = new JsonParser();
									parser.parse(URLDecoder.decode(value, "UTF-8"));
									parser = null;
									
									databaseConnection.setPreparedStatement(runSQL);
									databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(value, "UTF-8"));
									databaseConnection.getPreparedStatement().setString(2, result[1]);
									databaseConnection.executeUpdate();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "사용자 데이터 변경 성공! - ", "Value:", value, "| Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
								}catch(JsonSyntaxException ex) {
									ex.printStackTrace();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "사용자 데이터 변경 실패! 알 수 없는 JSON 형식 Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 JSON 형식입니다.", "UTF-8"));
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 데이터 변경 실패! [로그인 실패] Auth Token:", authToken));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 4
						 * 
						 * 설명 :
						 * 		노래 재생
						 * 
						 * 파라미터 :
						 * 		uuid        --> 노래 UUID
						 * 		auth        --> Auth Token
						 */
						case 4: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// 파라미터 추출
								String uuid = request.getParameter("uuid");
								// Header
						        Long startRange = 0l;
						        Long endRange = 0l;
						        Boolean isPartialRequest = false;
						        try {
						            if(request.getHeader("range") != null) {
						                String rangeStr = request.getHeader("range");
						                String[] range = rangeStr.replace("bytes=", "").split("-");
						                startRange = range[0] != null ? Long.parseLong(range[0]) : 0l;
						                isPartialRequest = true;
						            }
						        } catch(NullPointerException | ArrayIndexOutOfBoundsException e) {
						            System.err.println(e);
						        }
						        
						        // 노래 DB 검색
								databaseConnection.setPreparedStatement("SELECT music_mp3 from utaite_list where music_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(uuid, "UTF-8"));
								databaseConnection.setResultSet();
								
								StringBuilder sb = new StringBuilder();
								sb.append(mp3SaveRootPath);
								sb.append(File.separator);
								
								if (databaseConnection.getResultSet().next()) {
									sb.append(databaseConnection.getResultSet().getString("music_mp3"));
									sb.append(".mp3");
									
									try {
									 	File mp3 = new File(sb.toString());
									 	// Set response headers
								        response.setContentType("application/octet-stream");
									 	response.setHeader("Accept-Ranges", "bytes");
									 	response.setHeader("Content-Disposition", "attachment; filename=" + uuid);
								        response.setHeader("Content-Transfer-Encoding", "binary;");


								 		StringBuilder headerContentRangeWriter = new StringBuilder();
								 		
							 			endRange = mp3.length();
								 		
									 	if (isPartialRequest) {
								        	int len = (int) mp3.length();
									 		headerContentRangeWriter.append("bytes ");
									 		headerContentRangeWriter.append(startRange);
									 		headerContentRangeWriter.append("-");
									 		headerContentRangeWriter.append(endRange);
									 		headerContentRangeWriter.append("/");
									 		headerContentRangeWriter.append(len);

								            response.setHeader("Content-Range", headerContentRangeWriter.toString());
								        } else {
								        	int len = (int) mp3.length();
									 		headerContentRangeWriter.append("bytes ");
									 		headerContentRangeWriter.append("0-");
									 		headerContentRangeWriter.append(len);
									 		headerContentRangeWriter.append("/");
									 		headerContentRangeWriter.append(len);
									 		
								            response.setHeader("Content-Length", Integer.toString(len));
								            response.setHeader("Content-Range", headerContentRangeWriter.toString());
								            startRange = 0l;
								        }
									 	
									 	// 랜덤 액세스 파일을 이용해 mp3 파일을 범위로 읽기
								        try (RandomAccessFile randomAccessFile = new RandomAccessFile(mp3, "r");
								            ServletOutputStream sos = response.getOutputStream();){
								        	
								            Integer bufferSize = 1024, data = 0;
								            byte[] b = new byte[bufferSize];
								            Long count = startRange;
								            Long requestSize = endRange - startRange + 1;

								            // startRange에서 출발
								            randomAccessFile.seek(startRange);

								            while (true) {
								                // 버퍼 사이즈 (1024) 보다 범위가 작으면
								                if(requestSize <= 2) {
								                    // Range byte 0-1은 아래 의미가 아님.
								                    // data = randomAccessFile.read(b, 0, requestSize.intValue());
								                    // sos.write(b, 0, data);

								                    // ** write 없이 바로 flush ** //
								                    sos.flush();
								                    break;
								                }

								                // 나머지는 일반적으로 진행
								                data = randomAccessFile.read(b, 0, b.length);

								                // count가 endRange 이상이면 요청 범위를 넘어선 것이므로 종료
								                if(count <= endRange) {
								                    sos.write(b, 0, data);
								                    count += bufferSize;
								                    randomAccessFile.seek(count);
								                } else {
								                    break;
								                }

								            }

								            sos.flush();
								        }
									 	
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "노래 재생 성공!", "Music UUID:", uuid, " ,Auth Token:", authToken));
									}catch (IOException ioe) {
										ioe.printStackTrace();
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "노래 재생 실패! [IOException]", "Auth Token:" , authToken, " -", ioe.toString()));
									}
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "노래 재생 실패! [노래 UUID를 찾을 수 없음] Auth Token:", authToken));
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "노래 재생 실패! [로그인 실패] Auth Token:", authToken));
							}

							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 5
						 * 
						 * 설명 :
						 * 		사용자 우타이테 플레이어 접근 허용 확인
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 5: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 서비스 접근 허용 Auth Token:", authToken));
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, successMessage);
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 서비스 접근 거부 Auth Token:", authToken));
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
				
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 6
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 앱 정보 출력
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 6: {
							// 로그 출력
							databaseConnection.setPreparedStatement("SELECT * FROM utaite_info;");
							databaseConnection.setResultSet();
							if (databaseConnection.getResultSet().next()) {
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("version", databaseConnection.getResultSet().getString("version"));
								obj.addProperty("version_for_windows", databaseConnection.getResultSet().getString("version_for_windows"));
								obj.addProperty("key", databaseConnection.getResultSet().getString("app_sign_key"));
								obj.addProperty("update_description", URLEncoder.encode(databaseConnection.getResultSet().getString("update_description"), "UTF-8"));
								obj.addProperty("update_description_for_windows", URLEncoder.encode(databaseConnection.getResultSet().getString("update_description_for_windows"), "UTF-8"));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 앱 정보 출력 성공"));	
							}else {
								obj.addProperty(keyName_Result, failMessage);
								obj.addProperty("key", "[null]");
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 앱 정보 출력 실패"));
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 7
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 앱 다운로드
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 7: {
							// 파일 업로드된 경로
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_APK_PATH;
						    // 실제 내보낼 파일명
						    String orgfilename = "up_update_apk.apk";    
							try {
								File file = new File(root);
								if (file.isFile()) {
									byte b[] = new byte[(int) file.length()];
									orgfilename = java.net.URLEncoder.encode(orgfilename, "UTF-8");
									response.setHeader("Content-Disposition", "attachment;filename=".concat(orgfilename));
									response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
									response.setContentLength((int) file.length());
									
									BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
									BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
									int read = 0;
									while ((read = fin.read(b)) != -1) {
										outs.write(b, 0, read);
									}
									outs.close();
									fin.close();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 APK 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 APK 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 APK 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 8
						 * 
						 * 설명 :
						 * 		MP3 파일 다운로드
						 * 
						 * 파라미터 :
						 * 		uuid        --> 노래 UUID
						 * 		auth        --> Auth Token
						 */
						case 8: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// 파라미터 추출
								String uuid = request.getParameter("uuid");
								databaseConnection.setPreparedStatement("SELECT music_mp3 from utaite_list where music_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(uuid, "UTF-8"));
								databaseConnection.setResultSet();
								
								StringBuilder sb = new StringBuilder();
								sb.append(mp3SaveRootPath);
								sb.append(File.separator);
								
								if (databaseConnection.getResultSet().next()) {	
									sb.append(databaseConnection.getResultSet().getString("music_mp3"));
									sb.append(".mp3");
									
									try {
										File file = new File(sb.toString());
										if (file.isFile()) {
											byte b[] = new byte[(int) file.length()];
											response.setHeader("Content-Disposition", "attachment;filename=".concat(file.getName()));
											response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
											response.setContentLength((int) file.length());
											
											BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
											BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
											int read = 0;
											while ((read = fin.read(b)) != -1) {
												outs.write(b, 0, read);
											}
											outs.close();
											fin.close();
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "우타이테 플레이어 MP3 파일 다운로드 성공! [UUID] ", "Auth Token:", authToken, ",Music UUID:", uuid));
										} else {
											// 로그 출력
											rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv10(clientIP, "우타이테 플레이어 MP3 파일 다운로드 실패! File does not exist!", "Auth Token:", authToken, ",Music UUID:", uuid));
											
											obj.addProperty(keyName_Result, failMessage);
											obj.addProperty(keyName_Message, URLEncoder.encode("우타이테 플레이어 MP3 파일 다운로드 실패! File does not exist!", "UTF-8"));
											
											PrintWriter out = response.getWriter(); 
											out.println(gson.toJson(obj));
										}
									} catch (IOException ioe) {
										ioe.printStackTrace();
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "우타이테 플레이어 MP3 파일 다운로드 실패! [IOException]", "Auth Token:" , authToken, " -", ioe.toString()));
									}
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 MP3 파일 다운로드 실패! [노래 UUID를 찾을 수 없음] Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 MP3 파일 다운로드 실패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 9
						 * 
						 * 설명 :
						 * 		특정 폴더 용량 출력
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 9: {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("size", getFolderSize(new File(mp3SaveRootPath)));
							
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 MP3 파일 사이즈"));
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 10
						 * 
						 * 설명 :
						 * 		서버 정보 출력
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 10: {
							String[] getInfo = isServerAccessCheck();
							
							if (getInfo != null) {
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("version", getInfo[0]);
								obj.addProperty("access", getInfo[1]);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "서버 정보 출력 성공!"));
							}else {
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서버 정보 출력 실패! 알 수 없는 오류가 발생하였습니다."));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 11
						 * 
						 * 설명 :
						 * 		노래 재생 횟수 반영
						 * 
						 * 파라미터 :
						 * 		uuid        --> 노래 UUID
						 * 		auth        --> Auth Token
						 */
						case 11: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// 파라미터 추출
								String uuid = request.getParameter("music");
								// DB 접속
								databaseConnection.setPreparedStatement("UPDATE utaite_list set play_count = play_count + 1 where music_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, uuid);
								databaseConnection.executeUpdate();
								databaseConnection.closePreparedStatement();
								databaseConnection.setPreparedStatement("INSERT INTO utaite_user_play_count (user_uuid, music_uuid, COUNT) VALUE (?, ?, 1) ON DUPLICATE KEY UPDATE COUNT = COUNT + 1");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.getPreparedStatement().setString(2, uuid);
								databaseConnection.executeUpdate();
								
								if (databaseConnection.executeUpdate() >= 1) {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "노래 재생 횟수 반영 성공!", "Auth Token:", authToken, ",Music UUID:", uuid));
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "노래 재생 횟수 반영 실패! 수정된 노래 정보 개수가  0개 입니다. Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "노래 재생 횟수 반영 실패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 12
						 * 
						 * 설명 :
						 * 		재생 횟수 기반 Top8 노래 리스트
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 12: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// DB 접속
								databaseConnection.setPreparedStatement("SELECT music_uuid, play_count, music_date from utaite_list ORDER BY play_count DESC LIMIT 8;");
								databaseConnection.setResultSet();
								
								boolean isChecker = false;
								int index = 1;
								
								while (databaseConnection.getResultSet().next()) {
									isChecker = true;
									
									JsonObject obj1 = new JsonObject();
									obj1.addProperty(keyName_Result, successMessage);
									obj1.addProperty("uuid", databaseConnection.getResultSet().getString("music_uuid"));
									obj1.addProperty("play_count", databaseConnection.getResultSet().getInt("play_count"));
									obj.add(Integer.toString(index), obj1);
									index ++;
								}
								
								if (isChecker) {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "노래 순위 출력 성공! Auth Token:", authToken));
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "노래 순위 출력 살패! 검색된 내용이 없습니다. Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "노래 순위 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 13
						 * 
						 * 설명 :
						 * 		사용자 정보 수정 [구독]
						 * 
						 * 파라미터 :
						 * 		index       --> 구독 취소, 구독
						 * 		value       --> 데이터
						 * 		auth        --> Auth Token
						 */
						case 13: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // 로그인 성공
								// 파라미터 추출
								int index = Integer.parseInt(request.getParameter("index"));
								String value = request.getParameter("value");	
								// SQL
								String runSQL = "UPDATE utaite_user_info SET user_subscribe_list = ? WHERE user_uuid = ?;";
								
								databaseConnection.setPreparedStatement("SELECT singer_uuid FROM utaite_singer;");
								databaseConnection.setResultSet();
								boolean isExt = false;
								while (databaseConnection.getResultSet().next()) {
									if (databaseConnection.getResultSet().getString("singer_uuid").equals(value)) {
										isExt = true;
										break;
									}
								}
								
								if (isExt) {
									// JSON 형식 확인
									try {
										databaseConnection.closePreparedStatement();
										databaseConnection.closeResultSet();
										databaseConnection.setPreparedStatement("SELECT user_subscribe_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										
										if (databaseConnection.getResultSet().next()) {
											JsonParser parser = new JsonParser();
											
											JsonObject rootJSONBoject = (JsonObject) parser.parse(databaseConnection.getResultSet().getString("user_subscribe_list"));
											JsonArray jsonArray = (JsonArray) rootJSONBoject.get("list");
											if (index == 0) {
												boolean isChecker = false;
												for (int i = 0 ; i < jsonArray.size(); i ++) {
													if (jsonArray.get(i).getAsString().equals(value)) {
														isChecker = true;
														break;
													}
												}
												
												if (!isChecker) {
													jsonArray.add(value);
												}
											}else {
												boolean isChecker = false;
												int indexRemove = -1;
												
												for (int i = 0 ; i < jsonArray.size(); i ++) {
													if (jsonArray.get(i).getAsString().equals(value)) {
														isChecker = true;
														indexRemove = i;
														break;
													}
												}
												
												if (isChecker) {
													jsonArray.remove(indexRemove);
												}
											}
											
											rootJSONBoject.remove("list");
											rootJSONBoject.add("list", jsonArray);
											
											databaseConnection.closePreparedStatement();
											databaseConnection.closeResultSet();
											databaseConnection.setPreparedStatement(runSQL);
											databaseConnection.getPreparedStatement().setString(1, rootJSONBoject.toString());
											databaseConnection.getPreparedStatement().setString(2, result[1]);
											databaseConnection.executeUpdate();
	
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "사용자 데이터 변경 성공! [구독 정보 설정] ,Index:", String.valueOf(index), ",Value:", value, ",Auth Token", authToken));
											
											obj.addProperty(keyName_Result, successMessage);
										}else {
											// 로그 출력
											rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "사용자 데이터 변경 실패! [구독 정보 설정] [사용자 조회 실패] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
											
											obj.addProperty(keyName_Result, failMessage);
											obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 JSON 형식입니다.", "UTF-8"));
										}
									}catch(JsonSyntaxException ex) {
										ex.printStackTrace();
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "사용자 데이터 변경 실패! [구독 정보 설정] [알 수 없는 JSON 형식] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
										
										obj.addProperty(keyName_Result, failMessage);
										obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 JSON 형식입니다.", "UTF-8"));
									}
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "사용자 데이터 변경 실패! [구독 정보 설정] [알 수 없는 SINGER UUID] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 UUID 입니다.", "UTF-8"));
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 데이터 변경 실패! [구독 정보 설정] [로그인 실패] Auth Token:", authToken));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 14
						 * 
						 * 설명 :
						 * 		사용자 정보 수정 [플레이리스트]
						 * 
						 * 파라미터 :
						 * 		index       --> 명령어 타입
						 * 			            0 : 플레이리스트 삭제
						 * 					    1 : 플레이리스트 생성
						 * 					    2 : 플레이리스트 수정
						 * 					    3 : 플레이리스트 노래 수정
						 * 		value1      --> 데이터 1
						 * 		value2      --> 데이터 2
						 * 		value3      --> 데이터 3
						 * 		auth        --> Auth Token
						 */
						case 14: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // 로그인 성공
								// 파라미터 추출
								final String IMAGE_TYPE = "_IMAGE_TYPE_";
								final String NAME_TYPE = "_NAME_";
								int index = Integer.parseInt(request.getParameter("index"));
								String value1 = request.getParameter("value1");	
								String value2 = request.getParameter("value2");	
								String value3 = request.getParameter("value3");	
								value1 = URLDecoder.decode(value1,"UTF-8");
								value2 = URLDecoder.decode(value2,"UTF-8");
								value3 = URLDecoder.decode(value3,"UTF-8");
							
								
								// Index 분석
								switch (index) {
									/**
									 * 알 수 없는 명령어
									 */
									default:
										// JSON 데이터 설정
										obj.addProperty(keyName_Result, failMessage);
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [알 수 없는 INDEX] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										break;
								
									/**
									 * 플레이리스트 삭제
									 */
									case 0: {
										// 플레이리스트 확인
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											// JSON 데이터 파싱
											String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
											JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
											Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
											// 데이터 제거 확인
											boolean isExt = false;
											for (Map.Entry<String, JsonElement> entry: entries) {
											    if (value1.equals(entry.getKey())) {
											    	isExt = true;
											    	break;
											    }
											}
											// 데이터 제거
											if (isExt) {
												jsonRootObject.remove(value1);
												
												// 적용
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonRootObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
											}
											
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, successMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 삭제] ,Index:", String.valueOf(index), ",플레이리스트 이름:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}else {
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, failMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 삭제 실패 - 플레이리스트 조회 불가] ,Index:", String.valueOf(index), ",플레이리스트 이름:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									/**
									 * 플레이리스트 생성
									 */
									case 1: {
										// 파라미터 확인
										if (!value2.contains(IMAGE_TYPE)) {
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, failMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 생성 실패 - IMAGE_TYPE을 지정하지 않음] ,Index:", String.valueOf(index), ",플레이리스트 이름:", value1, ",플레이리스트 이미지:", value2, ",Value3:", value3, "Auth Token", authToken));
										}else {
											// 플레이리스트 확인
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON 데이터 파싱
												UUID uuid = UUID.randomUUID();
												String uuidStr = uuid.toString();
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												JsonArray jsonArray = new JsonArray();
												jsonArray.add(NAME_TYPE.concat(value1));
												jsonArray.add(value2);
												jsonRootObject.add(uuidStr, jsonArray);
												
												// 적용
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonRootObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, successMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 생성] ,Index:", String.valueOf(index), ",플레이리스트 이름:", value1, ",플레이리스트 이미지:", value2, ",Value3:", value3, "Auth Token", authToken));
											}else {
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, failMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 생성 실패 - 플레이리스트 조회 불가] ,Index:", String.valueOf(index), ",플레이리스트 이름:", value1, ",플레이리스트 이미지:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}
										
										break;
									}
									
									
									/**
									 * 플레이리스트 수정 [기본 정보]
									 */
									case 2: {
										// 수정 데이터 구분
										if (value1.equalsIgnoreCase("name")) { // 플레이리스트 이름
											// 플레이리스트 확인
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON 데이터 파싱
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
												// 데이터 수정
												JsonObject jsonSubObject = new JsonObject();
												for (Map.Entry<String, JsonElement> entry : entries) {
													if (value2.equals(entry.getKey())) {
														JsonArray getArray = (JsonArray) entry.getValue();
														JsonArray getNewArray = new JsonArray();
														
														for (int i = 0 ; i  < getArray.size(); i ++) {
															if (getArray.get(i).toString().contains(NAME_TYPE) && value3.length() <= 40) {
																getNewArray.add(NAME_TYPE.concat(value3));
																
																continue;
															}
															
															getNewArray.add(getArray.get(i));
														}
														
														jsonSubObject.add(entry.getKey(), getNewArray);
													}else {
														jsonSubObject.add(entry.getKey(), entry.getValue());
													}
												}
												// 적용
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, successMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 수정] ,Index:", String.valueOf(index), ",수정될 데이터:", value1, ",플레이리스트 이름:", value2, ",수정 데이터 값:", value3, "Auth Token", authToken));
											}else {
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, failMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 수정 실패 - 플레이리스트 조회 불가] ,Index:", String.valueOf(index), ",수정될 데이터:", value1, ",플레이리스트 이름:", value2, ",수정 데이터 값:", value3, "Auth Token", authToken));
											}
										}else if (value1.equalsIgnoreCase("image")) { // 플레이리스트 이미지
											// 플레이리스트 확인
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON 데이터 파싱
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
												// 데이터 수정
												JsonObject jsonSubObject = new JsonObject();
												for (Map.Entry<String, JsonElement> entry : entries) {
												    if (value2.equals(entry.getKey())) {
														JsonArray jsonArray = new JsonArray();
												    	JsonArray array = (JsonArray) entry.getValue();
												    	for (int indexArray = 0 ; indexArray < array.size(); indexArray++) {
												    		if (array.get(indexArray).toString().contains(IMAGE_TYPE) && value3.contains(IMAGE_TYPE)) {
													    		jsonArray.add(value3);
												    			
												    			continue;
												    		}
												    		
												    		jsonArray.add(array.get(indexArray));
												    	}
												    	
													    jsonSubObject.add(entry.getKey(), jsonArray);
												    }else {
													    jsonSubObject.add(entry.getKey(), entry.getValue());
												    }
												}
												// 적용
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, successMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 수정(Image)] ,Index:", String.valueOf(index), ",수정될 데이터:", value1, ",플레이리스트 이름:", value2, ",수정 데이터 값:", value3, "Auth Token", authToken));
											}else {
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, failMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 수정 실패 - 플레이리스트 조회 불가] ,Index:", String.valueOf(index), ",수정될 데이터:", value1, ",플레이리스트 이름:", value2, ",수정 데이터 값:", value3, "Auth Token", authToken));
											}
										}else { // 알 수 없는 데이터
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, failMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 수정 실패 - 데이터 선택 오류] ,Index:", String.valueOf(index), ",수정될 데이터:", value1, ",플레이리스트 이름:", value2, ",수정 데이터 값:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									
									/**
									 * 플레이리스트 노래 데이터 변경 [No Data-Buffer]
									 */
									case 3: {
										// 플레이리스트 확인
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											try {
												// JSON 데이터 파싱
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonParser jsonParser = new JsonParser();
												JsonObject jsonRootObject = (JsonObject) jsonParser.parse(jsonValue);
												JsonArray array = (JsonArray) jsonRootObject.getAsJsonArray(value1);
												JsonArray newJsonArray = new JsonArray();
												JsonArray inputArrayForSongList = (JsonArray) jsonParser.parse(value2);
												for (int indexArray = 0 ; indexArray < array.size(); indexArray++) {
										    		if (array.get(indexArray).toString().contains(IMAGE_TYPE) || array.get(indexArray).toString().contains(NAME_TYPE)) {
										    			newJsonArray.add(array.get(indexArray));
										    			
										    			if (newJsonArray.size() >= 2) break;
										    			
										    			continue;
										    		}
										    	}	
												

												for (int i = 0; i < inputArrayForSongList.size(); i++) {
													newJsonArray.add(inputArrayForSongList.get(i));
												}

												
												Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
												// 데이터 수정
												JsonObject jsonSubObject = new JsonObject();
												for (Map.Entry<String, JsonElement> entry : entries) {
												    if (value1.equals(entry.getKey())) {
													    jsonSubObject.add(entry.getKey(), newJsonArray);
												    }else {
													    jsonSubObject.add(entry.getKey(), entry.getValue());
												    }
												}
												
												// 적용
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, successMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (No Data-Buffer) 성공!] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}catch (Exception e) {
												// TODO: handle exception
												e.printStackTrace();
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, failMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (No Data-Buffer) 실패 - JSON 데이터 변환 실페] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}else {
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, failMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (No Data-Buffer) 실패 - 데이터 조회 불가] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									
									/**
									 * 플레이리스트 노래 데이터 변경 [Use Data-Buffer]
									 */
									case 4: {
										// 플레이리스트 확인
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											try {
												// JSON 데이터 파싱
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonParser jsonParser = new JsonParser();
												JsonObject jsonRootObject = (JsonObject) jsonParser.parse(jsonValue);
												JsonArray newJsonArray = new JsonArray();
												
												DataBufferManager bufferManager = new DataBufferManager();
												String requestValue = bufferManager.getBuffer(value1);

												
												if (requestValue == null) {
													// JSON 데이터 설정
													obj.addProperty(keyName_Result, failMessage);
													
													// 로그 출력
													rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (Use Data-Buffer) 실패 - NotFoundRequestCode] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
												}else {
													JsonArray array = (JsonArray) jsonRootObject.getAsJsonArray(value2);
													JsonArray inputArrayForSongList = (JsonArray) jsonParser.parse(requestValue);
													for (int indexArray = 0 ; indexArray < array.size(); indexArray++) {
											    		if (array.get(indexArray).toString().contains(IMAGE_TYPE) || array.get(indexArray).toString().contains(NAME_TYPE)) {
											    			newJsonArray.add(array.get(indexArray));
											    			
											    			if (newJsonArray.size() >= 2) break;
											    			
											    			continue;
											    		}
											    	}	
													
													
													for (int i = 0; i < inputArrayForSongList.size(); i++) {
														newJsonArray.add(inputArrayForSongList.get(i));
													}

													
													Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
													// 데이터 수정
													JsonObject jsonSubObject = new JsonObject();
													for (Map.Entry<String, JsonElement> entry : entries) {
													    if (value2.equals(entry.getKey())) {
														    jsonSubObject.add(entry.getKey(), newJsonArray);
													    }else {
														    jsonSubObject.add(entry.getKey(), entry.getValue());
													    }
													}

													// 적용
													databaseConnection.closePreparedStatement();
													databaseConnection.closeResultSet();
													databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
													databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
													databaseConnection.getPreparedStatement().setString(2, result[1]);
													databaseConnection.executeUpdate();
													
													// JSON 데이터 설정
													obj.addProperty(keyName_Result, successMessage);
													
													// 로그 출력
													rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 성공! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (Use Data-Buffer) 성공!] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
												}
											}catch (Exception e) {
												// TODO: handle exception
												e.printStackTrace();
												
												// JSON 데이터 설정
												obj.addProperty(keyName_Result, failMessage);
												
												// 로그 출력
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (Use Data-Buffer) 실패 - JSON 데이터 변환 실페] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}else {
											// JSON 데이터 설정
											obj.addProperty(keyName_Result, failMessage);
											
											// 로그 출력
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [플레이리스트 노래 정보 수정 (Use Data-Buffer) 실패 - 데이터 조회 불가] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 데이터 변경 실패! [플레이리스트 정보 설정] [로그인 실패] Auth Token:", authToken));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 15
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 프로그램 [PC - Windows] 다운로드 For ZIP
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 15: {
							// 파일 업로드된 경로
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_ZIP_PATH;
						    // 실제 내보낼 파일명
						    String orgfilename = "up_update_zip.zip";    
							try {
								File file = new File(root);
								if (file.isFile()) {
									byte b[] = new byte[(int) file.length()];
									orgfilename = java.net.URLEncoder.encode(orgfilename, "UTF-8");
									response.setHeader("Content-Disposition", "attachment;filename=".concat(orgfilename));
									response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
									response.setContentLength((int) file.length());
									
									BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
									BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
									int read = 0;
									while ((read = fin.read(b)) != -1) {
										outs.write(b, 0, read);
									}
									outs.close();
									fin.close();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 ZIP[WINDOWS] 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 ZIP[WINDOWS] 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 ZIP[WINDOWS] 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								e.printStackTrace();
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 16
						 * 
						 * 설명 :
						 * 		니코니코동 순위 출력
						 * 
						 * 파라미터 :
						 * 		uuid        --> 노래 UUID
						 */
						case 16: {  
							// 데이터 존재 유무 확인
							databaseConnection.setPreparedStatement("SELECT * FROM utaite_niconico_rank ORDER BY rank ASC");
							databaseConnection.setResultSet();

							JsonArray rankResult = new JsonArray();
							
							while (databaseConnection.getResultSet().next()) {
								JsonObject rank = new JsonObject();
								rank.addProperty("contentId", databaseConnection.getResultSet().getString("contentId"));
								rank.addProperty("title", databaseConnection.getResultSet().getString("title"));
								rank.addProperty("viewCounter", databaseConnection.getResultSet().getInt("viewCounter"));
								rank.addProperty("likeCounter", databaseConnection.getResultSet().getInt("likeCounter"));
								rank.addProperty("rank", databaseConnection.getResultSet().getInt("rank"));
								
								rankResult.add(rank);
							}
							
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("message", java.net.URLEncoder.encode(rankResult.toString(), "UTF-8"));
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "니코니코동 순위 출력 성공!"));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 17
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 EXE 다운로드
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 17: {
							// 파일 업로드된 경로
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_EXE_PATH;
						    // 실제 내보낼 파일명
						    String orgfilename = "up_windows_setup.exe";    
							try {
								File file = new File(root);
								if (file.isFile()) {
									byte b[] = new byte[(int) file.length()];
									orgfilename = java.net.URLEncoder.encode(orgfilename, "UTF-8");
									response.setHeader("Content-Disposition", "attachment;filename=".concat(orgfilename));
									response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
									response.setContentLength((int) file.length());
									
									BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
									BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
									int read = 0;
									while ((read = fin.read(b)) != -1) {
										outs.write(b, 0, read);
									}
									outs.close();
									fin.close();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 EXE 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 EXE 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 EXE 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								e.printStackTrace();
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 18
						 * 
						 * 설명 :
						 * 		사용자 노래 재생 횟수 출력
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 18: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// DB 접속
								databaseConnection.setPreparedStatement("SELECT * FROM utaite_user_play_count WHERE user_uuid = ? ORDER BY COUNT DESC;");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.setResultSet();
								
								JsonArray countResult = new JsonArray();
								
								while (databaseConnection.getResultSet().next()) {
									JsonObject info = new JsonObject();
									info.addProperty("uuid", databaseConnection.getResultSet().getString("music_uuid"));
									info.addProperty("count", databaseConnection.getResultSet().getInt("count"));
									countResult.add(info);
								}
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자 노래 재생 횟수 출력 성공! Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", java.net.URLEncoder.encode(countResult.toString(), "UTF-8"));
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "사용자 노래 재생 횟수 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 19
						 * 
						 * 설명 :
						 * 		픽시브 Top 50 이미지 출력
						 * 
						 * 파라미터 :
						 *      smode       --> 이미지 출력 : 0 / 이미지 정보 출력 : 1
						 * 		name        --> Image Name
						 * 		auth        --> Auth Token
						 */
						case 19: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								String smode = request.getParameter("smode");
								if (smode != null && (Integer.parseInt(smode) == 1)) {
									File dir = new File(PathManager.UTAITE_PLAYER_PIXIV_TOP_50_IMAGE_PATH);
									File files[] = dir.listFiles();

									JsonArray imageResult = new JsonArray();
									
									for (int i = 0; i < files.length; i++)
										imageResult.add(files[i].getName());	
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "픽시브 Top 50 이미지 정보 출력 성공! Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(imageResult.toString(), "UTF-8"));
								}else {
									String fileName = request.getParameter("name");
									File file = new File(PathManager.UTAITE_PLAYER_PIXIV_TOP_50_IMAGE_PATH, fileName);
									if (file.exists()) {
										response.setHeader("Content-Type", "image/png;");
										
										byte[] image = IOUtils.toByteArray(new FileInputStream(file));
										response.getOutputStream().write(image);
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, String.format("픽시브 Top 50 이미지 출력 성공! [%s] Auth Token:", fileName), authToken));
									
										return;
									}else {
										// 로그 출력
										rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, String.format("픽시브 Top 50 이미지 출력 살패! [파일을 찾을 수 없습니다 - '%s'] Auth Token:", fileName), authToken));
										
										obj.addProperty(keyName_Result, failMessage);
									}
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "픽시브 Top 50 이미지/이미지 정보 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 20
						 * 
						 * 설명 :
						 * 		사용자별 가장 많이 듣는 노래 가사별 유사도 Top30 출력
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 20: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// DB 접속
								databaseConnection.setPreparedStatement("SELECT exec_result FROM utaite_ngram_lyrics WHERE user_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.setResultSet();
								
								if (databaseConnection.getResultSet().next()) {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자별 가장 많이 듣는 노래 가사별 유사도 Top30 출력 성공! Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(databaseConnection.getResultSet().getString("exec_result"), "UTF-8"));
								}else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "사용자별 가장 많이 듣는 노래 가사별 유사도 Top30 출력 살패! [데이터 없음] Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "사용자별 가장 많이 듣는 노래 가사별 유사도 Top30 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 21
						 * 
						 * 설명 :
						 * 		OHLI 애니메이션 방영 정보 출력
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
						case 21: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								// DB 접속
								databaseConnection.setPreparedStatement("SELECT * FROM utaite_ohli_anim_air_info");
								databaseConnection.setResultSet();
								
								JsonArray animResult = new JsonArray();
								
								while (databaseConnection.getResultSet().next()) {
									JsonObject info = new JsonObject();
									info.addProperty("uuid", databaseConnection.getResultSet().getString("uuid"));
									info.addProperty("name", databaseConnection.getResultSet().getString("name"));
									info.addProperty("image", databaseConnection.getResultSet().getString("image"));
									info.addProperty("start_day", databaseConnection.getResultSet().getString("start_day"));
									info.addProperty("end_day", databaseConnection.getResultSet().getString("end_day"));
									info.addProperty("day_of_the_week", databaseConnection.getResultSet().getInt("day_of_the_week"));
									info.addProperty("live_time", databaseConnection.getResultSet().getString("live_time"));
									info.addProperty("official_site", databaseConnection.getResultSet().getString("official_site"));
									animResult.add(info);
								}
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OHLI 애니메이션 방영 정보 출력 성공! Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", java.net.URLEncoder.encode(animResult.toString(), "UTF-8"));
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "OHLI 애니메이션 방영 정보 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 22
						 * 
						 * 설명 :
						 * 		애니메이션 업로드 정보 출력
						 * 
						 * 파라미터 :
						 *      smode       --> 업로드 정보 출력 : 0 / 변경된 정보 출력 : 1 [date 파라미터 필요]
						 *      date        --> 변경 날짜
						 * 		auth        --> Auth Token
						 */
						case 22: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // 로그인 성공
								
								// 데이터 갱신 확인
								boolean isReload = false;
								databaseConnection.setPreparedStatement("SELECT is_reload FROM utaite_anim_upload_info_var");
								databaseConnection.setResultSet();
								if (databaseConnection.getResultSet().next()) {
									isReload = databaseConnection.getResultSet().getInt("is_reload") == 1;
								}
								
								String smode = request.getParameter("smode");
								String date = request.getParameter("date");
								
								if (isReload) {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "애니메이션 업로드 정보 출력 실패! [데이터 갱신 중] ,sMode:", smode, ",Date:", date, "Auth Token", authToken));

									obj.addProperty(keyName_Result, failMessage);
								}else {
									databaseConnection.closeResultSet();
									databaseConnection.closePreparedStatement();

									if (smode != null && (Integer.parseInt(smode) == 1)) {
										// DB 접속
										databaseConnection.setPreparedStatement("SELECT * FROM utaite_anim_upload_info WHERE NOT date = ?;");
										databaseConnection.getPreparedStatement().setString(1, date);
										databaseConnection.setResultSet();
									}else {
										// DB 접속
										databaseConnection.setPreparedStatement("SELECT * FROM utaite_anim_upload_info");
										databaseConnection.setResultSet();
									}	
									
									JsonArray animResult = new JsonArray();
									
									while (databaseConnection.getResultSet().next()) {
										JsonObject info = new JsonObject();
										info.addProperty("uuid", databaseConnection.getResultSet().getString("uuid"));
										info.addProperty("date", databaseConnection.getResultSet().getString("date"));
										info.addProperty("name", databaseConnection.getResultSet().getString("name"));
										info.addProperty("image", databaseConnection.getResultSet().getString("image"));
										info.addProperty("url", databaseConnection.getResultSet().getString("url"));
										info.addProperty("yoil", databaseConnection.getResultSet().getInt("yoil"));
										info.addProperty("episode", databaseConnection.getResultSet().getString("episode"));
										animResult.add(info);
									}
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "애니메이션 업로드 정보 출력 성공! sMode:", smode, ",Date:", date, "Auth Token", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(animResult.toString(), "UTF-8"));
								}
							}else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "애니메이션 업로드 정보 출력 살패! [로그인 실패] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 23
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 드라이버 다운로드
						 * 
						 * 파라미터 :
						 *      bit         --> 64비트 운영체제 : x64 , 32비트 운영체제 : x86
						 */
						case 23: {
							// 파일 업로드된 경로
						    final String root_x64 = PathManager.UTAITE_PLAYER_MANAGER_DRIVER_VCREDIST_x64_PATH;
						    final String root_x32 = PathManager.UTAITE_PLAYER_MANAGER_DRIVER_VCREDIST_x86_PATH;
						    // 실제 내보낼 파일명
						    final String orgfilename_x64 = "up_windows_driver_vcredist_x64.exe";
						    final String orgfilename_x32 = "up_windows_driver_vcredist_x32.exe";
							try {
								String bit = request.getParameter("bit");
								
								File file = new File(bit.equals("x64") ? root_x64 : root_x32);
								if (file.isFile()) {
									byte b[] = new byte[(int) file.length()];
									String orgfilename = bit.equals("x64") ? orgfilename_x64 : orgfilename_x32;
	
									orgfilename = java.net.URLEncoder.encode(orgfilename, "UTF-8");
									response.setHeader("Content-Disposition", "attachment;filename=".concat(orgfilename));
									response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
									response.setContentLength((int) file.length());
									
									BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
									BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
									int read = 0;
									while ((read = fin.read(b)) != -1) {
										outs.write(b, 0, read);
									}
									outs.close();
									fin.close();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 드라이버 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 드라이버 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 드라이버 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * 우타이테 플레이어 관리 모드 : 24
						 * 
						 * 설명 :
						 * 		우타이테 플레이어 Windows 버전 ZIP 파일 다운로드
						 * 
						 * 파라미터 :
						 *      <없음>
						 */
						case 24: {
							// 파일 업로드된 경로
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_INSATLL_ZIP_PATH;
						    // 실제 내보낼 파일명
						    String orgfilename = "up_windows_install_zip.zip";
							try {
								File file = new File(root);
								if (file.isFile()) {
									byte b[] = new byte[(int) file.length()];
									orgfilename = java.net.URLEncoder.encode(orgfilename, "UTF-8");
									response.setHeader("Content-Disposition", "attachment;filename=".concat(orgfilename));
									response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
									response.setContentLength((int) file.length());
									
									BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
									BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
									int read = 0;
									while ((read = fin.read(b)) != -1) {
										outs.write(b, 0, read);
									}
									outs.close();
									fin.close();
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 ZIP 설치 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "우타이테 플레이어 ZIP 설치 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "우타이테 플레이어 ZIP 설치 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
					}
				}
			}else { // 페이지 초기화 실패
				obj.addProperty(keyName_Result, failMessage);
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
			}
		}catch (Exception e) {
			e.printStackTrace();
			
			obj.addProperty(keyName_Result, "ROOT_ERROR");
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "알 수 없는 오류 발생!", e.toString()));
		}
		
		
		// DB 연결 해제
		try {
			if (databaseConnection != null) 
				databaseConnection.allClose();
		}catch (SQLException e) {
			e.printStackTrace();
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "데이터베이스 연결 해제 오류 발생! ", e.toString()));
		}
	}
    
	
	
    
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		service(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	
	
	
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	/**
	 * 노래 아티스트 이름, 이미지 구하기
	 * 
	 * @param uuid 아티스트 UUID
	 * @return String Array
	 * @throws SQLException DB 접속 오류
	 * @throws ClassNotFoundException DB 접속 오류
	 */
	private String[] getSingerNameAndImage(String uuid) throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT * FROM utaite_singer WHERE singer_uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, uuid);
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			String[] string = new String[2];
			string[0] = databaseConnection.getResultSet().getString("singer_name");
			string[1] = databaseConnection.getResultSet().getString("singer_image");
			
			databaseConnection.allClose();
			
			return string;
		}else {
			return null;
		}
	}
	
	
	
	/**
	 * 특정 폴더 용량 구하는 함수
	 * 
	 * @param directory 폴더 경로
	 * @return long
	 */
	private long getFolderSize(File directory) {
	    long length = 0;
	    try {
	        for (File file : directory.listFiles()) {
	            if (file.isFile())
	                length += file.length();
	        else
	            length += getFolderSize(file);
	        }		
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return length;
	}
	
	
	
	/**
	 * 서버 접근 제한 확인
	 * 
	 * @return 0 --> Version
	 * 		   1 --> Access check value
	 * @throws SQLException DB 접속 오류
	 * @throws ClassNotFoundException DB 접속 오류
	 */
	public String[] isServerAccessCheck() throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT * FROM utaite_info WHERE main_key = 1;");
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			String[] string = new String[2];
			string[0] = databaseConnection.getResultSet().getString("version");
			string[1] = databaseConnection.getResultSet().getString("access_check").toString();
			
			databaseConnection.allClose();
			
			return string;
		}else {
			return null;
		}
	}
	
	
	
	/**
	 * 우타이테 플레이어 접근 허용 확인
	 * 
	 * @param user_uuid 사용자 UUID
	 * @return boolean
	 * @throws SQLException DB 접속 오류
	 * @throws ParseException 날짜 변환 오류
	 * @throws ClassNotFoundException DB 접속 오류
	 */
	private boolean isAccessCheck(String user_uuid) throws SQLException, ParseException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT user_access_var, user_access_date from utaite_user_info where user_uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, user_uuid);
		databaseConnection.setResultSet();
		
		boolean isUse = true;
		
		if (databaseConnection.getResultSet().next()) {
			int checker = databaseConnection.getResultSet().getInt("user_access_var");
			String date = databaseConnection.getResultSet().getString("user_access_date");
			
			// 날자 및 변수 확인
			if (checker == 1) {
				if (!date.equals("[null]")) {
					if (date.equals("[unlimited]")) {
						// 사용 허가
						isUse = true;
					}else {
						// 현재 날자
						Date nowDate = new Date();		
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date getdate = simpleDateFormat.parse(date);
						// 날자 확인
						if (getdate.compareTo(nowDate) >= 0) {
							// 사용 허가
							isUse = true;
						}else {
							// 이용 차단
							isUse = false;
						}
					}
				}else {
					// 이용 차단
					isUse = false;
				}
			}else {
				// 이용 차단
				isUse = false;
			}
		}else {
			isUse = false;
		}
		
		databaseConnection.allClose();
		
		return isUse;
	}
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
}
