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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.util.JSPUtilsInitTask;
import kro.kr.rhya_network.util.ServiceAccessChecker;
import kro.kr.rhya_network.utils.db.DatabaseManager;

/**
 * Servlet implementation class UtaitePlayerManagerV2
 */
@WebServlet("/utaite_player_manager_v2")
public class UtaitePlayerManagerV2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// JSON 반환 결과
	private final String successMessage = "success";
	private final String failMessage = "fail";
     
	
	
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UtaitePlayerManagerV2() {
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
						 * 우타이테 플레이어 관리 모드 : 26
						 * 
						 * 설명 :
						 * 		<NULL>
						 * 
						 * 파라미터 :
						 * 		auth        --> Auth Token
						 */
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

	
	
	
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	// ----------------------------------------------------------------------- //
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		service(request, response);
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
}
