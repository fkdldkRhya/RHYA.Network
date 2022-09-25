<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.SelfXSSFilter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.RhyaSHA512"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.DateTimeChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>

<%
//���� Ŭ���� ����
PageParameter.ForgotPWD forgotpwdV = new PageParameter.ForgotPWD();
reCaptChaInfo captchaV = new reCaptChaInfo();
RhyaSHA512 rhyaSHA512 = new RhyaSHA512();

// URL ���� ���� Ȯ��
String strReferer = request.getHeader("referer");
if(strReferer == null) {
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
	return;
}

//Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
//Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

//���� �ۼ� StringBuilder
StringBuilder sql = new StringBuilder();

//Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);

//�����ͺ��̽� Ŀ���� ���� ����
DatabaseConnection cont = new DatabaseConnection();
//�����ͺ��̽� ���� ���� ���� ����
PreparedStatement stat = null;
ResultSet rs = null;


//------------------------------------------------
if (!IPBlockChecker.isIPBlock(clientIP)) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP ���� ��Ͽ� �ִ� ȣ��Ʈ�� ������ �õ��Ͽ����ϴ�. �ش� ȣ��Ʈ�� ������ �ý����� �ź��߽��ϴ�."));
	// ������ �̵�
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	rd.forward(request,response);
	return;
}
//------------------------------------------------


//�����ͺ��̽� ���� ���� ó��
try {
	// �����ͺ��̽� ����
	cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
					DatabaseInfo.DATABASE_CONNECTION_URL,
					DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
					DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
}catch (SQLException ex1) {
	// �����ͺ��̽� ���� ���� ó��
	cont.Close();
	// �α� �ۼ�
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
	// ������ �̵�
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
}catch (ClassNotFoundException ex2) {
	// �����ͺ��̽� ���� ���� ó��
	cont.Close();
	// �α� �ۼ�
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
	// ������ �̵�
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
}

//������ ���� Ȯ��
if (cont != null) {
	// ���� ����
	sql.append("SELECT * FROM ");
	sql.append(DatabaseInfo.DATABASE_TABLE_NAME_JSP_PAGE_SETTING);
	sql.append(" WHERE ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_ID);
	sql.append("=");
	sql.append("?;");

	
	// ���� ����
	stat = cont.GetConnection().prepareStatement(sql.toString());
	stat.setInt(1, JspPageInfo.PageID_User_Account_ForgotPW_Input_Task);
	// ���� ���� StringBuilder �ʱ�ȭ
	sql.delete(0,sql.length());
	// ���� ����
	rs = stat.executeQuery();
	// ���� ���� ���
	int state = 0;
	if (rs.next()) {
		state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
	}
	// ���� Ȯ�� - ��� ó��
	if (!JspPageInfo.JspPageStateManager(state)) {
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		forgotpwdV = null;
		captchaV = null;
		
		// ������ �̵�
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
		return;
	}
}

//��� ���
Gson gson = new Gson();
JsonObject obj = new JsonObject();

//�Ķ���� ��������
String pw = URLDecoder.decode(request.getParameter(forgotpwdV.PASSWORD), "UTF-8");
String pwc = URLDecoder.decode(request.getParameter(forgotpwdV.PASSWORD_C), "UTF-8");
String uuid = URLDecoder.decode(request.getParameter(forgotpwdV.UUID_USER), "UTF-8");
String auth = URLDecoder.decode(request.getParameter(forgotpwdV.UUID_AUTH), "UTF-8");
String key = request.getParameter(forgotpwdV.INT_KEY);
String token = request.getParameter(forgotpwdV.RE_CHAPT_CHA);

//Null Ȯ��
if (pw == null ||
    pwc == null ||
	key == null ||
	token == null) {
	
	// ��� ����
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "�Է°��� Null�� ���ԵǸ� �� �˴ϴ�.");
	// �α� ���
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : �Է°��� Null�� ���ԵǸ� �� �˴ϴ�."));
	// ��� ���
	out.println(gson.toJson(obj));
	// ���� ����
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	forgotpwdV = null;
	captchaV = null;
	
	return;
}

//���� ó��
try {
	// ���� ������ ������ ����
	String int_random_key_org = (String) session.getAttribute(ParameterManipulation.INTRandomKeySession);
	// ���� ������ ��ȣȭ
	int_random_key_org = RhyaAES.AES_Decode(int_random_key_org);
	// �Ķ���� ��ȣȭ
	key = RhyaAES.AES_Decode(key);
	// �α� ���
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { forgotpwdV.PASSWORD, forgotpwdV.PASSWORD_C, forgotpwdV.INT_KEY, forgotpwdV.RE_CHAPT_CHA },
															  new String[] { pw, pwc, key, token }));
	// XSS ���͸�
	key = SelfXSSFilter.TextXSSFilter(key);
	// ������ ����Ű ��
	if (!int_random_key_org.equals(key)) {
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "����Ű�� ��ġ���� �ʽ��ϴ�.");
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : ����Ű�� ��ġ���� �ʽ��ϴ�."));
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		forgotpwdV = null;
		captchaV = null;
		
		return;
	}
	// reCaptCha �˻�
	if (!captchaV.reCaptChaChecker(token)) {
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "Google reCAPTCHA v3�� ������� ���߽��ϴ�.");
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Google reCAPTCHA v3�� ������� ���߽��ϴ�."));
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		captchaV = null;
		
		return;
	}
	// ���� ����
	sql.append("SELECT ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
	sql.append(",");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
	sql.append(" FROM ");
	sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
	sql.append(" WHERE ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
	sql.append(" = ?;");
	// �����ͺ��̽� ����
	stat.close();
	stat = cont.GetConnection().prepareStatement(sql.toString());
	stat.setString(1, uuid);
	// ���� ����
	rs = stat.executeQuery();
	// ���� ���� ���
	String get_email_uuid = null;
	String get_email_date = null;
	if (rs.next()) {
		// ��� ����
		get_email_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
		get_email_date = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
	}
	// ��� Ȯ��
	if (get_email_date == null ||
		get_email_uuid == null) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : �����ͺ��̽� �ᱣ���� Null�� ���ԵǸ� �� �˴ϴ�."));
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "�����ͺ��̽� �ᱣ���� Null�� ���ԵǸ� �� �˴ϴ�.");
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
	if (!auth.equals(get_email_uuid)) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : �ش� ������ ���� ������ �������� �ʽ��ϴ�."));
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "�ش� ������ ���� ������ �������� �ʽ��ϴ�.");
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
	// ��й�ȣ Ȯ��
	if (!rhyaSHA512.getSHA512(pw).equals(rhyaSHA512.getSHA512(pwc))) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : ��й�ȣ�� ��й�ȣ Ȯ�ο� �Էµ� ���� ��ġ���� �ʽ��ϴ�."));
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "��й�ȣ�� ��й�ȣ Ȯ�ο� �Էµ� ���� ��ġ���� �ʽ��ϴ�.");
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
	if (!(pw.length() > 7)) {
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "��й�ȣ�� �ּ� 8���� �̻� �Է��� �ּ���.");
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "ȸ������ ���� : ��й�ȣ�� �ּ� 8���� �̻� �Է��� �ּ���."));
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		captchaV = null;
		
		return;
	}
	// �ð� Ȯ��
	if (DateTimeChecker.isTime_H(get_email_date, 5)) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� : �̸��� ���� ����!"));
		// ���� ���� StringBuilder �ʱ�ȭ
		sql.delete(0,sql.length());
		// ���� ����
		sql.append("UPDATE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" SET ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
		sql.append(" = ?, ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
		sql.append(" = ? WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, null);
		stat.setString(2, null);
		stat.setString(3, uuid);
		// ���� ����
		stat.executeUpdate();
		// ���� ���� StringBuilder �ʱ�ȭ
		sql.delete(0,sql.length());
		// ���� ����
		sql.append("UPDATE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" SET ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		sql.append(" = ? WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		sql.append(" = ?;");
		// �����ͺ��̽� ����
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, rhyaSHA512.getSHA512(pw));
		stat.setString(2, uuid);
		// ���� ����
		stat.executeUpdate();
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� : ��й�ȣ ���� ����!"));
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_SUCCESS);
		obj.addProperty(forgotpwdV.MSG, "");
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
	}else {
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ���� : �ð� ����"));
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "�ش� ��û ���� �ð��� �������ϴ�.");
		// ��� ���
		out.println(gson.toJson(obj));
		// ���� ����
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
}catch (Exception ex) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// ��� ����
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "�� �� ���� ���� �߻�");
	// ��� ���
	out.println(gson.toJson(obj));
	// ���� ����
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	forgotpwdV = null;
	captchaV = null;
}
%>

