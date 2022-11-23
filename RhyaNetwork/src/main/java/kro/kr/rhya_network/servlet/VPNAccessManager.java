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
	// JSON 반환 결과
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
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_VPN_Access_Manager)) { // 페이지 초기화 성공
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
						 * VPN 서비스 관리 모드 : 0
						 * 
						 * 설명 :
						 * 		OpenVPN 정보 불러오기
						 * 
						 * 파라미터 :
						 * 		authToken   --> Auth Token
						 */
						case 0: {
							// Auth token 확인
							String authToken = request.getParameter("auth");
							PageParameter.AuthToken authTokenParm = new PageParameter.AuthToken();
							AuthTokenChecker authTokenChecker = new AuthTokenChecker();
							String[] result = authTokenChecker.getAuthInfo(authToken);
							if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS) &&
									result[2].equals(authTokenParm.SERVICE.get(2)) &&
									isAccessUserForVPNService(result[1])) { // 로그인 성공
								// 데이터 존재 여부 확인
								databaseConnection.setPreparedStatement("SELECT * FROM vpn_service_info;");
								databaseConnection.setResultSet();
								if (databaseConnection.getResultSet().next()) {
									if (databaseConnection.getResultSet().getInt("access") != 0) {
										// JSON 데이터 설정
										obj.addProperty(keyName_Result, failMessage);
										obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN 정보 출력 실패! [접근 거부]", "UTF-8"));
									}else {
										// JSON 데이터 설정
										obj.addProperty(keyName_Result, successMessage);
										byte[] encodedBytes = Base64.encodeBase64(databaseConnection.getResultSet().getString("open_vpn_config").getBytes());
										obj.addProperty("open_vpn_config", URLEncoder.encode(new String(encodedBytes), "UTF-8"));
										obj.addProperty("account_id", URLEncoder.encode(databaseConnection.getResultSet().getString("account_id"), "UTF-8"));
										obj.addProperty("account_pw", URLEncoder.encode(databaseConnection.getResultSet().getString("account_pw"), "UTF-8"));
										
										// 로그 출력
										rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OpenVPN 정보 출력 성공! Auth Token:", authToken));
									}
								}else {
									// JSON 데이터 설정
									obj.addProperty(keyName_Result, failMessage);
									obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN 정보 출력 실패! [로그인 실패]", "UTF-8"));
								}
							}else { // 로그인 실패
								// JSON 데이터 설정
								obj.addProperty(keyName_Result, failMessage);
								obj.addProperty(keyName_Message, URLEncoder.encode("OpenVPN 정보 출력 실패! [로그인 실패]", "UTF-8"));
								
								// 로그 출력
								rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "OpenVPN 정보 출력 실패! [로그인 실패] Auth Token:", authToken));
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
			
			obj.addProperty(keyName_Result, e.getMessage());
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "알 수 없는 오류 발생!", e.toString()));
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
	 * OpenVPN 서비스 접근 하용 계정 확인
	 * @param user_uuid 사용자 UUID
	 * @return 접근 가능 여부
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
