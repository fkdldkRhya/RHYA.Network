<%@ page import="kro.kr.rhya_network.utils.online_attendance.OnlineAttendanceTeacherVO"%>
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
<%@ page import="kro.kr.rhya_network.utils.online_attendance.OnlineAttendanceAccessChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
// Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
// Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);

// ��� ���
Gson gson = new Gson();
JsonObject obj = new JsonObject();
final String successMessage = "success";
final String failMessage = "fail";
final String keyName_Result = "result";
final String keyName_Message = "message";


try {
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Online_Attendance_Account_Sync)) {
		// �Ķ���� �Է�
		String mode = request.getParameter("mode");
		int modeInt = 0;
		
		if (mode != null) {
			try {
				modeInt = Integer.parseInt(mode);
			}catch(Exception ex) { modeInt = -1; }
		}
		
		switch (modeInt) {
			default:
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���.", "UTF-8"));
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "�� �� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���."));
				break;
				
				
			case 0: {
				String authToken = request.getParameter("authToken");
				OnlineAttendanceAccessChecker onlineAttendanceAccessChecker = new OnlineAttendanceAccessChecker();

				if (onlineAttendanceAccessChecker.isAccessCheck(authToken)) {
					obj.addProperty(keyName_Result, successMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode("�ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ��ֽ��ϴ�.", "UTF-8"));
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "check_sync | �ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ��ֽ��ϴ�."));
				}else {
					obj.addProperty(keyName_Result,failMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode("�ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ����� �ʽ��ϴ�.", "UTF-8"));
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "check_sync | �ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ����� �ʽ��ϴ�."));
				}
				
				break;
			}
				
				
			case 1: {
				String authToken = request.getParameter("authToken");
				String authorizationKey = request.getParameter("authorizationKey");
				
				OnlineAttendanceAccessChecker onlineAttendanceAccessChecker = new OnlineAttendanceAccessChecker();
				int result = onlineAttendanceAccessChecker.requestChecker(request, authToken, authorizationKey);
				
				switch (result) {
					case 0:{
						obj.addProperty(keyName_Result, "NO_AUTH_TOKEN");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / Auth token ���� Null��"));
						
						break;
					}
					
					case 1: {
						obj.addProperty(keyName_Result, "NO_SYNC_KEY_FOR_DB");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / ���� ���� �ڵ尡 �����ͺ��̽����� �������� ����"));
						
						break;
					}
					
					case 2: {
						obj.addProperty(keyName_Result, "MATCH_SYNC_KEY");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / ���� ���� �ڵ尡 ��ġ��"));
						
						break;
					}
					
					case 3: {
						obj.addProperty(keyName_Result, "NOT_MATCH_SYNC_KEY");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / ���� ���� �ڵ� ��ġ���� ����"));
						
						break;
					}
					
					case 4: {
						obj.addProperty(keyName_Result, "EMAIL_ALREADY_SEND");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / �̸����� �̹� ���۵�"));
						
						break;
					}
					
					case 5: {
						obj.addProperty(keyName_Result, "UPDATE_AND_SEND_EMAIL_SUCCESS");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / account_sync_email�� �����ϰ� ������ �߼ۿ� ������"));
						
						break;
					}
					
					case 6: {
						obj.addProperty(keyName_Result, "INSERT_AND_SEND_EMAIL_SUCCESS");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / ���� ���� �����͸� �����ͺ��̽��� �����ϰ� ������ �߼ۿ� ������"));
						
						break;
					}
					
					case 7: {
						obj.addProperty(keyName_Result, "INSERT_AND_SEND_EMAIL_FAIL");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / ���� ���� ������ ���� �Ǵ� �̸��� ������ ������"));
						
						break;
					}
					
					case 8: {
						obj.addProperty(keyName_Result, "UNKNOWN_ERROR");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "request_sync | ���� ����ȭ ��û ���� / �� �� ���� ����"));
						
						break;
					}
					
					
				}
				break;
			}
			
			
			case 2: {
				String authToken = request.getParameter("authToken");
				OnlineAttendanceAccessChecker onlineAttendanceAccessChecker = new OnlineAttendanceAccessChecker();

				OnlineAttendanceTeacherVO onlineAttendanceTeacherVO = onlineAttendanceAccessChecker.getTeacherInfo(authToken);
				
				if (onlineAttendanceTeacherVO != null) {
					final String noValueText = "[NoValue]";
					
					obj.addProperty(keyName_Result, successMessage);
					obj.addProperty("uuid", URLEncoder.encode(onlineAttendanceTeacherVO.getUuid(), "UTF-8"));
					obj.addProperty("name", URLEncoder.encode(onlineAttendanceTeacherVO.getName(), "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getName2() != null) 
						obj.addProperty("name2", URLEncoder.encode(onlineAttendanceTeacherVO.getName2(), "UTF-8"));	
					else 
						obj.addProperty("name2", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getImage() != null) 
						obj.addProperty("image", URLEncoder.encode(onlineAttendanceTeacherVO.getImage(), "UTF-8"));
					else 
						obj.addProperty("image", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getDescription() != null) 
						obj.addProperty("description", URLEncoder.encode(onlineAttendanceTeacherVO.getDescription(), "UTF-8"));
					else 
						obj.addProperty("description", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getDepartment1() != null) 
						obj.addProperty("department1", URLEncoder.encode(onlineAttendanceTeacherVO.getDepartment1(), "UTF-8"));
					else 
						obj.addProperty("department1", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getDepartment2() != null) 
						obj.addProperty("department2", URLEncoder.encode(onlineAttendanceTeacherVO.getDepartment2(), "UTF-8"));
					else 
						obj.addProperty("department2", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getEmail() != null) 
						obj.addProperty("email_address", URLEncoder.encode(onlineAttendanceTeacherVO.getEmail(), "UTF-8"));
					else 
						obj.addProperty("email_address", URLEncoder.encode(noValueText, "UTF-8"));

					
					if (onlineAttendanceTeacherVO.getMobile_phone() != null) 
						obj.addProperty("mobile_phone", URLEncoder.encode(onlineAttendanceTeacherVO.getMobile_phone(), "UTF-8"));
					else 
						obj.addProperty("mobile_phone", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getOffice_phone() != null) 
						obj.addProperty("office_phone", URLEncoder.encode(onlineAttendanceTeacherVO.getOffice_phone(), "UTF-8"));
					else 
						obj.addProperty("office_phone", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getPosition() != null) 
						obj.addProperty("position", URLEncoder.encode(onlineAttendanceTeacherVO.getPosition(), "UTF-8"));
					else 
						obj.addProperty("position", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					if (onlineAttendanceTeacherVO.getSubject() != null) 
						obj.addProperty("subject", URLEncoder.encode(onlineAttendanceTeacherVO.getSubject(), "UTF-8"));
					else 
						obj.addProperty("subject", URLEncoder.encode(noValueText, "UTF-8"));
					
					
					obj.addProperty("school_id", onlineAttendanceTeacherVO.getSchool_id());
					obj.addProperty("version", onlineAttendanceTeacherVO.getVersion());
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "get_sync_account | ���� ������ �������µ� �����߽��ϴ�."));
				}else {
					obj.addProperty(keyName_Result,failMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode("�ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ����� �ʽ��ϴ�.", "UTF-8"));
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "get_sync_account | �ش� ������ �¶��� �⼮�� ���񽺿� �����Ǿ����� �ʽ��ϴ�."));
				}
				
				break;
			}
			
			
			case 3: {
				int schoolID = Integer.parseInt(request.getParameter("schoolid"));
				
				DatabaseManager.DatabaseConnection db = new DatabaseManager.DatabaseConnection();
				db.init();
				db.connection();
				db.setPreparedStatement("SELECT school_name FROM online_attendance_school_info WHERE school_id = ?");
				db.getPreparedStatement().setInt(1, schoolID);
				db.setResultSet();
				
				if (db.getResultSet().next()) {
					obj.addProperty(keyName_Result, successMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode(db.getResultSet().getString("school_name"), "UTF-8"));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "request_school_id | �¶��� �⼮�� �б� ��ȸ ����! ID: ", String.valueOf(schoolID)));
				}else {
					obj.addProperty(keyName_Result, failMessage);
					obj.addProperty(keyName_Message, URLEncoder.encode(db.getResultSet().getString("school_name"), "UTF-8"));
					
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "request_school_id | �¶��� �⼮�� �б� ��ȸ ����! ID: ", String.valueOf(schoolID)));
				}
				
				out.println(gson.toJson(obj));
				
				db.allClose();
				
				break;
			}
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP ������ �ʱ�ȭ �� ������ �߻��Ͽ����ϴ�."));
	}
}catch(Exception ex) {
	obj.addProperty(keyName_Result, failMessage);
	obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� ������ �߻� �Ͽ����ϴ�. ".concat(ex.getMessage()), "UTF-8"));
	out.println(gson.toJson(obj));
	
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
}


%>