<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.io.File.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="org.apache.commons.io.FileUtils"%>
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
<%@ page import="kro.kr.rhya_network.utaite_player.UtaitePlayerTicketManager"%>
<%@ page import="kro.kr.rhya_network.email.EmailSendDATA"%>
<%@ page import="kro.kr.rhya_network.email.SendEmail"%>
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
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Utaite_Player_Licenses_Application_Task)) {
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		if (login_session != null) {
			// 자동 로그인
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			
			if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				// 이용권 확인
				UtaitePlayerTicketManager utaitePlayerTicketManager = new UtaitePlayerTicketManager();
				if (utaitePlayerTicketManager.isAccessCheck(auto_login_result[1])) {
					obj.addProperty(keyName_Result, failMessage);
					obj.addProperty(keyName_Message, "이미 이용권을 소유하고 있습니다.");
					out.println(gson.toJson(obj));
					
					rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv9(clientIP, "이미 이용권을 소유하고 있습니다.", "/", "USER:", auto_login_result[1]));
				}else {
					String date = utaitePlayerTicketManager.ticketApplicationState(auto_login_result[1]);
					if (date == null) {
						EmailSendDATA.UtaitePlayerTicketApplication emailSendData = new EmailSendDATA.UtaitePlayerTicketApplication();
						EmailSendDATA.ForgotPassword emailSendDataForgotPW = new EmailSendDATA.ForgotPassword();
						SendEmail sendEmail = new SendEmail();
						// 메일 데이터
						final String email_url = emailSendData.Url(request, auto_login_result[1]);
						// 메일 전송
						sendEmail.Send(sendEmail.GetProperties(), emailSendData.Html(emailSendDataForgotPW.html_text, auto_login_result[3], auto_login_result[1], email_url), emailSendData.Title(auto_login_result[3]), EmailSendDATA.ADMIN_EMAIL);
						
						utaitePlayerTicketManager.ticketApplication(auto_login_result[1]);
						
						obj.addProperty(keyName_Result, successMessage);
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv9(clientIP, "이용권 신청 성공", "/", "USER:", auto_login_result[1]));
					}else {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, "이용권 승인 대기 중인 계정입니다.");
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv9(clientIP, "이용권 승인 대기 중", "/", "USER:", auto_login_result[1]));
					}
				}
			}else {
				obj.addProperty(keyName_Result, failMessage);
				obj.addProperty(keyName_Message, "로그인이 되어있지 않습니다.");
				out.println(gson.toJson(obj));
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv9(clientIP, "로그인이 되어있지 않습니다.", "/", "USER:", auto_login_result[1]));
			}
		}else {
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, "로그인이 되어있지 않습니다.");
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv9(clientIP, "로그인이 되어있지 않습니다.", "/", "USER:", null));
		}
	}else {
		obj.addProperty(keyName_Result, failMessage);
		obj.addProperty(keyName_Message, "JSP 페이지 초기화 중 오류가 발생하였습니다.");
		out.println(gson.toJson(obj));
		
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
	}
}catch(Exception ex) {
	obj.addProperty(keyName_Result, failMessage);
	obj.addProperty(keyName_Message,"알 수 없는 오류가 발생 하였습니다. ".concat(ex.getMessage()));
	out.println(gson.toJson(obj));
	
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
}
%>