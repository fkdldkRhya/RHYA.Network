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
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.email.EmailSendDATA"%>
<%@ page import="kro.kr.rhya_network.email.SendEmail"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
String redPage = request.getParameter(PageParameter.REDIRECT_PAGE_ID_PARM);
String ctoken = request.getParameter(PageParameter.IS_CREATE_TOKEN_PARM);
int loginSuccessPage = JspPageInfo.PageID_Rhya_Network_Main;
int isCreateToken = 0;
boolean isCreateTokenTOF = true;
if (redPage != null) {
	loginSuccessPage = Integer.parseInt(redPage);
}
if (ctoken != null) {
	isCreateToken = Integer.parseInt(ctoken);
	if (isCreateToken != 0) {
		isCreateTokenTOF = true;
	}else {
		isCreateTokenTOF = false;
	}
}
%>

<%
//���� Ŭ���� ����
PageParameter.ForgotPWD forgotpwdV = new PageParameter.ForgotPWD();
reCaptChaInfo captchaV = new reCaptChaInfo();
EmailSendDATA.ForgotPassword emailSendData = new EmailSendDATA.ForgotPassword();
SendEmail sendEmail = new SendEmail();

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
	sql = null;
	forgotpwdV = null;
	captchaV = null;
	// �α� �ۼ�
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
	// ������ �̵�
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
}catch (ClassNotFoundException ex2) {
	// �����ͺ��̽� ���� ���� ó��
	cont.Close();
	sql = null;
	forgotpwdV = null;
	captchaV = null;
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
	stat.setInt(1, JspPageInfo.PageID_User_Account_ForgotPW_Task);
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
String name = URLDecoder.decode(request.getParameter(forgotpwdV.NAME), "UTF-8");
String id = URLDecoder.decode(request.getParameter(forgotpwdV.ID), "UTF-8");
String email = URLDecoder.decode(request.getParameter(forgotpwdV.EMAIL), "UTF-8");
String key = request.getParameter(forgotpwdV.INT_KEY);
String token = request.getParameter(forgotpwdV.RE_CHAPT_CHA);

//Null Ȯ��
if (name == null ||
    id == null ||
	email == null ||
	key == null ||
	token == null) {
	
	// ��� ����
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "�Է°��� Null�� ���ԵǸ� �� �˴ϴ�.");
	// �α� ���
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ��û ���� : �Է°��� Null�� ���ԵǸ� �� �˴ϴ�."));
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
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { forgotpwdV.NAME, forgotpwdV.ID, forgotpwdV.EMAIL, forgotpwdV.INT_KEY, forgotpwdV.RE_CHAPT_CHA },
															  new String[] { name, id, email, key, token }));
	// XSS ���͸�
	name = SelfXSSFilter.TextXSSFilter(name);
	id = SelfXSSFilter.TextXSSFilter(id);
	key = SelfXSSFilter.TextXSSFilter(key);
	// ������ ����Ű ��
	if (!int_random_key_org.equals(key)) {
		// ��� ����
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "����Ű�� ��ġ���� �ʽ��ϴ�.");
		// �α� ���
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ��û ���� : ����Ű�� ��ġ���� �ʽ��ϴ�."));
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
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
	sql.append(" FROM ");
	sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
	sql.append(" WHERE ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
	sql.append(" =?;");
	// �����ͺ��̽� ����
	stat.close();
	stat = cont.GetConnection().prepareStatement(sql.toString());
	stat.setString(1, id);
	// ���� ����
	rs = stat.executeQuery();
	// ���� ���
	boolean result = rs.next();
	// ��� ó��
	if (result) {
		// ���� ���̵�
	 	final String user_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		
		// ���� ���� Ȯ��
		// ===================================================================================
		// ���� ���� StringBuilder �ʱ�ȭ
		sql.delete(0,sql.length());
		// ���� ����
		sql.append("SELECT * ");
		sql.append(" FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" =?;");
		// �����ͺ��̽� ����
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, user_uuid);
		// ���� ����
		rs = stat.executeQuery();
		rs.next();
		// ������ ��
		if (!(rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_NAME).equals(name) &&
			rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_EMAIL).equals(email))) {
			// �α� ���
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ��û : �ش� ������ ���� ������ �������� �ʽ��ϴ�."));
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
			forgotpwdV = null;
			captchaV = null;
			return;
		}
		// ===================================================================================
		
		// ������ ���� ����
		java.util.UUID uuid_ = java.util.UUID.randomUUID();
		final String uuid = uuid_.toString();
		final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
		// ���� ������
		final String email_url = emailSendData.Url(request, user_uuid.toString(), uuid.toString(), loginSuccessPage, isCreateToken);
		// ���� ����
		sendEmail.Send(sendEmail.GetProperties(), emailSendData.Html(id, email_url), emailSendData.Title(id), email);
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
		stat.setString(1, uuid);
		stat.setString(2, date);
		stat.setString(3, user_uuid);
		// ���� ����
		stat.executeUpdate();
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ �缳�� ��û ����!"));
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
		forgotpwdV = null;
		
		return;	
	}else {
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "��й�ȣ ���� ��û : �ش� ������ ���� ������ �������� �ʽ��ϴ�."));
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
		forgotpwdV = null;
		captchaV = null;
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