<%@ page import="kro.kr.rhya_network.utils.db.DatabaseManager"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<%
// Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
// Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// 쿼리 작성 StringBuilder
StringBuilder sql = new StringBuilder();

// 클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

// 데이터베이스 커넥터
DatabaseManager.DatabaseConnection cont = new DatabaseManager.DatabaseConnection();

// 출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();

// 예외 처리
try {
	// DB 접속
	cont.init();
	cont.connection();
	cont.setPreparedStatement("SELECT * FROM rhya_network_info;");
	cont.setResultSet();
	if (cont.getResultSet().next()) {
		// 결과 설정
		obj.addProperty("server", "open");
		obj.addProperty("privacy_policy_version", cont.getResultSet().getString("privacy_policy_version"));
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA.Network 서버 정보 출력 성공!"));
		// 결과 출력
		out.println(gson.toJson(obj));	
	}else {
		// 결과 설정
		obj.addProperty("server", "error");
		obj.addProperty("privacy_policy_version", "0.0.0");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA.Network 서버 정보 출력 실패!"));
		// 결과 출력
		out.println(gson.toJson(obj));
	}
}catch (Exception ex) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// 결과 설정
	obj.addProperty("result", "fail");
	// 결과 출력
	out.println(gson.toJson(obj));
}finally {
	// 연결 종료
	cont.allClose();	
}
%>