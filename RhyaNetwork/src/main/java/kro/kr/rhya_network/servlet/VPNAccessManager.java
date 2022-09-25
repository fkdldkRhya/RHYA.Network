package kro.kr.rhya_network.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.page.PageParameter;
import kro.kr.rhya_network.util.AuthTokenChecker;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.utils.db.DatabaseManager;
import kro.kr.rhya_network.utils.db.DatabaseManager.DatabaseConnection;

/**
 * Servlet implementation class VPNAccessManager
 */
@WebServlet("/vpn_access_manager")
public class VPNAccessManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// JSON ��ȯ ���
	private final String successMessage = "success";
	private final String failMessage = "fail";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VPNAccessManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
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
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_VPN_Access_Manager)) { // ������ �ʱ�ȭ ����
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
						 * VPN ���� ���� ��� : 0
						 * 
						 * ���� :
						 * 		OpenVPN ���� �ҷ�����
						 * 
						 * �Ķ���� :
						 * 		authToken   --> Auth Token
						 */
						case 0: {
							// Auth token Ȯ��
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(2)) &&
									isAccessUserForVPNService(result[1])) { // �α��� ����
								// ������ ���� ���� Ȯ��
								databaseConnection.setPreparedStatement("SELECT * FROM vpn_service_info;");
								databaseConnection.setResultSet();
								if (databaseConnection.getResultSet().next()) {
									if (databaseConnection.getResultSet().getInt("access") != 0) {
										// JSON ������ ����
										obj.addProperty(keyName_Result, failMessage);
										obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN ���� ��� ����! [���� �ź�]", "UTF-8"));
									}else {
										// JSON ������ ����
										obj.addProperty(keyName_Result, successMessage);
										byte[] encodedBytes = Base64.encodeBase64(databaseConnection.getResultSet().getString("open_vpn_config").getBytes());
										obj.addProperty("open_vpn_config", URLEncoder.encode(new String(encodedBytes), "UTF-8"));
										obj.addProperty("account_id", URLEncoder.encode(databaseConnection.getResultSet().getString("account_id"), "UTF-8"));
										obj.addProperty("account_pw", URLEncoder.encode(databaseConnection.getResultSet().getString("account_pw"), "UTF-8"));
										
										// �α� ���
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OpenVPN ���� ��� ����! Auth Token:", authToken));
									}
								}else {
									// JSON ������ ����
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN ���� ��� ����! [�α��� ����]", "UTF-8"));
								}
							}else { // �α��� ����
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN ���� ��� ����! [�α��� ����]", "UTF-8"));
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OpenVPN ���� ��� ����! [�α��� ����] Auth Token:", authToken));
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
			
			obj.addProperty(keyName_Result, e.getMessage());
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�� �� ���� ���� �߻�!", e.toString()));
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
		response.getWriter().append("Served at: ").append(request.getContextPath());
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
	 * OpenVPN ���� ���� �Ͽ� ���� Ȯ��
	 * @param user_uuid ����� UUID
	 * @return ���� ���� ����
	 */
	private boolean isAccessUserForVPNService(String user_uuid) {
		try {
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseConnection();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * from vpn_service_user where user_uuid = ?;");
			databaseConnection.getPreparedStatement().setString(1, user_uuid);
			databaseConnection.setResultSet();
			
			boolean isUse = true;
			
			if (databaseConnection.getResultSet().next()) 
				isUse = databaseConnection.getResultSet().getInt("user_access") == 1 ? true : false;
			else 
				isUse = false;
			
			databaseConnection.allClose();
			
			return isUse;
		}catch (Exception ex) {
			ex.printStackTrace();
			
			return false;
		}
	}
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
}
