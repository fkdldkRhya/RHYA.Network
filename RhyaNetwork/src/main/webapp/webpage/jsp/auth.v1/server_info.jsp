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
// Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
// Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// ���� �ۼ� StringBuilder
StringBuilder sql = new StringBuilder();

// Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);

// �����ͺ��̽� Ŀ����
DatabaseManager.DatabaseConnection cont = new DatabaseManager.DatabaseConnection();

// ��� ���
Gson gson = new Gson();
JsonObject obj = new JsonObject();

// ���� ó��
try {
	// DB ����
	cont.init();
	cont.connection();
	cont.setPreparedStatement("SELECT * FROM rhya_network_info;");
	cont.setResultSet();
	if (cont.getResultSet().next()) {
		// ��� ����
		obj.addProperty("server", "open");
		obj.addProperty("privacy_policy_version", cont.getResultSet().getString("privacy_policy_version"));
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA.Network ���� ���� ��� ����!"));
		// ��� ���
		out.println(gson.toJson(obj));	
	}else {
		// ��� ����
		obj.addProperty("server", "error");
		obj.addProperty("privacy_policy_version", "0.0.0");
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA.Network ���� ���� ��� ����!"));
		// ��� ���
		out.println(gson.toJson(obj));
	}
}catch (Exception ex) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// ��� ����
	obj.addProperty("result", "fail");
	// ��� ���
	out.println(gson.toJson(obj));
}finally {
	// ���� ����
	cont.allClose();	
}
%>