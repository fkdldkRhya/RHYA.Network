package kro.kr.rhya_network.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.data_buffer.DataBufferManager;
import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.util.JSPUtilsInitTask;

/**
 * Servlet implementation class DataBufferUserManager
 */
@WebServlet("/data_buffer_user_manager")
public class DataBufferUserManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataBufferUserManager() {
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
		final String successMessage = "success";
		final String failMessage = "fail";
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		
		try {
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Online_Attendance)) {
				// 명령어
				int command = Integer.parseInt(request.getParameter("mode"));
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
					 * Data-Buffer 관리자 : 0
					 * 
					 * 설명 :
					 * 		데이터 등록
					 * 
					 * 파라미터 :
					 * 		default  --> 기본 데이터 입력
					 */
					case 0: {
						String getParmInput = request.getParameter("default");
						getParmInput = URLDecoder.decode(getParmInput, "UTF-8");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String key = dataBufferManager.createBuffer(getParmInput);
						
						obj.addProperty(keyName_Result, successMessage);
						obj.addProperty(keyName_Message, key);
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer 데이터 생성 성공! / RequestCode: ", key));
						break;
					}
					
					
					/**
					 * Data-Buffer 관리자 : 1
					 * 
					 * 설명 :
					 * 		데이터 등록
					 * 
					 * 파라미터 :
					 * 		request  --> 요청 키
					 * 		input    --> 입력 데이터
					 * 		index    --> index
					 */
					case 1: {
						// 파라미터 추출
						String getParmRequestCode = request.getParameter("request");
						String getParmInput = request.getParameter("input");
						String getParmIndex = request.getParameter("index");
						getParmInput = URLDecoder.decode(getParmInput, "UTF-8");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String value = dataBufferManager.addBuffer(getParmRequestCode, Integer.parseInt(getParmIndex), getParmInput);
						
						if (value != null) {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty(keyName_Message, value);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "Data-Buffer 데이터 추가 성공! / RequestCode: ", getParmRequestCode, ", Input: ", getParmInput, ", Index: ", String.valueOf(getParmIndex)));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "Data-Buffer 데이터 추가 실패! [NotFoundRequestCode] / RequestCode: ", getParmRequestCode, ", Input: ", getParmInput, ", Index: ", String.valueOf(getParmIndex)));
						}
						
						break;
					}
					
					
					/**
					 * Data-Buffer 관리자 : 2
					 * 
					 * 설명 :
					 * 		데이터 불러오기
					 * 
					 * 파라미터 :
					 * 		request  --> 요청 키
					 */
					case 2: {
						String getParmRequestCode = request.getParameter("default");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String value = dataBufferManager.getBuffer(getParmRequestCode);
						
						if (value == null) {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty(keyName_Message, value);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer 데이터 출력 성공! / RequestCode: ", getParmRequestCode));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer 데이터 출력 실패! [NoFoundRequestCode] / RequestCode: ", getParmRequestCode));
						}
						
						break;
					}
				}
			}else {
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
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
