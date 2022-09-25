package kro.kr.rhya_network.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.util.PathManager;

/**
 * Servlet implementation class OtherServiceDownload
 */
@WebServlet("/other_service_download")
public class OtherServiceDownload extends HttpServlet {
	private static final long serialVersionUID = 3L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OtherServiceDownload() {
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

		// Ŭ���̾�Ʈ ������
		String clientIP = GetClientIPAddress.getClientIp(request);
		
		// ��� ���
		Gson gson = new Gson();
		JsonObject obj = new JsonObject();
		final String failMessage = "fail";
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		
		try {
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Other_Service_Downloader)) {
				// ��ɾ�
				int command = Integer.parseInt(request.getParameter("package"));
				// ��ɾ� ����
				switch (command) {
					default: {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���.", "UTF-8"));
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "�� �� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���."));
						break;
					}
					
					/**
					 * ����Ϻ���������û �˸��� APK
					 */
					case 0: {
						// ���� ���ε�� ���
					    final String root = PathManager.BBEDU_ALERT_APK_PATH;
					    // ���� ������ ���ϸ�
					    String orgfilename = "bbedu_alert_apk.apk";    
						try {
							File file = new File(root);
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
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "����Ϻ���������û �˸��� APK ���� �ٿ�ε� ����!"));
							} else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "����Ϻ���������û �˸��� APK ���� �ٿ�ε� ����! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����Ϻ���������û �˸��� APK ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
							e.printStackTrace();
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON ������ ���
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					/**
					 * ����Ϻ���������û �˸��� MSI
					 */
					case 1: {
						// ���� ���ε�� ���
					    final String root = PathManager.BBEDU_ALERT_MSI_PATH;
					    // ���� ������ ���ϸ�
					    String orgfilename = "bbedu_alert_msi.msi";    
						try {
							File file = new File(root);
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
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "����Ϻ���������û �˸��� MSI ���� �ٿ�ε� ����!"));
							} else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "����Ϻ���������û �˸��� MSI ���� �ٿ�ε� ����! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "����Ϻ���������û �˸��� MSI ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
							e.printStackTrace();
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON ������ ���
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					/**
					 * RHYA OpenVPN Client ZIP
					 */
					case 2: {
						// ���� ���ε�� ���
					    final String root = PathManager.RHYA_OPEN_VPN_CLIENT_ZIP_PATH;
					    // ���� ������ ���ϸ�
					    String orgfilename = "rhya_open_vpn_client.zip";    
						try {
							File file = new File(root);
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
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA OpenVPN Client ZIP ���� �ٿ�ε� ����!"));
							} else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "RHYA OpenVPN Client ZIP ���� �ٿ�ε� ����! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "RHYA OpenVPN Client ZIP ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON ������ ���
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					
					/**
					 * WanaCrypt0r EXE
					 */
					case 3: {
						// ���� ���ε�� ���
					    final String root = PathManager.WANACRY_ROOT_PATH;
					    // ���� ������ ���ϸ�
					    String orgfilename = "WanaCrypt0r.exe";    
						try {
							File file = new File(root, orgfilename);
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
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r EXE ���� �ٿ�ε� ����!"));
							} else {
								// �α� ���
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "WanaCrypt0r EXE ���� �ٿ�ε� ����! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// �α� ���
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r EXE ���� �ٿ�ε� ����! �� �� ���� ������ �߻��Ͽ����ϴ�.", e.toString()));
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON ������ ���
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
				}
			}
		}catch (Exception ex) {
			// TODO: handle exception
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� ������ �߻� �Ͽ����ϴ�. ".concat(ex.getMessage()), "UTF-8"));
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
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
