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
		final String failMessage = "fail";
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		
		try {
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Other_Service_Downloader)) {
				// 명령어
				int command = Integer.parseInt(request.getParameter("package"));
				// 명령어 구분
				switch (command) {
					default: {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 명령입니다. mode 파라미터를 확인해 주세요.", "UTF-8"));
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "알 수 명령입니다. mode 파라미터를 확인해 주세요."));
						break;
					}
					
					/**
					 * 서울북부지원교육청 알리미 APK
					 */
					case 0: {
						// 파일 업로드된 경로
					    final String root = PathManager.BBEDU_ALERT_APK_PATH;
					    // 실제 내보낼 파일명
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
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "서울북부지원교육청 알리미 APK 파일 다운로드 성공!"));
							} else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서울북부지원교육청 알리미 APK 파일 다운로드 실패! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "서울북부지원교육청 알리미 APK 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
							e.printStackTrace();
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON 데이터 출력
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					/**
					 * 서울북부지원교육청 알리미 MSI
					 */
					case 1: {
						// 파일 업로드된 경로
					    final String root = PathManager.BBEDU_ALERT_MSI_PATH;
					    // 실제 내보낼 파일명
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
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "서울북부지원교육청 알리미 MSI 파일 다운로드 성공!"));
							} else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서울북부지원교육청 알리미 MSI 파일 다운로드 실패! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "서울북부지원교육청 알리미 MSI 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
							e.printStackTrace();
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON 데이터 출력
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					/**
					 * RHYA OpenVPN Client ZIP
					 */
					case 2: {
						// 파일 업로드된 경로
					    final String root = PathManager.RHYA_OPEN_VPN_CLIENT_ZIP_PATH;
					    // 실제 내보낼 파일명
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
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "RHYA OpenVPN Client ZIP 파일 다운로드 성공!"));
							} else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "RHYA OpenVPN Client ZIP 파일 다운로드 실패! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "RHYA OpenVPN Client ZIP 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON 데이터 출력
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
					
					
					
					/**
					 * WanaCrypt0r EXE
					 */
					case 3: {
						// 파일 업로드된 경로
					    final String root = PathManager.WANACRY_ROOT_PATH;
					    // 실제 내보낼 파일명
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
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r EXE 파일 다운로드 성공!"));
							} else {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "WanaCrypt0r EXE 파일 다운로드 실패! File does not exist!"));
								
								obj.addProperty(keyName_Result, failMessage);
							}
						} catch (IOException e) {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r EXE 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
							
							obj.addProperty(keyName_Result, failMessage);
						}
						
						
						// JSON 데이터 출력
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						break;
					}
				}
			}
		}catch (Exception ex) {
			// TODO: handle exception
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("알 수 없는 오류가 발생 하였습니다. ".concat(ex.getMessage()), "UTF-8"));
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
