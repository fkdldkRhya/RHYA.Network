<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.net.URLEncoder"%>
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
<%@ page import="kro.kr.rhya_network.util.CookieGenerator"%>
<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter.AuthToken"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>

<%
PageParameter.AuthToken authParm = new PageParameter.AuthToken();
PageParameter.SignUp signupV = new PageParameter.SignUp();
CookieGenerator cookieGen = new CookieGenerator();

//��� ���
Gson gson = new Gson();
JsonObject obj = new JsonObject();

//Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
//Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

//Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);

//��Ű ������
final String successResult = "<SUCCESS>";
final String failResult = "<Null>";

// ������ ���� Ȯ��
// ------------------------------------------------
if (!IPBlockChecker.isIPBlock(clientIP)) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP ���� ��Ͽ� �ִ� ȣ��Ʈ�� ������ �õ��Ͽ����ϴ�. �ش� ȣ��Ʈ�� ������ �ý����� �ź��߽��ϴ�."));
	// ������ �̵�
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
	return;
}
// ------------------------------------------------

//���� ó��
try {
	//�Ķ���� �Է�
	String auth_token_uuid = request.getParameter(authParm.TOKEN);
	String auth_token_name = request.getParameter(authParm.NAME);
	// ������ Ȯ��
	if (!authParm.SERVICE.contains(auth_token_name)) {
		// �α� ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : �����ϴ� ���񽺰� �ƴ�"));
		
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
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : DB ���� ����!"));
			
			// �����ͺ��̽� ���� ���� ó��
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
			// ���� ����
			cont.Close();
			cont = null;
			sql = null;
			// ��� ����
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}catch (ClassNotFoundException ex2) {
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : DB ���� ����!"));
			
			// �����ͺ��̽� ���� ���� ó��
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
			// ���� ����
			cont.Close();
			cont = null;
			sql = null;
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
			stat.setInt(1, JspPageInfo.PageID_User_Account_Auth_Get_Info_Token);
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
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Page ���� �ź�"));
				
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
				// Null Ȯ��
				if (auth_token_uuid != null && auth_token_name != null) {
					// ���� ����
					sql.append("SELECT * FROM ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
					sql.append(" WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
					sql.append("=");
					sql.append("? AND ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_NAME);
					sql.append("=");
					sql.append("?;");
					// ���� ����
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, auth_token_uuid);
					stat.setString(2, auth_token_name);
					// ���� ���� StringBuilder �ʱ�ȭ
					sql.delete(0,sql.length());
					// ���� ����
					rs = stat.executeQuery();
					// ��� ��
					if (rs.next()) {
						// ��ū Ȯ��
						AuthTokenChecker authTokenChecker = new AuthTokenChecker();
						String[] result = authTokenChecker.getMoreAuthInfo(auth_token_uuid);
						if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Auth Token Info Success : Token is enabled /", auth_token_uuid));
							
							// ��� ����
							obj.addProperty(signupV.RESULT, result[0]);
							obj.addProperty("uuid", URLEncoder.encode(result[1], "UTF-8"));
							obj.addProperty("autotoken", URLEncoder.encode(result[2], "UTF-8"));
							obj.addProperty(signupV.ID, URLEncoder.encode(result[3], "UTF-8"));
							obj.addProperty(signupV.NAME, URLEncoder.encode(result[4], "UTF-8"));
							obj.addProperty(signupV.EMAIL, URLEncoder.encode(result[5], "UTF-8"));
							obj.addProperty(signupV.BIRTHDAY, URLEncoder.encode(result[6], "UTF-8"));
							obj.addProperty("regdate", URLEncoder.encode(result[7], "UTF-8"));
							out.println(gson.toJson(obj));
							
							// ���� ����
							rs.close();
							stat.close();
							cont.Close();
							rl = null;
							sql = null;	
						}else {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Not defined token"));
							
							// ��� ����
							obj.addProperty(signupV.RESULT, result[0]);
							out.println(gson.toJson(obj));
							
							// ���� ����
							rs.close();
							stat.close();
							cont.Close();
							rl = null;
							sql = null;
						}
					}else {
						// �α� ���
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Not defined token"));
						
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
				}else {
					// �α� ���
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : �Ķ���� == Null"));
					
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
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Check Error : DB ���� ����!"));
			
			// ��� ����
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}
	}
}catch (Exception ex) {
	// �α� ���
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "Auth Token Check Error :", ex.toString()));
	
	// ��� ����
	obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
	out.println(gson.toJson(obj));
}
%>