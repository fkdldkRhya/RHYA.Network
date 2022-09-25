<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Random"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.util.CookieGenerator"%>
<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>

<%
PageParameter.AuthToken authParm = new PageParameter.AuthToken();
PageParameter.SignUp signupV = new PageParameter.SignUp();
CookieGenerator cookieGen = new CookieGenerator();

// Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
// Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

//��� ���
Gson gson = new Gson();
JsonObject obj = new JsonObject();

//��Ű ������
final String failResult = "<Null>";

// Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);


// ������ ���� Ȯ��
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


//���� ó��
try {
	//�Ķ���� �Է�
	String auto_login_uuid = request.getParameter(authParm.USER);
	String auto_login_token = request.getParameter(authParm.TOKEN);
	String auth_token_name = request.getParameter(authParm.NAME);

	// ������ Ȯ��
	if (!authParm.SERVICE.contains(auth_token_name)) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : �����ϴ� ���񽺰� �ƴ�"));
		
		// ��Ű ����
		cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
		// ��� ����
		obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
		out.println(gson.toJson(obj));
	}else {
		// ���� �ۼ� StringBuilder
		StringBuilder sql = new StringBuilder();
		
		// �����ͺ��̽� Ŀ���� ���� ����
		DatabaseConnection cont = new DatabaseConnection();
		// �����ͺ��̽� ���� ���� ���� ����
		PreparedStatement stat = null;
		ResultSet rs = null;
		
		// �����ͺ��̽� ���� ���� ó��
		try {
			// �����ͺ��̽� ����
			cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
							DatabaseInfo.DATABASE_CONNECTION_URL,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		}catch (SQLException ex1) {
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : DB ���� ����!"));
			
			// �����ͺ��̽� ���� ���� ó��
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
			// ���� ����
			cont.Close();
			cont = null;
			sql = null;
			
			// ��Ű ����
			cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
			// ��� ����
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}catch (ClassNotFoundException ex2) {
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : DB ���� ����!"));
			
			// �����ͺ��̽� ���� ���� ó��
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
			// ���� ����
			cont.Close();
			cont = null;
			sql = null;

			// ��Ű ����
			cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
			// ��� ����
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
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
			stat.setInt(1, JspPageInfo.PageID_Rhya_Network_Auth_Get_Token);
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
				// �α� ���
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : Page ���� �ź�"));
				
				// ��Ű ����
				cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
				// ��� ����
				obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
				out.println(gson.toJson(obj));
			    
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
			}else {
				// �ڵ� �α��� Ȯ��
				String[] login_result = LoginChecker.IsAutoLogin(auto_login_token, auto_login_uuid, response, true);
				// �ڵ� �α��� ������ ��
				if (login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
					// ���� ����
					sql.append("SELECT * FROM ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
					sql.append(" WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_USER);
					sql.append("=");
					sql.append("? AND ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_NAME);
					sql.append("=");
					sql.append("?;");
					// ���� ����
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, auto_login_uuid);
					stat.setString(2, auth_token_name);
					// ���� ���� StringBuilder �ʱ�ȭ
					sql.delete(0,sql.length());
					// ���� ����
					rs = stat.executeQuery();
					// ���� ���
					if (rs.next()) {
						// ��߱�
						// --------------------------------------------------------------
						// ��Ű ����
						String auth_token_new = rs.getString("auth_token");
						cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, auth_token_new, "/", authParm.RESULT_COOKIE, 60);
						// ��� ����
						obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_SUCCESS);
						obj.addProperty(signupV.MSG, auth_token_new);
						out.println(gson.toJson(obj));
						
						
						// �α� ���
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Auth Token Gen : �߱� ����! /", auth_token_new));
					    
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						rl = null;
						sql = null;
						// --------------------------------------------------------------
					}else {
						// �ű� �߱�
						// --------------------------------------------------------------
						// ������
						UUID uuid_ = UUID.randomUUID();
						final String auth_token_new = uuid_.toString();
						final String now_date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
						// ���� ����
						sql.append("INSERT INTO ");
						sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
						sql.append(" VALUES (?,?,?,?);");
						// ���� ����
						stat.close();
						stat = cont.GetConnection().prepareStatement(sql.toString());
						stat.setString(1, auth_token_new);
						stat.setString(2, auto_login_uuid);
						stat.setString(3, auth_token_name);
						stat.setString(4, now_date);
						// ���� ����
						stat.executeUpdate();
						// ��Ű ����
						cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, auth_token_new, "/", authParm.RESULT_COOKIE, 60);
						// ��� ����
						obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_SUCCESS);
						obj.addProperty(signupV.MSG, auth_token_new);
						out.println(gson.toJson(obj));
						
						// �α� ���
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Auth Token Gen : �߱� ����! /", auth_token_new));
					    
						// ���� ����
						rs.close();
						stat.close();
						cont.Close();
						rl = null;
						sql = null;
						// --------------------------------------------------------------
					}
				}else {
					// �α� ���
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : �α��� ����!"));
					
					// ��Ű ����
					cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
					// ��Ű ����
					cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
					// ��� ����
					obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
					out.println(gson.toJson(obj));
					
					// ���� ����
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
				}
			}
		}else {
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Gen Error : DB ���� ����!"));
			
			// ��Ű ����
			cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
			// ��Ű ����
			cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
			// ��� ����
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}
	}
}catch (Exception ex) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "Auth Token Gen Error :", ex.toString()));
	
	// ��Ű ����
	cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
	// ��Ű ����
	cookieGen.createCookie(response, rl, clientIP, authParm.RESULT_COOKIE, failResult, "/", authParm.RESULT_COOKIE, 60);
	// ��� ����
	obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
	out.println(gson.toJson(obj));
}
%>