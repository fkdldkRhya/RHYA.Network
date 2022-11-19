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
	// MP3 ���� ���� ���
	private final String mp3SaveRootPath = PathManager.UTAITE_PLAYER_MANAGER_MP3_PATH;
	// JSON ��ȯ ���
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
		
		// Rhya �ΰ� ���� ����
		RhyaLogger rl = new RhyaLogger();
		// Rhya �ΰ� ����
		rl.JspName = request.getServletPath();
		rl.LogConsole = true;
		rl.LogFile = true;
		
		
		// Ŭ���̾�Ʈ IP
		String clientIP = GetClientIPAddress.getClientIp(request);
		
		
		// JSON ���
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		// JSON ����
		Gson gson = new Gson();
		JsonObject obj = new JsonObject();

		
		// DB ������ ����
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		
		
		// ��ü ���� ó��
		try {
			// ������ �ʱ�ȭ
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Utaite_Player)) { // ������ �ʱ�ȭ ����
				// DB ����
				try {
					databaseConnection.init();
					databaseConnection.connection();
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					databaseConnection = null;
					
					// �α� ���
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�����ͺ��̽� ���� �� ���� �߻�! ", e.toString()));
				}
				
				
				// Null Ȯ��
				if (databaseConnection != null) {
					// Main �Ķ���� ����
					int inputMode = Integer.parseInt(request.getParameter("mode"));
					// �α� ���
					rl.Log(RhyaLogger.Type.Debug, rl.CreateLogTextv8(clientIP, "Ŭ���̾�Ʈ�� �ش� �Ķ���ͷ� ������ Mode:", Integer.toString(inputMode)));
					
					// ������ ���� Ȯ��
					if (!new ServiceAccessChecker().isAccessService(0)) {
						// �α� ���
						rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "���� ���� ���ܵ�!"));
						
						obj.addProperty(keyName_Result, "service_access_block");
						
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						databaseConnection.allClose();
						
						return;
					}
					
					// �Ķ���� ����
					switch (inputMode) {
						/**
						 * �� �� ���� ��ɾ�
						 */
						default: {
							// �α� ���
							rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�Է��� ����� �м��� �� �����ϴ�. Mode:", Integer.toString(inputMode)));
							
							obj.addProperty(keyName_Result, failMessage);
							obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���.", "UTF-8"));
							
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 0
						 * 
						 * ���� :
						 * 		����� ���� ��������
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 0: {
							// Auth token Ȯ��
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // �α��� ����
								// ������ ���� ���� Ȯ��
								databaseConnection.setPreparedStatement("SELECT * FROM utaite_user_info WHERE user_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.setResultSet();
								if (!databaseConnection.getResultSet().next()) {
									// ������ ����
									databaseConnection.closeResultSet();
									databaseConnection.closePreparedStatement();
									databaseConnection.setPreparedStatement("INSERT INTO utaite_user_info VALUES (?, ?, ?, ?, ?);");
									databaseConnection.getPreparedStatement().setString(1, result[1]);
									databaseConnection.getPreparedStatement().setString(2, "{}");
									databaseConnection.getPreparedStatement().setString(3, "{\"list\": []}");
									databaseConnection.getPreparedStatement().setInt(4, 0);
									databaseConnection.getPreparedStatement().setString(5, "[null]");
									databaseConnection.executeUpdate();
									// JSON ������ ���� [ �⺻ ]
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("uuid", URLEncoder.encode(result[1], "UTF-8"));
									obj.addProperty("play_list", URLEncoder.encode("{}", "UTF-8"));
									obj.addProperty("subscribe_list", URLEncoder.encode("{\"list\": []}", "UTF-8"));
									obj.addProperty("access_var", 0);
									obj.addProperty("access_date", URLEncoder.encode("[null]", "UTF-8"));
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ���� ��� ����! (�ű� �����) Auth Token:", authToken));
								}else {
									// JSON ������ ����
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("uuid", URLEncoder.encode(databaseConnection.getResultSet().getString("user_uuid"), "UTF-8"));
									obj.addProperty("play_list", URLEncoder.encode(databaseConnection.getResultSet().getString("user_play_list"), "UTF-8"));
									obj.addProperty("subscribe_list", URLEncoder.encode(databaseConnection.getResultSet().getString("user_subscribe_list"), "UTF-8"));
									obj.addProperty("access_var", databaseConnection.getResultSet().getInt("user_access_var"));
									obj.addProperty("access_date", URLEncoder.encode(databaseConnection.getResultSet().getString("user_access_date"), "UTF-8"));
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ���� ��� ����! Auth Token:", authToken));
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ���� ��� ����! [�α��� ����] Auth Token:", authToken));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 1
						 * 
						 * ���� :
						 * 		�뷡 ���� ���
						 * 
						 * �Ķ���� :
						 * 	    - �� �� �����ؼ� �Է�
						 *      ------------------------------
						 * 		new         --> �ű� �뷡 ���
						 * 		suuid       --> Ư�� �뷡 ���
						 * 		all         --> ��� �뷡 ���
						 * 		version     --> ��� �뷡 ���� ���
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
									isAccessCheck(result[1])) { // �α��� ����
								// �Ķ���� ����
								String newSong = request.getParameter("new");
								String suuid = request.getParameter("suuid");
								String all = request.getParameter("all");
								String version = request.getParameter("version");
								
								// �Ķ���� �з�
								if (newSong != null) { // �ֽ� ���ڼ�
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list ORDER BY music_date DESC;");
									databaseConnection.setResultSet();

									// �뷡 ����Ʈ
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "�ֱٿ� �߰��� �뷡 ����Ʈ ��� ����! Auth Token:", authToken));
								}else if (suuid != null) { // Ư�� �뷡
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list WHERE music_uuid = ?;");
									databaseConnection.getPreparedStatement().setString(1, suuid);
									databaseConnection.setResultSet();
									
									// �뷡 ����Ʈ
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Ư�� �뷡 ���� ��� ����! Auth Token:", authToken));
								}else if (all != null) {
									databaseConnection.setPreparedStatement("SELECT * FROM utaite_list;");
									databaseConnection.setResultSet();

									// �뷡 ����Ʈ
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��� �뷡 ���� ��� ����! Auth Token:", authToken));
								}else if (version != null) { // ���� Ȯ��
									databaseConnection.setPreparedStatement("SELECT music_uuid, update_version FROM utaite_list;");
									databaseConnection.setResultSet();

									// �뷡 ����Ʈ
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��� �뷡 ���� ���� ��� ����! Auth Token:", authToken));
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�Է��� ����� �м��� �� �����ϴ�. [�뷡 ���� ���] Auth Token: ", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��� �뷡 ���� ���� ��� ����! [�α��� ����]  Auth Token:", authToken));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 2
						 * 
						 * ���� :
						 * 		�뷡 ���� �뷮 ���
						 * 
						 * �Ķ���� :
						 * 		<����>
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
							
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "�뷡 ���� �뷮 ��� ����!"));
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 3
						 * 
						 * ���� :
						 * 		����� ������ ����
						 * 
						 * �Ķ���� :
						 * 		index       --> ������ ����
						 * 		value       --> ������
						 * 		auth        --> Auth Token
						 */
						case 3: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // �α��� ����
								// �Ķ���� ����
								int index = Integer.parseInt(request.getParameter("index"));
								String value = request.getParameter("value");	
								// SQL
								String runSQL = "UPDATE utaite_user_info SET <KEY> = ? WHERE user_uuid = ?;";
								if (index == 0) runSQL = runSQL.replace("<KEY>", "user_play_list");
								else runSQL = runSQL.replace("<KEY>", "user_subscribe_list");
								
								// JSON ���� Ȯ��
								try {
									JsonParser parser = new JsonParser();
									parser.parse(URLDecoder.decode(value, "UTF-8"));
									parser = null;
									
									databaseConnection.setPreparedStatement(runSQL);
									databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(value, "UTF-8"));
									databaseConnection.getPreparedStatement().setString(2, result[1]);
									databaseConnection.executeUpdate();
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "����� ������ ���� ����! - ", "Value:", value, "| Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
								}catch(JsonSyntaxException ex) {
									ex.printStackTrace();
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "����� ������ ���� ����! �� �� ���� JSON ���� Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� JSON �����Դϴ�.", "UTF-8"));
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ������ ���� ����! [�α��� ����] Auth Token:", authToken));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 4
						 * 
						 * ���� :
						 * 		�뷡 ���
						 * 
						 * �Ķ���� :
						 * 		uuid        --> �뷡 UUID
						 * 		auth        --> Auth Token
						 */
						case 4: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// �Ķ���� ����
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
						        
						        // �뷡 DB �˻�
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
									 	
									 	// ���� �׼��� ������ �̿��� mp3 ������ ������ �б�
								        try (RandomAccessFile randomAccessFile = new RandomAccessFile(mp3, "r");
								            ServletOutputStream sos = response.getOutputStream();){
								        	
								            Integer bufferSize = 1024, data = 0;
								            byte[] b = new byte[bufferSize];
								            Long count = startRange;
								            Long requestSize = endRange - startRange + 1;

								            // startRange���� ���
								            randomAccessFile.seek(startRange);

								            while (true) {
								                // ���� ������ (1024) ���� ������ ������
								                if(requestSize <= 2) {
								                    // Range byte 0-1�� �Ʒ� �ǹ̰� �ƴ�.
								                    // data = randomAccessFile.read(b, 0, requestSize.intValue());
								                    // sos.write(b, 0, data);

								                    // ** write ���� �ٷ� flush ** //
								                    sos.flush();
								                    break;
								                }

								                // �������� �Ϲ������� ����
								                data = randomAccessFile.read(b, 0, b.length);

								                // count�� endRange �̻��̸� ��û ������ �Ѿ ���̹Ƿ� ����
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
									 	
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "�뷡 ��� ����!", "Music UUID:", uuid, " ,Auth Token:", authToken));
									}catch (IOException ioe) {
										ioe.printStackTrace();
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "�뷡 ��� ����! [IOException]", "Auth Token:" , authToken, " -", ioe.toString()));
									}
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "�뷡 ��� ����! [�뷡 UUID�� ã�� �� ����] Auth Token:", authToken));
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "�뷡 ��� ����! [�α��� ����] Auth Token:", authToken));
							}

							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 5
						 * 
						 * ���� :
						 * 		����� ��Ÿ���� �÷��̾� ���� ��� Ȯ��
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 5: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� ���� ���� ��� Auth Token:", authToken));
								// JSON ������ ����
								obj.addProperty(keyName_Result, successMessage);
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� ���� ���� �ź� Auth Token:", authToken));
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
				
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 6
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� �� ���� ���
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 6: {
							// �α� ���
							databaseConnection.setPreparedStatement("SELECT * FROM utaite_info;");
							databaseConnection.setResultSet();
							if (databaseConnection.getResultSet().next()) {
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("version", databaseConnection.getResultSet().getString("version"));
								obj.addProperty("version_for_windows", databaseConnection.getResultSet().getString("version_for_windows"));
								obj.addProperty("key", databaseConnection.getResultSet().getString("app_sign_key"));
								obj.addProperty("update_description", URLEncoder.encode(databaseConnection.getResultSet().getString("update_description"), "UTF-8"));
								obj.addProperty("update_description_for_windows", URLEncoder.encode(databaseConnection.getResultSet().getString("update_description_for_windows"), "UTF-8"));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� �� ���� ��� ����"));	
							}else {
								obj.addProperty(keyName_Result, failMessage);
								obj.addProperty("key", "[null]");
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� �� ���� ��� ����"));
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 7
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� �� �ٿ�ε�
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 7: {
							// ���� ���ε�� ���
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_APK_PATH;
						    // ���� ������ ���ϸ�
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� APK ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� APK ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� APK ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 8
						 * 
						 * ���� :
						 * 		MP3 ���� �ٿ�ε�
						 * 
						 * �Ķ���� :
						 * 		uuid        --> �뷡 UUID
						 * 		auth        --> Auth Token
						 */
						case 8: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// �Ķ���� ����
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
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! [UUID] ", "Auth Token:", authToken, ",Music UUID:", uuid));
										} else {
											// �α� ���
											rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv10(clientIP, "��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! File does not exist!", "Auth Token:", authToken, ",Music UUID:", uuid));
											
											obj.addProperty(keyName_Result, failMessage);
											obj.addProperty(keyName_Message, URLEncoder.encode("��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! File does not exist!", "UTF-8"));
											
											PrintWriter out = response.getWriter(); 
											out.println(gson.toJson(obj));
										}
									} catch (IOException ioe) {
										ioe.printStackTrace();
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! [IOException]", "Auth Token:" , authToken, " -", ioe.toString()));
									}
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! [�뷡 UUID�� ã�� �� ����] Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� MP3 ���� �ٿ�ε� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 9
						 * 
						 * ���� :
						 * 		Ư�� ���� �뷮 ���
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 9: {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("size", getFolderSize(new File(mp3SaveRootPath)));
							
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� MP3 ���� ������"));
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 10
						 * 
						 * ���� :
						 * 		���� ���� ���
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 10: {
							String[] getInfo = isServerAccessCheck();
							
							if (getInfo != null) {
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("version", getInfo[0]);
								obj.addProperty("access", getInfo[1]);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "���� ���� ��� ����!"));
							}else {
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "���� ���� ��� ����! �� �� ���� ������ �߻��Ͽ����ϴ�."));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 11
						 * 
						 * ���� :
						 * 		�뷡 ��� Ƚ�� �ݿ�
						 * 
						 * �Ķ���� :
						 * 		uuid        --> �뷡 UUID
						 * 		auth        --> Auth Token
						 */
						case 11: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// �Ķ���� ����
								String uuid = request.getParameter("music");
								// DB ����
								databaseConnection.setPreparedStatement("UPDATE utaite_list set play_count = play_count + 1 where music_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, uuid);
								databaseConnection.executeUpdate();
								databaseConnection.closePreparedStatement();
								databaseConnection.setPreparedStatement("INSERT INTO utaite_user_play_count (user_uuid, music_uuid, COUNT) VALUE (?, ?, 1) ON DUPLICATE KEY UPDATE COUNT = COUNT + 1");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.getPreparedStatement().setString(2, uuid);
								databaseConnection.executeUpdate();
								
								if (databaseConnection.executeUpdate() >= 1) {
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv10(clientIP, "�뷡 ��� Ƚ�� �ݿ� ����!", "Auth Token:", authToken, ",Music UUID:", uuid));
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�뷡 ��� Ƚ�� �ݿ� ����! ������ �뷡 ���� ������  0�� �Դϴ�. Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�뷡 ��� Ƚ�� �ݿ� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 12
						 * 
						 * ���� :
						 * 		��� Ƚ�� ��� Top8 �뷡 ����Ʈ
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 12: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// DB ����
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
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "�뷡 ���� ��� ����! Auth Token:", authToken));
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�뷡 ���� ��� ����! �˻��� ������ �����ϴ�. Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�뷡 ���� ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 13
						 * 
						 * ���� :
						 * 		����� ���� ���� [����]
						 * 
						 * �Ķ���� :
						 * 		index       --> ���� ���, ����
						 * 		value       --> ������
						 * 		auth        --> Auth Token
						 */
						case 13: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // �α��� ����
								// �Ķ���� ����
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
									// JSON ���� Ȯ��
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
	
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "����� ������ ���� ����! [���� ���� ����] ,Index:", String.valueOf(index), ",Value:", value, ",Auth Token", authToken));
											
											obj.addProperty(keyName_Result, successMessage);
										}else {
											// �α� ���
											rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "����� ������ ���� ����! [���� ���� ����] [����� ��ȸ ����] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
											
											obj.addProperty(keyName_Result, failMessage);
											obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� JSON �����Դϴ�.", "UTF-8"));
										}
									}catch(JsonSyntaxException ex) {
										ex.printStackTrace();
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "����� ������ ���� ����! [���� ���� ����] [�� �� ���� JSON ����] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
										
										obj.addProperty(keyName_Result, failMessage);
										obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� JSON �����Դϴ�.", "UTF-8"));
									}
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "����� ������ ���� ����! [���� ���� ����] [�� �� ���� SINGER UUID] ,Index:", String.valueOf(index), ",Value:", value, "Auth Token", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� UUID �Դϴ�.", "UTF-8"));
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ������ ���� ����! [���� ���� ����] [�α��� ����] Auth Token:", authToken));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 14
						 * 
						 * ���� :
						 * 		����� ���� ���� [�÷��̸���Ʈ]
						 * 
						 * �Ķ���� :
						 * 		index       --> ��ɾ� Ÿ��
						 * 			            0 : �÷��̸���Ʈ ����
						 * 					    1 : �÷��̸���Ʈ ����
						 * 					    2 : �÷��̸���Ʈ ����
						 * 					    3 : �÷��̸���Ʈ �뷡 ����
						 * 		value1      --> ������ 1
						 * 		value2      --> ������ 2
						 * 		value3      --> ������ 3
						 * 		auth        --> Auth Token
						 */
						case 14: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0))) { // �α��� ����
								// �Ķ���� ����
								final String IMAGE_TYPE = "_IMAGE_TYPE_";
								final String NAME_TYPE = "_NAME_";
								int index = Integer.parseInt(request.getParameter("index"));
								String value1 = request.getParameter("value1");	
								String value2 = request.getParameter("value2");	
								String value3 = request.getParameter("value3");	
								value1 = URLDecoder.decode(value1,"UTF-8");
								value2 = URLDecoder.decode(value2,"UTF-8");
								value3 = URLDecoder.decode(value3,"UTF-8");
							
								
								// Index �м�
								switch (index) {
									/**
									 * �� �� ���� ��ɾ�
									 */
									default:
										// JSON ������ ����
										obj.addProperty(keyName_Result, failMessage);
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�� �� ���� INDEX] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										break;
								
									/**
									 * �÷��̸���Ʈ ����
									 */
									case 0: {
										// �÷��̸���Ʈ Ȯ��
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											// JSON ������ �Ľ�
											String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
											JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
											Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
											// ������ ���� Ȯ��
											boolean isExt = false;
											for (Map.Entry<String, JsonElement> entry: entries) {
											    if (value1.equals(entry.getKey())) {
											    	isExt = true;
											    	break;
											    }
											}
											// ������ ����
											if (isExt) {
												jsonRootObject.remove(value1);
												
												// ����
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonRootObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
											}
											
											// JSON ������ ����
											obj.addProperty(keyName_Result, successMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ����] ,Index:", String.valueOf(index), ",�÷��̸���Ʈ �̸�:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}else {
											// JSON ������ ����
											obj.addProperty(keyName_Result, failMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - �÷��̸���Ʈ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",�÷��̸���Ʈ �̸�:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									/**
									 * �÷��̸���Ʈ ����
									 */
									case 1: {
										// �Ķ���� Ȯ��
										if (!value2.contains(IMAGE_TYPE)) {
											// JSON ������ ����
											obj.addProperty(keyName_Result, failMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - IMAGE_TYPE�� �������� ����] ,Index:", String.valueOf(index), ",�÷��̸���Ʈ �̸�:", value1, ",�÷��̸���Ʈ �̹���:", value2, ",Value3:", value3, "Auth Token", authToken));
										}else {
											// �÷��̸���Ʈ Ȯ��
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON ������ �Ľ�
												UUID uuid = UUID.randomUUID();
												String uuidStr = uuid.toString();
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												JsonArray jsonArray = new JsonArray();
												jsonArray.add(NAME_TYPE.concat(value1));
												jsonArray.add(value2);
												jsonRootObject.add(uuidStr, jsonArray);
												
												// ����
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonRootObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, successMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ����] ,Index:", String.valueOf(index), ",�÷��̸���Ʈ �̸�:", value1, ",�÷��̸���Ʈ �̹���:", value2, ",Value3:", value3, "Auth Token", authToken));
											}else {
												// JSON ������ ����
												obj.addProperty(keyName_Result, failMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - �÷��̸���Ʈ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",�÷��̸���Ʈ �̸�:", value1, ",�÷��̸���Ʈ �̹���:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}
										
										break;
									}
									
									
									/**
									 * �÷��̸���Ʈ ���� [�⺻ ����]
									 */
									case 2: {
										// ���� ������ ����
										if (value1.equalsIgnoreCase("name")) { // �÷��̸���Ʈ �̸�
											// �÷��̸���Ʈ Ȯ��
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON ������ �Ľ�
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
												// ������ ����
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
												// ����
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, successMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ����] ,Index:", String.valueOf(index), ",������ ������:", value1, ",�÷��̸���Ʈ �̸�:", value2, ",���� ������ ��:", value3, "Auth Token", authToken));
											}else {
												// JSON ������ ����
												obj.addProperty(keyName_Result, failMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - �÷��̸���Ʈ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",������ ������:", value1, ",�÷��̸���Ʈ �̸�:", value2, ",���� ������ ��:", value3, "Auth Token", authToken));
											}
										}else if (value1.equalsIgnoreCase("image")) { // �÷��̸���Ʈ �̹���
											// �÷��̸���Ʈ Ȯ��
											databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
											databaseConnection.getPreparedStatement().setString(1, result[1]);
											databaseConnection.setResultSet();
											if (databaseConnection.getResultSet().next()) {
												// JSON ������ �Ľ�
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonObject jsonRootObject = (JsonObject) new JsonParser().parse(jsonValue);
												Set<Map.Entry<String, JsonElement>> entries = jsonRootObject.entrySet();
												// ������ ����
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
												// ����
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, successMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ����(Image)] ,Index:", String.valueOf(index), ",������ ������:", value1, ",�÷��̸���Ʈ �̸�:", value2, ",���� ������ ��:", value3, "Auth Token", authToken));
											}else {
												// JSON ������ ����
												obj.addProperty(keyName_Result, failMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - �÷��̸���Ʈ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",������ ������:", value1, ",�÷��̸���Ʈ �̸�:", value2, ",���� ������ ��:", value3, "Auth Token", authToken));
											}
										}else { // �� �� ���� ������
											// JSON ������ ����
											obj.addProperty(keyName_Result, failMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ ���� ���� - ������ ���� ����] ,Index:", String.valueOf(index), ",������ ������:", value1, ",�÷��̸���Ʈ �̸�:", value2, ",���� ������ ��:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									
									/**
									 * �÷��̸���Ʈ �뷡 ������ ���� [No Data-Buffer]
									 */
									case 3: {
										// �÷��̸���Ʈ Ȯ��
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											try {
												// JSON ������ �Ľ�
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
												// ������ ����
												JsonObject jsonSubObject = new JsonObject();
												for (Map.Entry<String, JsonElement> entry : entries) {
												    if (value1.equals(entry.getKey())) {
													    jsonSubObject.add(entry.getKey(), newJsonArray);
												    }else {
													    jsonSubObject.add(entry.getKey(), entry.getValue());
												    }
												}
												
												// ����
												databaseConnection.closePreparedStatement();
												databaseConnection.closeResultSet();
												databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
												databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
												databaseConnection.getPreparedStatement().setString(2, result[1]);
												databaseConnection.executeUpdate();
												
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, successMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (No Data-Buffer) ����!] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}catch (Exception e) {
												// TODO: handle exception
												e.printStackTrace();
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, failMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (No Data-Buffer) ���� - JSON ������ ��ȯ ����] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}else {
											// JSON ������ ����
											obj.addProperty(keyName_Result, failMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (No Data-Buffer) ���� - ������ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
									
									
									/**
									 * �÷��̸���Ʈ �뷡 ������ ���� [Use Data-Buffer]
									 */
									case 4: {
										// �÷��̸���Ʈ Ȯ��
										databaseConnection.setPreparedStatement("SELECT user_play_list FROM utaite_user_info WHERE user_uuid = ?");
										databaseConnection.getPreparedStatement().setString(1, result[1]);
										databaseConnection.setResultSet();
										if (databaseConnection.getResultSet().next()) {
											try {
												// JSON ������ �Ľ�
												String jsonValue = databaseConnection.getResultSet().getString("user_play_list");
												JsonParser jsonParser = new JsonParser();
												JsonObject jsonRootObject = (JsonObject) jsonParser.parse(jsonValue);
												JsonArray newJsonArray = new JsonArray();
												
												DataBufferManager bufferManager = new DataBufferManager();
												String requestValue = bufferManager.getBuffer(value1);

												
												if (requestValue == null) {
													// JSON ������ ����
													obj.addProperty(keyName_Result, failMessage);
													
													// �α� ���
													rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (Use Data-Buffer) ���� - NotFoundRequestCode] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
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
													// ������ ����
													JsonObject jsonSubObject = new JsonObject();
													for (Map.Entry<String, JsonElement> entry : entries) {
													    if (value2.equals(entry.getKey())) {
														    jsonSubObject.add(entry.getKey(), newJsonArray);
													    }else {
														    jsonSubObject.add(entry.getKey(), entry.getValue());
													    }
													}

													// ����
													databaseConnection.closePreparedStatement();
													databaseConnection.closeResultSet();
													databaseConnection.setPreparedStatement("UPDATE utaite_user_info SET user_play_list = ? WHERE user_uuid = ?");
													databaseConnection.getPreparedStatement().setString(1, jsonSubObject.toString());
													databaseConnection.getPreparedStatement().setString(2, result[1]);
													databaseConnection.executeUpdate();
													
													// JSON ������ ����
													obj.addProperty(keyName_Result, successMessage);
													
													// �α� ���
													rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (Use Data-Buffer) ����!] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
												}
											}catch (Exception e) {
												// TODO: handle exception
												e.printStackTrace();
												
												// JSON ������ ����
												obj.addProperty(keyName_Result, failMessage);
												
												// �α� ���
												rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (Use Data-Buffer) ���� - JSON ������ ��ȯ ����] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
											}
										}else {
											// JSON ������ ����
											obj.addProperty(keyName_Result, failMessage);
											
											// �α� ���
											rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�÷��̸���Ʈ �뷡 ���� ���� (Use Data-Buffer) ���� - ������ ��ȸ �Ұ�] ,Index:", String.valueOf(index), ",Value1:", value1, ",Value2:", value2, ",Value3:", value3, "Auth Token", authToken));
										}
										
										break;
									}
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� ������ ���� ����! [�÷��̸���Ʈ ���� ����] [�α��� ����] Auth Token:", authToken));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 15
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� ���α׷� [PC - Windows] �ٿ�ε� For ZIP
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 15: {
							// ���� ���ε�� ���
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_ZIP_PATH;
						    // ���� ������ ���ϸ�
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ZIP[WINDOWS] ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ZIP[WINDOWS] ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� ZIP[WINDOWS] ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								e.printStackTrace();
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 16
						 * 
						 * ���� :
						 * 		���ڴ��ڵ� ���� ���
						 * 
						 * �Ķ���� :
						 * 		uuid        --> �뷡 UUID
						 */
						case 16: {  
							// ������ ���� ���� Ȯ��
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
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "���ڴ��ڵ� ���� ��� ����!"));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 17
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� EXE �ٿ�ε�
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 17: {
							// ���� ���ε�� ���
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_EXE_PATH;
						    // ���� ������ ���ϸ�
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� EXE ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� EXE ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� EXE ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								e.printStackTrace();
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 18
						 * 
						 * ���� :
						 * 		����� �뷡 ��� Ƚ�� ���
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 18: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// DB ����
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
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����� �뷡 ��� Ƚ�� ��� ����! Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", java.net.URLEncoder.encode(countResult.toString(), "UTF-8"));
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "����� �뷡 ��� Ƚ�� ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 19
						 * 
						 * ���� :
						 * 		�Ƚú� Top 50 �̹��� ���
						 * 
						 * �Ķ���� :
						 *      smode       --> �̹��� ��� : 0 / �̹��� ���� ��� : 1
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
									isAccessCheck(result[1])) { // �α��� ����
								String smode = request.getParameter("smode");
								if (smode != null && (Integer.parseInt(smode) == 1)) {
									File dir = new File(PathManager.UTAITE_PLAYER_PIXIV_TOP_50_IMAGE_PATH);
									File files[] = dir.listFiles();

									JsonArray imageResult = new JsonArray();
									
									for (int i = 0; i < files.length; i++)
										imageResult.add(files[i].getName());	
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "�Ƚú� Top 50 �̹��� ���� ��� ����! Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(imageResult.toString(), "UTF-8"));
								}else {
									String fileName = request.getParameter("name");
									File file = new File(PathManager.UTAITE_PLAYER_PIXIV_TOP_50_IMAGE_PATH, fileName);
									if (file.exists()) {
										response.setHeader("Content-Type", "image/png;");
										
										byte[] image = IOUtils.toByteArray(new FileInputStream(file));
										response.getOutputStream().write(image);
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, String.format("�Ƚú� Top 50 �̹��� ��� ����! [%s] Auth Token:", fileName), authToken));
									
										return;
									}else {
										// �α� ���
										rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, String.format("�Ƚú� Top 50 �̹��� ��� ����! [������ ã�� �� �����ϴ� - '%s'] Auth Token:", fileName), authToken));
										
										obj.addProperty(keyName_Result, failMessage);
									}
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�Ƚú� Top 50 �̹���/�̹��� ���� ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 20
						 * 
						 * ���� :
						 * 		����ں� ���� ���� ��� �뷡 ���纰 ���絵 Top30 ���
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 20: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// DB ����
								databaseConnection.setPreparedStatement("SELECT exec_result FROM utaite_ngram_lyrics WHERE user_uuid = ?;");
								databaseConnection.getPreparedStatement().setString(1, result[1]);
								databaseConnection.setResultSet();
								
								if (databaseConnection.getResultSet().next()) {
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����ں� ���� ���� ��� �뷡 ���纰 ���絵 Top30 ��� ����! Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(databaseConnection.getResultSet().getString("exec_result"), "UTF-8"));
								}else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����ں� ���� ���� ��� �뷡 ���纰 ���絵 Top30 ��� ����! [������ ����] Auth Token:", authToken));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "����ں� ���� ���� ��� �뷡 ���纰 ���絵 Top30 ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 21
						 * 
						 * ���� :
						 * 		OHLI �ִϸ��̼� �濵 ���� ���
						 * 
						 * �Ķ���� :
						 * 		auth        --> Auth Token
						 */
						case 21: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								// DB ����
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
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OHLI �ִϸ��̼� �濵 ���� ��� ����! Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", java.net.URLEncoder.encode(animResult.toString(), "UTF-8"));
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "OHLI �ִϸ��̼� �濵 ���� ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 22
						 * 
						 * ���� :
						 * 		�ִϸ��̼� ���ε� ���� ���
						 * 
						 * �Ķ���� :
						 *      smode       --> ���ε� ���� ��� : 0 / ����� ���� ��� : 1 [date �Ķ���� �ʿ�]
						 *      date        --> ���� ��¥
						 * 		auth        --> Auth Token
						 */
						case 22: {
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(0)) &&
									isAccessCheck(result[1])) { // �α��� ����
								
								// ������ ���� Ȯ��
								boolean isReload = false;
								databaseConnection.setPreparedStatement("SELECT is_reload FROM utaite_anim_upload_info_var");
								databaseConnection.setResultSet();
								if (databaseConnection.getResultSet().next()) {
									isReload = databaseConnection.getResultSet().getInt("is_reload") == 1;
								}
								
								String smode = request.getParameter("smode");
								String date = request.getParameter("date");
								
								if (isReload) {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv12(clientIP, "�ִϸ��̼� ���ε� ���� ��� ����! [������ ���� ��] ,sMode:", smode, ",Date:", date, "Auth Token", authToken));

									obj.addProperty(keyName_Result, failMessage);
								}else {
									databaseConnection.closeResultSet();
									databaseConnection.closePreparedStatement();

									if (smode != null && (Integer.parseInt(smode) == 1)) {
										// DB ����
										databaseConnection.setPreparedStatement("SELECT * FROM utaite_anim_upload_info WHERE NOT date = ?;");
										databaseConnection.getPreparedStatement().setString(1, date);
										databaseConnection.setResultSet();
									}else {
										// DB ����
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "�ִϸ��̼� ���ε� ���� ��� ����! sMode:", smode, ",Date:", date, "Auth Token", authToken));
									
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("message", java.net.URLEncoder.encode(animResult.toString(), "UTF-8"));
								}
							}else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�ִϸ��̼� ���ε� ���� ��� ����! [�α��� ����] Auth Token:", authToken));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 23
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� ����̹� �ٿ�ε�
						 * 
						 * �Ķ���� :
						 *      bit         --> 64��Ʈ �ü�� : x64 , 32��Ʈ �ü�� : x86
						 */
						case 23: {
							// ���� ���ε�� ���
						    final String root_x64 = PathManager.UTAITE_PLAYER_MANAGER_DRIVER_VCREDIST_x64_PATH;
						    final String root_x32 = PathManager.UTAITE_PLAYER_MANAGER_DRIVER_VCREDIST_x86_PATH;
						    // ���� ������ ���ϸ�
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ����̹� ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ����̹� ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� ����̹� ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						
						/**
						 * ��Ÿ���� �÷��̾� ���� ��� : 24
						 * 
						 * ���� :
						 * 		��Ÿ���� �÷��̾� Windows ���� ZIP ���� �ٿ�ε�
						 * 
						 * �Ķ���� :
						 *      <����>
						 */
						case 24: {
							// ���� ���ε�� ���
						    final String root = PathManager.UTAITE_PLAYER_MANAGER_INSATLL_ZIP_PATH;
						    // ���� ������ ���ϸ�
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ZIP ��ġ ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��Ÿ���� �÷��̾� ZIP ��ġ ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��Ÿ���� �÷��̾� ZIP ��ġ ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
					}
				}
			}else { // ������ �ʱ�ȭ ����
				obj.addProperty(keyName_Result, failMessage);
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP ������ �ʱ�ȭ �� ������ �߻��Ͽ����ϴ�."));
			}
		}catch (Exception e) {
			e.printStackTrace();
			
			obj.addProperty(keyName_Result, "ROOT_ERROR");
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�� �� ���� ���� �߻�!", e.toString()));
		}
		
		
		// DB ���� ����
		try {
			if (databaseConnection != null) 
				databaseConnection.allClose();
		}catch (SQLException e) {
			e.printStackTrace();
			
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�����ͺ��̽� ���� ���� ���� �߻�! ", e.toString()));
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
	 * �뷡 ��Ƽ��Ʈ �̸�, �̹��� ���ϱ�
	 * 
	 * @param uuid ��Ƽ��Ʈ UUID
	 * @return String Array
	 * @throws SQLException DB ���� ����
	 * @throws ClassNotFoundException DB ���� ����
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
	 * Ư�� ���� �뷮 ���ϴ� �Լ�
	 * 
	 * @param directory ���� ���
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
	 * ���� ���� ���� Ȯ��
	 * 
	 * @return 0 --> Version
	 * 		   1 --> Access check value
	 * @throws SQLException DB ���� ����
	 * @throws ClassNotFoundException DB ���� ����
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
	 * ��Ÿ���� �÷��̾� ���� ��� Ȯ��
	 * 
	 * @param user_uuid ����� UUID
	 * @return boolean
	 * @throws SQLException DB ���� ����
	 * @throws ParseException ��¥ ��ȯ ����
	 * @throws ClassNotFoundException DB ���� ����
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
			
			// ���� �� ���� Ȯ��
			if (checker == 1) {
				if (!date.equals("[null]")) {
					if (date.equals("[unlimited]")) {
						// ��� �㰡
						isUse = true;
					}else {
						// ���� ����
						Date nowDate = new Date();		
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date getdate = simpleDateFormat.parse(date);
						// ���� Ȯ��
						if (getdate.compareTo(nowDate) >= 0) {
							// ��� �㰡
							isUse = true;
						}else {
							// �̿� ����
							isUse = false;
						}
					}
				}else {
					// �̿� ����
					isUse = false;
				}
			}else {
				// �̿� ����
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
