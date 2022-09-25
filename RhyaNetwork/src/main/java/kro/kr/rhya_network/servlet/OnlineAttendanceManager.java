package kro.kr.rhya_network.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.util.PathManager;
import kro.kr.rhya_network.util.ServiceAccessChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.online_attendance.OnlineAttendanceAccessChecker;
import kro.kr.rhya_network.utils.online_attendance.OnlineAttendanceTeacherVO;

/**
 * Servlet implementation class OnlineAttendanceManager
 */
@WebServlet("/online_attendance_manager")
public class OnlineAttendanceManager extends HttpServlet {
	private static final long serialVersionUID = 2L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OnlineAttendanceManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
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
		final String successMessage = "success";
		final String failMessage = "fail";
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		
		try {
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Online_Attendance)) {
				// 페이지 접근 확인
				if (!new ServiceAccessChecker().isAccessService(1)) {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서비스 접근 차단됨!"));
					
					obj.addProperty(keyName_Result, "service_access_block");
					
					PrintWriter out = response.getWriter(); 
					out.println(gson.toJson(obj));

					return;
				}
				
				// 명령어
				int command = Integer.parseInt(request.getParameter("mode"));
				// 명령어 구분
				switch (command) {
					default: {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, URLEncoder.encode("알 수 명령입니다. mode 파라미터를 확인해 주세요.", "UTF-8"));
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "알 수 명령입니다. mode 파라미터를 확인해 주세요."));
						break;
					}
						
					
					case 0: {
						/**
						 * 온라인 출석부 관리 모드 : 0
						 * 
						 * 설명 :
						 * 		앱 버전, 앱 서명 SHA 지문 출력
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
						databaseConnection.init();
						databaseConnection.connection();
						databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_app;");
						databaseConnection.setResultSet();
						if (databaseConnection.getResultSet().next()) {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("version", databaseConnection.getResultSet().getString("version"));
							obj.addProperty("app_sign_key", databaseConnection.getResultSet().getString("app_sign_key"));
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "App version, App sign SHA key 출력 성공"));	
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "App version, App sign SHA key 출력 실패"));
						}
						
						databaseConnection.allClose();
						
						break;
					}
					
					
					case 1: {
						/**
						 * 온라인 출석부 관리 모드 : 1
						 * 
						 * 설명 :
						 * 		입력된 학교에 소속되어있는 모든 선생님 정보 출력
						 * 		(단, 전화번호-이메일 주소 같은 민감 정보는 제외됨.)
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							
							JsonArray array = new JsonArray();
							final String noValueText = "[NoValue]";
							
							while (databaseConnection.getResultSet().next()) {
								JsonObject input = new JsonObject();
								
								
								String uuid = databaseConnection.getResultSet().getString("uuid");	
								String name = databaseConnection.getResultSet().getString("name");	
								String name2 = databaseConnection.getResultSet().getString("name_no_duplication");
								String image = databaseConnection.getResultSet().getString("image");
								String description = databaseConnection.getResultSet().getString("description");
								String department1 = databaseConnection.getResultSet().getString("department1");
								String department2 = databaseConnection.getResultSet().getString("department2");
								String office_phone = databaseConnection.getResultSet().getString("office_phone");
								String position = databaseConnection.getResultSet().getString("position");
								String subject = databaseConnection.getResultSet().getString("subject");
								int schoolID = databaseConnection.getResultSet().getInt("school_id");
								int version = databaseConnection.getResultSet().getInt("version");
								
								input.addProperty("uuid", URLEncoder.encode(uuid, "UTF-8"));
								input.addProperty("name", URLEncoder.encode(name, "UTF-8"));
								
								
								if (name2 != null) 
									input.addProperty("name2", URLEncoder.encode(name2, "UTF-8"));	
								else 
									input.addProperty("name2", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (image != null) 
									input.addProperty("image", URLEncoder.encode(image, "UTF-8"));
								else 
									input.addProperty("image", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (description != null) 
									input.addProperty("description", URLEncoder.encode(description, "UTF-8"));
								else 
									input.addProperty("description", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (department1 != null) 
									input.addProperty("department1", URLEncoder.encode(department1, "UTF-8"));
								else 
									input.addProperty("department1", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (department2 != null) 
									input.addProperty("department2", URLEncoder.encode(department2, "UTF-8"));
								else 
									input.addProperty("department2", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (office_phone != null) 
									input.addProperty("office_phone", URLEncoder.encode(office_phone, "UTF-8"));
								else 
									input.addProperty("office_phone", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (position != null) 
									input.addProperty("position", URLEncoder.encode(position, "UTF-8"));
								else 
									input.addProperty("position", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (subject != null) 
									input.addProperty("subject", URLEncoder.encode(subject, "UTF-8"));
								else 
									input.addProperty("subject", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								input.addProperty("school_id", schoolID);
								input.addProperty("version", version);
								array.add(input);
							}
							
							
							databaseConnection.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("teacher", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 선생님 정보 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 선생님 정보 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 2: {
						/**
						 * 온라인 출석부 관리 모드 : 2
						 * 
						 * 설명 :
						 * 		모드 1에서 출력되지 않은 민감정보를 출력하는 작업
						 * 		(전화번호, 이메일이 공개가 제한되어 있다면 '[private]'을 반환)
						 * 
						 * 파라미터 :
						 * 		teacherUUID  --> 선생님 UUID
						 * 		authToken    --> Auth Token
						 */
						String tUUID = request.getParameter("uuid");
						String authToken = request.getParameter("authToken");
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE uuid = ?;");
							databaseConnection.getPreparedStatement().setString(1, tUUID);
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								final String privateInfo = "[private]";
								final String noValueText = "[NoValue]";
								
								int checker = checkTeacherPrivateInfo(tUUID);
								String phone = databaseConnection.getResultSet().getString("mobile_phone");
								String email = databaseConnection.getResultSet().getString("email_address");
								
								if (phone == null) phone = noValueText;
								if (email == null) email = noValueText;
								
								
								if (checker == 1) {
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("phone", privateInfo);
									obj.addProperty("email", privateInfo);
									
									PrintWriter out = response.getWriter(); 
									out.println(gson.toJson(obj));
								}else if (checker == 2) {
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("phone", privateInfo);
									obj.addProperty("email", email);
									
									PrintWriter out = response.getWriter(); 
									out.println(gson.toJson(obj));
								}else if (checker == 3) {
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("phone", phone);
									obj.addProperty("email", privateInfo);
									
									PrintWriter out = response.getWriter(); 
									out.println(gson.toJson(obj));
								}else if (checker == 4 || checker == 0) {
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("phone", phone);
									obj.addProperty("email", email);
									
									PrintWriter out = response.getWriter(); 
									out.println(gson.toJson(obj));
								}else {
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("phone", noValueText);
									obj.addProperty("email", noValueText);
									
									PrintWriter out = response.getWriter(); 
									out.println(gson.toJson(obj));
								}
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "민감정보 출력 성공! Teacher UUID:", tUUID, ",Auth Token:", authToken));
							}else {
								obj.addProperty(keyName_Result, failMessage);
								PrintWriter out = response.getWriter(); 
								out.println(gson.toJson(obj));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "민감정보 출력 실패! [선생님 UUID 검색 실패] Teacher UUID:", tUUID, ",Auth Token:", authToken));
							}
							
							databaseConnection.allClose();
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "민감정보 출력 실패! [로그인 실패] Teacher UUID:", tUUID, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					
					case 3: {
						/**
						 * 온라인 출석부 관리 모드 : 3
						 * 
						 * 설명 :
						 * 		입력된 학교에 소속되어있는 모든 학생 정보 출력
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT class_uuid FROM online_attendance_class_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							DatabaseManager.DatabaseConnection databaseConnectionSub = new DatabaseManager.DatabaseConnection();
							databaseConnectionSub.init();
							databaseConnectionSub.connection();
							
							
							JsonArray array = new JsonArray();
							final String noValueText = "[NoValue]";
							
							while (databaseConnection.getResultSet().next()) {
								String class_uuid = databaseConnection.getResultSet().getString("class_uuid");
								databaseConnectionSub.setPreparedStatement("SELECT * FROM online_attendance_student_info WHERE student_class_uuid = ?;");
								databaseConnectionSub.getPreparedStatement().setString(1, class_uuid);
								databaseConnectionSub.setResultSet();
								
								while (databaseConnectionSub.getResultSet().next()) {
									JsonObject input = new JsonObject();
									
									
									String student_uuid = databaseConnectionSub.getResultSet().getString("student_uuid");	
									String student_class_uuid = databaseConnectionSub.getResultSet().getString("student_class_uuid");	
									int student_number = databaseConnectionSub.getResultSet().getInt("student_number");
									String student_name = databaseConnectionSub.getResultSet().getString("student_name");
									String student_image = databaseConnectionSub.getResultSet().getString("student_image");
									int gender = databaseConnectionSub.getResultSet().getInt("gender");
									int move_out = databaseConnectionSub.getResultSet().getInt("move_out");
									int year = databaseConnectionSub.getResultSet().getInt("school_year");
									String note = databaseConnectionSub.getResultSet().getString("note");
									int version = databaseConnectionSub.getResultSet().getInt("version");
									
									
									input.addProperty("student_uuid", URLEncoder.encode(student_uuid, "UTF-8"));
									input.addProperty("student_class_uuid", URLEncoder.encode(student_class_uuid, "UTF-8"));
									input.addProperty("student_number", student_number);
									input.addProperty("student_name", URLEncoder.encode(student_name, "UTF-8"));
									
									
									if (student_image != null) 
										input.addProperty("student_image", URLEncoder.encode(student_image, "UTF-8"));
									else 
										input.addProperty("student_image", URLEncoder.encode(noValueText, "UTF-8"));
									
									
									input.addProperty("gender", gender);
									input.addProperty("move_out", move_out);
									input.addProperty("year", year);
									
						
									if (note != null) 
										input.addProperty("note", URLEncoder.encode(note, "UTF-8"));
									else 
										input.addProperty("note", URLEncoder.encode(noValueText, "UTF-8"));
									
									
									input.addProperty("version", version);
									array.add(input);
								}
								
								databaseConnectionSub.closeResultSet();
								databaseConnectionSub.closePreparedStatement();
							}
							
							databaseConnection.allClose();
							databaseConnectionSub.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("student", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 학생 정보 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 학생 정보 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						
						break;
					}
					
					
					case 4: {
						/**
						 * 온라인 출석부 관리 모드 : 4
						 * 
						 * 설명 :
						 * 		입력된 학교에 소속되어있는 모든 학생 정보 출력 [ Version 정보와 UUID만 출력 ]
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT class_uuid FROM online_attendance_class_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							DatabaseManager.DatabaseConnection databaseConnectionSub = new DatabaseManager.DatabaseConnection();
							databaseConnectionSub.init();
							databaseConnectionSub.connection();
							
							
							JsonArray array = new JsonArray();
							while (databaseConnection.getResultSet().next()) {
								String class_uuid = databaseConnection.getResultSet().getString("class_uuid");
								databaseConnectionSub.setPreparedStatement("SELECT * FROM online_attendance_student_info WHERE student_class_uuid = ?;");
								databaseConnectionSub.getPreparedStatement().setString(1, class_uuid);
								databaseConnectionSub.setResultSet();
								
								while (databaseConnectionSub.getResultSet().next()) {
									JsonObject input = new JsonObject();
									
									String student_uuid = databaseConnectionSub.getResultSet().getString("student_uuid");
									int version = databaseConnectionSub.getResultSet().getInt("version");
									
									
									input.addProperty("student_uuid", URLEncoder.encode(student_uuid, "UTF-8"));
									input.addProperty("version", version);
									array.add(input);
								}
								
								databaseConnectionSub.closeResultSet();
								databaseConnectionSub.closePreparedStatement();
							}
							
							databaseConnection.allClose();
							databaseConnectionSub.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("student", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 학생 정보 (UUID, Version) 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 학생 정보 (UUID, Version) 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						
						break;
					}
					
					
					case 5: {
						/**
						 * 온라인 출석부 관리 모드 : 5
						 * 
						 * 설명 :
						 * 		입력된 학교에 소속되어있는 모든 선생님 정보 출력 [ Version 정보와 UUID만 출력 ]
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							
							JsonArray array = new JsonArray();
							
							while (databaseConnection.getResultSet().next()) {
								JsonObject input = new JsonObject();
								
								String uuid = databaseConnection.getResultSet().getString("uuid");	
								int version = databaseConnection.getResultSet().getInt("version");
								
								
								input.addProperty("uuid", URLEncoder.encode(uuid, "UTF-8"));
								input.addProperty("version", version);
								array.add(input);
							}
							
							
							databaseConnection.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("teacher", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 선생님 정보 (UUID, Version) 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 선생님 정보 (UUID, Version) 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 6: {
						/**
						 * 온라인 출석부 관리 모드 : 6
						 * 
						 * 설명 :
						 * 		특정 선생님 정보 출력
						 * 		(단, 전화번호-이메일 주소 같은 민감 정보는 제외됨.)
						 * 
						 * 파라미터 :
						 * 		teacherUUID  --> 선생님 UUID
						 * 		authToken    --> Auth Token
						 */
						String tUUID = request.getParameter("uuid");
						String authToken = request.getParameter("authToken");
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_user_info WHERE uuid = ?;");
							databaseConnection.getPreparedStatement().setString(1, tUUID);
							databaseConnection.setResultSet();
							
							final String noValueText = "[NoValue]";
							
							if (databaseConnection.getResultSet().next()) {
								String uuid = databaseConnection.getResultSet().getString("uuid");	
								String name = databaseConnection.getResultSet().getString("name");	
								String name2 = databaseConnection.getResultSet().getString("name_no_duplication");
								String image = databaseConnection.getResultSet().getString("image");
								String description = databaseConnection.getResultSet().getString("description");
								String department1 = databaseConnection.getResultSet().getString("department1");
								String department2 = databaseConnection.getResultSet().getString("department2");
								String office_phone = databaseConnection.getResultSet().getString("office_phone");
								String position = databaseConnection.getResultSet().getString("position");
								String subject = databaseConnection.getResultSet().getString("subject");
								int schoolID = databaseConnection.getResultSet().getInt("school_id");
								int version = databaseConnection.getResultSet().getInt("version");
								
								obj.addProperty("uuid", URLEncoder.encode(uuid, "UTF-8"));
								obj.addProperty("name", URLEncoder.encode(name, "UTF-8"));
								
								
								if (name2 != null) 
									obj.addProperty("name2", URLEncoder.encode(name2, "UTF-8"));	
								else 
									obj.addProperty("name2", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (image != null) 
									obj.addProperty("image", URLEncoder.encode(image, "UTF-8"));
								else 
									obj.addProperty("image", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (description != null) 
									obj.addProperty("description", URLEncoder.encode(description, "UTF-8"));
								else 
									obj.addProperty("description", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (department1 != null) 
									obj.addProperty("department1", URLEncoder.encode(department1, "UTF-8"));
								else 
									obj.addProperty("department1", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (department2 != null) 
									obj.addProperty("department2", URLEncoder.encode(department2, "UTF-8"));
								else 
									obj.addProperty("department2", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (office_phone != null) 
									obj.addProperty("office_phone", URLEncoder.encode(office_phone, "UTF-8"));
								else 
									obj.addProperty("office_phone", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (position != null) 
									obj.addProperty("position", URLEncoder.encode(position, "UTF-8"));
								else 
									obj.addProperty("position", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								if (subject != null) 
									obj.addProperty("subject", URLEncoder.encode(subject, "UTF-8"));
								else 
									obj.addProperty("subject", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								obj.addProperty("school_id", schoolID);
								obj.addProperty("version", version);
								
								databaseConnection.allClose();
								
								PrintWriter out = response.getWriter(); 
								obj.addProperty(keyName_Result, successMessage);
								out.println(gson.toJson(obj));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 선생님 정보 출력 성공! Teacher UUID:", tUUID, ",Auth Token:", authToken)); 
							}else {
								databaseConnection.allClose();
								
								PrintWriter out = response.getWriter(); 
								obj.addProperty(keyName_Result, failMessage);
								out.println(gson.toJson(obj));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 선생님 정보 출력 실패! Teacher UUID:", tUUID, ",Auth Token:", authToken)); 
							}
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 선생님 정보 출력 실패! [로그인 실패] Teacher UUID:", tUUID, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 7: {
						/**
						 * 온라인 출석부 관리 모드 : 7
						 * 
						 * 설명 :
						 * 		특정 학생 정보 출력
						 * 
						 * 파라미터 :
						 * 		studentUUID  --> 학생 UUID
						 * 		authToken    --> Auth Token
						 */
						String sUUID = request.getParameter("uuid");
						String authToken = request.getParameter("authToken");
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_student_info WHERE student_uuid = ?;");
							databaseConnection.getPreparedStatement().setString(1, sUUID);
							databaseConnection.setResultSet();
							
							final String noValueText = "[NoValue]";
							if (databaseConnection.getResultSet().next()) {
								String student_uuid = databaseConnection.getResultSet().getString("student_uuid");	
								String student_class_uuid = databaseConnection.getResultSet().getString("student_class_uuid");	
								int student_number = databaseConnection.getResultSet().getInt("student_number");
								String student_name = databaseConnection.getResultSet().getString("student_name");
								String student_image = databaseConnection.getResultSet().getString("student_image");
								int gender = databaseConnection.getResultSet().getInt("gender");
								int move_out = databaseConnection.getResultSet().getInt("move_out");
								int year = databaseConnection.getResultSet().getInt("school_year");
								String note = databaseConnection.getResultSet().getString("note");
								int version = databaseConnection.getResultSet().getInt("version");
								
								
								obj.addProperty("student_uuid", URLEncoder.encode(student_uuid, "UTF-8"));
								obj.addProperty("student_class_uuid", URLEncoder.encode(student_class_uuid, "UTF-8"));
								obj.addProperty("student_number", student_number);
								obj.addProperty("student_name", URLEncoder.encode(student_name, "UTF-8"));
								
								
								if (student_image != null) 
									obj.addProperty("student_image", URLEncoder.encode(student_image, "UTF-8"));
								else 
									obj.addProperty("student_image", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								obj.addProperty("gender", gender);
								obj.addProperty("move_out", move_out);
								obj.addProperty("year", year);
								
					
								if (note != null) 
									obj.addProperty("note", URLEncoder.encode(note, "UTF-8"));
								else 
									obj.addProperty("note", URLEncoder.encode(noValueText, "UTF-8"));
								
								
								obj.addProperty("version", version);
								
								databaseConnection.allClose();
								
								PrintWriter out = response.getWriter(); 
								obj.addProperty(keyName_Result, successMessage);
								out.println(gson.toJson(obj));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 학생 정보 출력 성공! Student UUID:", sUUID, ",Auth Token:", authToken)); 
							}else {
								obj.addProperty(keyName_Result, failMessage);
								PrintWriter out = response.getWriter(); 
								out.println(gson.toJson(obj));
								
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 학생 정보 출력 성공! [로그인 실패] Student UUID:", sUUID, ",Auth Token:", authToken));
							}
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 학생 정보 출력 성공! [로그인 실패] Student UUID:", sUUID, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 8: {
						/**
						 * 온라인 출석부 관리 모드 : 8
						 * 
						 * 설명 :
						 * 		입력된 학교에 존재하는 모든 반 정보 출력
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_class_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							
							JsonArray array = new JsonArray();

							while (databaseConnection.getResultSet().next()) {
								JsonObject input = new JsonObject();
								
								
								String class_uuid = databaseConnection.getResultSet().getString("class_uuid");	
								String class_nickname = databaseConnection.getResultSet().getString("class_nickname");	
								String class_teacher_uuid = databaseConnection.getResultSet().getString("class_teacher_uuid");
								String version = databaseConnection.getResultSet().getString("version");
								
								input.addProperty("class_uuid", URLEncoder.encode(class_uuid, "UTF-8"));
								input.addProperty("class_nickname", URLEncoder.encode(class_nickname, "UTF-8"));
								input.addProperty("class_teacher_uuid", URLEncoder.encode(class_teacher_uuid, "UTF-8"));
								input.addProperty("school_id", school_id);
								input.addProperty("version", version);
								array.add(input);
							}
							
							
							databaseConnection.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("class", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 반 정보 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 반 정보 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 9: {
						/**
						 * 온라인 출석부 관리 모드 : 9
						 * 
						 * 설명 :
						 * 		입력된 학교에 존재하는 부서 정보 출력
						 * 
						 * 파라미터 :
						 * 		schoolid  --> 학교 아이디 입력
						 * 		authToken --> Auth Token
						 */
						String authToken = request.getParameter("authToken");
						int school_id = Integer.parseInt(request.getParameter("schoolid"));
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_department_info WHERE school_id = ?;");
							databaseConnection.getPreparedStatement().setInt(1, school_id);
							databaseConnection.setResultSet();
							
							JsonArray array = new JsonArray();

							while (databaseConnection.getResultSet().next()) {
								JsonObject input = new JsonObject();
								
								
								String uuid = databaseConnection.getResultSet().getString("uuid");	
								String name = databaseConnection.getResultSet().getString("name");
								String version = databaseConnection.getResultSet().getString("version");
								
								input.addProperty("uuid", URLEncoder.encode(uuid, "UTF-8"));
								input.addProperty("name", URLEncoder.encode(name, "UTF-8"));
								input.addProperty("version", version);
								array.add(input);
							}
							
							
							databaseConnection.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("department", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 부서 정보 출력 성공! School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "모든 부서 정보 출력 실패! [로그인 실패] School ID:", String.valueOf(school_id), ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 10: {
						/**
						 * 온라인 출석부 관리 모드 : 10
						 * 
						 * 설명 :
						 * 		특정 반에 대한 출석부 정보 불러오기
						 * 
						 * 파라미터 :
						 * 		date        --> 날자
						 * 		classUUID   --> 반 구분 아이디 입력
						 * 		authToken   --> Auth Token
						 */
						
						String authToken = request.getParameter("authToken");
						String classUUID = request.getParameter("classUUID");
						String date = request.getParameter("date");
						
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();
						
						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							
							date = URLDecoder.decode(date, "UTF-8");
							
							ArrayList<String> getStudentUUID = new ArrayList<String>();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_student_info WHERE student_class_uuid = ?;");
							databaseConnection.getPreparedStatement().setString(1, classUUID);
							databaseConnection.setResultSet();
							while (databaseConnection.getResultSet().next()) {
								getStudentUUID.add(databaseConnection.getResultSet().getString("student_uuid"));
							}
							
							
							int type = 0;
							String startDate = null;
							String endDate = null;
							if (!date.equals("all")) {
								if (date.contains("@")) {
									type = 1;
									
									String[] split = date.split("@");
									
									startDate = split[0];
									endDate = split[1];
								}else 
									type = 2;
								
							}else 
								type = 3;
							
							
							JsonArray array = new JsonArray();
							
							final String noValueText = "[NoValue]";
							
							for (String studentUUID : getStudentUUID) {
								databaseConnection.closePreparedStatement();
								databaseConnection.closeResultSet();
								
								if (type == 1) {
									databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_info WHERE attendance_date BETWEEN ? AND ? AND student_uuid = ? order by attendance_date");
									databaseConnection.getPreparedStatement().setString(1, startDate);
									databaseConnection.getPreparedStatement().setString(2, endDate);
									databaseConnection.getPreparedStatement().setString(3, studentUUID);
								}else if (type == 2) {
									databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_info WHERE attendance_date = ? AND student_uuid = ? order by attendance_date;");
									databaseConnection.getPreparedStatement().setString(1, date);
									databaseConnection.getPreparedStatement().setString(2, studentUUID);
								}else {
									databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_info WHERE student_uuid = ? order by attendance_date;");
									databaseConnection.getPreparedStatement().setString(1, studentUUID);
								}
								
								databaseConnection.setResultSet();
							

								while (databaseConnection.getResultSet().next()) {
									JsonObject input = new JsonObject();
									
									String student_uuid = databaseConnection.getResultSet().getString("student_uuid");	
									java.sql.Date attendance_date = databaseConnection.getResultSet().getDate("attendance_date");
									int value_1 = databaseConnection.getResultSet().getInt("value_1");
									String value_1_teacher = databaseConnection.getResultSet().getString("value_1_teacher");
									int value_2 = databaseConnection.getResultSet().getInt("value_2");
									String value_2_teacher = databaseConnection.getResultSet().getString("value_2_teacher");
									int value_3 = databaseConnection.getResultSet().getInt("value_3");
									String value_3_teacher = databaseConnection.getResultSet().getString("value_3_teacher");
									int value_4 = databaseConnection.getResultSet().getInt("value_4");
									String value_4_teacher = databaseConnection.getResultSet().getString("value_4_teacher");
									int value_5 = databaseConnection.getResultSet().getInt("value_5");
									String value_5_teacher = databaseConnection.getResultSet().getString("value_5_teacher");
									int value_6 = databaseConnection.getResultSet().getInt("value_6");
									String value_6_teacher = databaseConnection.getResultSet().getString("value_6_teacher");
									int value_7 = databaseConnection.getResultSet().getInt("value_7");
									String value_7_teacher = databaseConnection.getResultSet().getString("value_7_teacher");
									int value_8 = databaseConnection.getResultSet().getInt("value_8");
									String value_8_teacher = databaseConnection.getResultSet().getString("value_8_teacher");
									int value_9 = databaseConnection.getResultSet().getInt("value_9");
									String value_9_teacher = databaseConnection.getResultSet().getString("value_9_teacher");
									int value_10 = databaseConnection.getResultSet().getInt("value_10");
									String value_10_teacher = databaseConnection.getResultSet().getString("value_10_teacher");
									String note = databaseConnection.getResultSet().getString("note");	
									
									
									SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
									
									input.addProperty("student_uuid", URLEncoder.encode(student_uuid, "UTF-8"));
									input.addProperty("attendance_date", simpleDateFormat.format(attendance_date));
									input.addProperty("value_1", value_1);
									input.addProperty("value_2", value_2);
									input.addProperty("value_3", value_3);
									input.addProperty("value_4", value_4);
									input.addProperty("value_5", value_5);
									input.addProperty("value_6", value_6);
									input.addProperty("value_7", value_7);
									input.addProperty("value_8", value_8);
									input.addProperty("value_9", value_9);
									input.addProperty("value_10", value_10);
									
									
									
									if (value_1_teacher != null) 
										input.addProperty("value_1_teacher", URLEncoder.encode(value_1_teacher, "UTF-8"));
									else 
										input.addProperty("value_1_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_2_teacher != null) 
										input.addProperty("value_2_teacher", URLEncoder.encode(value_2_teacher, "UTF-8"));
									else 
										input.addProperty("value_2_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_3_teacher != null) 
										input.addProperty("value_3_teacher", URLEncoder.encode(value_3_teacher, "UTF-8"));
									else 
										input.addProperty("value_3_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_4_teacher != null) 
										input.addProperty("value_4_teacher", URLEncoder.encode(value_4_teacher, "UTF-8"));
									else 
										input.addProperty("value_4_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_5_teacher != null) 
										input.addProperty("value_5_teacher", URLEncoder.encode(value_5_teacher, "UTF-8"));
									else 
										input.addProperty("value_5_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_6_teacher != null) 
										input.addProperty("value_6_teacher", URLEncoder.encode(value_6_teacher, "UTF-8"));
									else 
										input.addProperty("value_6_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_7_teacher != null) 
										input.addProperty("value_7_teacher", URLEncoder.encode(value_7_teacher, "UTF-8"));
									else 
										input.addProperty("value_7_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_8_teacher != null) 
										input.addProperty("value_8_teacher", URLEncoder.encode(value_8_teacher, "UTF-8"));
									else 
										input.addProperty("value_8_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_9_teacher != null) 
										input.addProperty("value_9_teacher", URLEncoder.encode(value_9_teacher, "UTF-8"));
									else 
										input.addProperty("value_9_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (value_10_teacher != null) 
										input.addProperty("value_10_teacher", URLEncoder.encode(value_10_teacher, "UTF-8"));
									else 
										input.addProperty("value_10_teacher", URLEncoder.encode(noValueText, "UTF-8"));
									
									if (note != null) 
										input.addProperty("note", URLEncoder.encode(note, "UTF-8"));
									else 
										input.addProperty("note", URLEncoder.encode(noValueText, "UTF-8"));
									
									array.add(input);
								}
							}
							
							
							databaseConnection.allClose();
							
							PrintWriter out = response.getWriter(); 
							obj.addProperty(keyName_Result, successMessage);
							obj.add("student", array);
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 반에 대한 출석부 정보 불러오기 성공! Class UUID:", classUUID, ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv9(clientIP, "특정 반에 대한 출석부 정보 불러오기 실패! [로그인 실패] Class UUID:", classUUID, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 11: {
						/**
						 * 온라인 출석부 관리 모드 : 11
						 * 
						 * 설명 :
						 * 		특정 학생에 대한 'note' 정보 수정
						 * 
						 * 파라미터 :
						 * 		date        --> 날자
						 * 		studentUUID --> 학생 구분 아이디 입력
						 * 		description --> 삽입 데이터
						 * 		authToken   --> Auth Token
						 */
						
						String authToken = request.getParameter("authToken");
						String date = request.getParameter("date");
						String description = request.getParameter("description");
						String studentUUID = request.getParameter("studentUUID");
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();

						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_info WHERE student_uuid = ? AND attendance_date = ?;");
							databaseConnection.getPreparedStatement().setString(1, studentUUID);
							databaseConnection.getPreparedStatement().setString(2, date);
							databaseConnection.setResultSet();

							boolean isTaskForInsert = false;
							
							byte[] decodedBytes = Base64.decodeBase64(URLDecoder.decode(description, "UTF-8"));
							description = new String(decodedBytes);
							description = URLDecoder.decode(description, "UTF-8");
							description = description.replaceAll("</br>", System.lineSeparator());

							if (databaseConnection.getResultSet().next()) {
								databaseConnection.closeResultSet();
								databaseConnection.closePreparedStatement();
								
								databaseConnection.setPreparedStatement("UPDATE online_attendance_info SET note = ? WHERE student_uuid = ? AND attendance_date = ?;");
								if (description.equals("[NoValue]")) {
									databaseConnection.getPreparedStatement().setString(1, null);									
								}else {
									databaseConnection.getPreparedStatement().setString(1, description);
								}

								databaseConnection.getPreparedStatement().setString(2, studentUUID);
								databaseConnection.getPreparedStatement().setString(3, date);
								databaseConnection.executeUpdate();
							}else {
								isTaskForInsert = true;
								
								databaseConnection.closeResultSet();
								databaseConnection.closePreparedStatement();
								
								databaseConnection.setPreparedStatement("INSERT INTO online_attendance_info VALUE ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
								databaseConnection.getPreparedStatement().setString(1, studentUUID);
								databaseConnection.getPreparedStatement().setString(2, date);
								databaseConnection.getPreparedStatement().setInt(3, -1);
								databaseConnection.getPreparedStatement().setString(4, null);
								databaseConnection.getPreparedStatement().setInt(5, -1);
								databaseConnection.getPreparedStatement().setString(6, null);
								databaseConnection.getPreparedStatement().setInt(7, -1);
								databaseConnection.getPreparedStatement().setString(8, null);
								databaseConnection.getPreparedStatement().setInt(9, -1);
								databaseConnection.getPreparedStatement().setString(10, null);
								databaseConnection.getPreparedStatement().setInt(11, -1);
								databaseConnection.getPreparedStatement().setString(12, null);
								databaseConnection.getPreparedStatement().setInt(13, -1);
								databaseConnection.getPreparedStatement().setString(14, null);
								databaseConnection.getPreparedStatement().setInt(15, -1);
								databaseConnection.getPreparedStatement().setString(16, null);
								databaseConnection.getPreparedStatement().setInt(17, -1);
								databaseConnection.getPreparedStatement().setString(18, null);
								databaseConnection.getPreparedStatement().setInt(19, -1);
								databaseConnection.getPreparedStatement().setString(20, null);
								databaseConnection.getPreparedStatement().setInt(21, -1);
								databaseConnection.getPreparedStatement().setString(22, null);
								if (description.equals("[NoValue]")) {
									databaseConnection.getPreparedStatement().setString(23, null);									
								}else {
									databaseConnection.getPreparedStatement().setString(23, description);
								}
								databaseConnection.executeUpdate();
							}
							
							databaseConnection.allClose();
							
							obj.addProperty(keyName_Result, successMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "특정 학생에 대한 'note' 정보 수정 성공! Student UUID:", studentUUID, ",Date:", date, ",Value:", description, ",IsNewInsert", String.valueOf(isTaskForInsert), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "특정 학생에 대한 'note' 정보 수정 실패! [로그인 실패] Student UUID:", studentUUID, ",Date:", date, ",Value:", description, ",IsNewInsert", null, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					case 12: {
						/**
						 * 온라인 출석부 관리 모드 : 12
						 * 
						 * 설명 :
						 * 		특정 학생에 대한 출석 정보 수정
						 * 
						 * 파라미터 :
						 * 		date        --> 날자
						 * 		studentUUID --> 학생 구분 아이디 입력
						 * 		time        --> 수업 시간
						 * 		value       --> 출석부 시간
						 * 		authToken   --> Auth Token
						 */
						
						String authToken = request.getParameter("authToken");
						String date = request.getParameter("date");
						String studentUUID = request.getParameter("studentUUID");
						int time = Integer.parseInt(request.getParameter("time"));
						int value = Integer.parseInt(request.getParameter("value"));
						OnlineAttendanceAccessChecker accessChecker = new OnlineAttendanceAccessChecker();

						if (accessChecker.isAccessCheck(authToken)) {
							DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
							databaseConnection.init();
							databaseConnection.connection();
							databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_info WHERE student_uuid = ? AND attendance_date = ?;");
							databaseConnection.getPreparedStatement().setString(1, studentUUID);
							databaseConnection.getPreparedStatement().setString(2, date);
							databaseConnection.setResultSet();

							
							boolean isTaskForInsert = false;
							
							
							if (!databaseConnection.getResultSet().next()) {
								databaseConnection.closeResultSet();
								databaseConnection.closePreparedStatement();
								
								isTaskForInsert = true;
								
								databaseConnection.setPreparedStatement("INSERT INTO online_attendance_info VALUE ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
								databaseConnection.getPreparedStatement().setString(1, studentUUID);
								databaseConnection.getPreparedStatement().setString(2, date);
								databaseConnection.getPreparedStatement().setInt(3, -1);
								databaseConnection.getPreparedStatement().setString(4, null);
								databaseConnection.getPreparedStatement().setInt(5, -1);
								databaseConnection.getPreparedStatement().setString(6, null);
								databaseConnection.getPreparedStatement().setInt(7, -1);
								databaseConnection.getPreparedStatement().setString(8, null);
								databaseConnection.getPreparedStatement().setInt(9, -1);
								databaseConnection.getPreparedStatement().setString(10, null);
								databaseConnection.getPreparedStatement().setInt(11, -1);
								databaseConnection.getPreparedStatement().setString(12, null);
								databaseConnection.getPreparedStatement().setInt(13, -1);
								databaseConnection.getPreparedStatement().setString(14, null);
								databaseConnection.getPreparedStatement().setInt(15, -1);
								databaseConnection.getPreparedStatement().setString(16, null);
								databaseConnection.getPreparedStatement().setInt(17, -1);
								databaseConnection.getPreparedStatement().setString(18, null);
								databaseConnection.getPreparedStatement().setInt(19, -1);
								databaseConnection.getPreparedStatement().setString(20, null);
								databaseConnection.getPreparedStatement().setInt(21, -1);
								databaseConnection.getPreparedStatement().setString(22, null);
								databaseConnection.getPreparedStatement().setString(23, null);
								databaseConnection.executeUpdate();
							}
							
							
							databaseConnection.closeResultSet();
							databaseConnection.closePreparedStatement();
							
							databaseConnection.stringBuilder.append("UPDATE online_attendance_info SET");
							
							switch (time) {
								case 1:
									databaseConnection.stringBuilder.append(" value_1 = ?, value_1_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;

								case 2: 
									databaseConnection.stringBuilder.append(" value_2 = ?, value_2_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 3: 
									databaseConnection.stringBuilder.append(" value_3 = ?, value_3_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 4: 
									databaseConnection.stringBuilder.append(" value_4 = ?, value_4_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 5: 
									databaseConnection.stringBuilder.append(" value_5 = ?, value_5_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 6: 
									databaseConnection.stringBuilder.append(" value_6 = ?, value_6_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 7: 
									databaseConnection.stringBuilder.append(" value_7 = ?, value_7_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 8: 
									databaseConnection.stringBuilder.append(" value_8 = ?, value_8_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 9: 
									databaseConnection.stringBuilder.append(" value_9 = ?, value_9_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
									
								case 10: 
									databaseConnection.stringBuilder.append(" value_10 = ?, value_10_teacher = ? WHERE student_uuid = ? AND attendance_date = ?;");
									break;
							}
							
							OnlineAttendanceTeacherVO onlineAttendanceTeacherVO = accessChecker.getTeacherInfo(authToken);
							
							
							databaseConnection.setPreparedStatement(databaseConnection.stringBuilder.toString());
							databaseConnection.getPreparedStatement().setInt(1, value);
							databaseConnection.getPreparedStatement().setString(2, onlineAttendanceTeacherVO.getUuid());
							databaseConnection.getPreparedStatement().setString(3, studentUUID);
							databaseConnection.getPreparedStatement().setString(4, date);
							databaseConnection.executeUpdate();
							
							
							databaseConnection.allClose();
							
							obj.addProperty(keyName_Result, successMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "특정 학생에 대한 출석 정보 수정 성공! Student UUID:", studentUUID, ",Date", date, ",Value:", String.valueOf(value), ",IsNewInsert", String.valueOf(isTaskForInsert), ",Auth Token:", authToken));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv11(clientIP, "특정 학생에 대한 출석 정보 수정 실패! [로그인 실패] Student UUID:", studentUUID, ",Date", date, ",Value:", String.valueOf(value), ",IsNewInsert", null, ",Auth Token:", authToken));
						}
						
						break;
					}
					
					
					
					case 13: {
						/**
						 * 온라인 출석부 관리 모드 : 13
						 * 
						 * 설명 :
						 * 		앱 다운로드
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
					    final String root = PathManager.ONLINE_ATTENDANCE_APK_PATH;
					    // 실제 내보낼 파일명
					    String orgfilename = "oa_update_apk.apk";    
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
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "온라인 출석부 APK 파일 다운로드 성공!"));
						}else {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "File does not exist! 다시 시도해주십시오. [ 온라인 출석부 APK 파일 다운로드 실패 ]"));
							
							obj.addProperty(keyName_Result, failMessage);
							obj.addProperty(keyName_Message, URLEncoder.encode("File does not exist! 다시 시도해주십시오.", "UTF-8"));
							
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
						}
					}
				}
			}else {
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
			}
		}catch (Exception e) {
			// TODO: handle exception
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 오류가 발생 하였습니다. ".concat(e.getMessage()), "UTF-8"));
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, e.toString()));
		}
	}
	
	
	
	
	// 선생님 정보 출력 제한 확인
	private int checkTeacherPrivateInfo(String uuid) throws SQLException, ClassNotFoundException {
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		databaseConnection.connection();
		databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_private_info WHERE uuid = ?;");
		databaseConnection.getPreparedStatement().setString(1, uuid);
		databaseConnection.setResultSet();
		
		if (databaseConnection.getResultSet().next()) {
			int phone = databaseConnection.getResultSet().getInt("value_phone");
			int email = databaseConnection.getResultSet().getInt("value_email");
			databaseConnection.allClose();

			if (phone == 1 && email == 1) return 1;
			if (phone == 1) return 2;
			if (email == 1) return 3;
			
			return 0;
		}else {
			databaseConnection.allClose();
			
			return 4;
		}
	}
}
