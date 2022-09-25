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

//출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();

//Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
//Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

//클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

//쿠키 데이터
final String successResult = "<SUCCESS>";
final String failResult = "<Null>";

// 아이피 차단 확인
// ------------------------------------------------
if (!IPBlockChecker.isIPBlock(clientIP)) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP 차단 목록에 있는 호스트가 접속을 시도하였습니다. 해당 호스트의 접속을 시스템이 거부했습니다."));
	// 페이지 이동
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
	return;
}
// ------------------------------------------------

//예외 처리
try {
	//파라미터 입력
	String auth_token_uuid = request.getParameter(authParm.TOKEN);
	String auth_token_name = request.getParameter(authParm.NAME);
	// 데이터 확인
	if (!authParm.SERVICE.contains(auth_token_name)) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : 지원하는 서비스가 아님"));
		
		// 결과 설정
		obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
		out.println(gson.toJson(obj));
	}else {
		// 쿼리 작성 StringBuilder
		StringBuilder sql = new StringBuilder();
		
		// 데이터베이스 커넥터 변수 선언
		DatabaseConnection cont = new DatabaseConnection();
		// 데이터베이스 쿼리 실행 변수 선언
		PreparedStatement stat = null;
		ResultSet rs = null;
		
		// 데이터베이스 접속 예외 처리
		try {
			// 데이터베이스 접속
			cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
							DatabaseInfo.DATABASE_CONNECTION_URL,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		}catch (SQLException ex1) {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : DB 접근 오류!"));
			
			// 데이터베이스 접속 오류 처리
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
			// 연결 종료
			cont.Close();
			cont = null;
			sql = null;
			// 결과 설정
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}catch (ClassNotFoundException ex2) {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : DB 접근 오류!"));
			
			// 데이터베이스 접속 오류 처리
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
			// 연결 종료
			cont.Close();
			cont = null;
			sql = null;
			// 결과 설정
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}
		//페이지 상태 확인
		if (cont != null) {
			// 쿼리 생성
			sql.append("SELECT * FROM ");
			sql.append(DatabaseInfo.DATABASE_TABLE_NAME_JSP_PAGE_SETTING);
			sql.append(" WHERE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_ID);
			sql.append("=");
			sql.append("?;");
			
			// 쿼리 설정
			stat = cont.GetConnection().prepareStatement(sql.toString());
			stat.setInt(1, JspPageInfo.PageID_User_Account_Auth_Get_Info_Token);
			// 쿼리 생성 StringBuilder 초기화
			sql.delete(0,sql.length());
			// 쿼리 실행
			rs = stat.executeQuery();
			// 쿼리 실행 결과
			int state = 0;
			if (rs.next()) {
				state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
			}
			// 상태 확인 - 결과 처리
			if (!JspPageInfo.JspPageStateManager(state)) {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Page 접근 거부"));
				
				// 결과 설정
				obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
				out.println(gson.toJson(obj));
			    
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
			}else {
				// Null 확인
				if (auth_token_uuid != null && auth_token_name != null) {
					// 쿼리 생성
					sql.append("SELECT * FROM ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_AUTH_TOKEN);
					sql.append(" WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_TOKEN);
					sql.append("=");
					sql.append("? AND ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_AUTH_NAME);
					sql.append("=");
					sql.append("?;");
					// 쿼리 설정
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setString(1, auth_token_uuid);
					stat.setString(2, auth_token_name);
					// 쿼리 생성 StringBuilder 초기화
					sql.delete(0,sql.length());
					// 쿼리 실행
					rs = stat.executeQuery();
					// 결과 비교
					if (rs.next()) {
						// 토큰 확인
						AuthTokenChecker authTokenChecker = new AuthTokenChecker();
						String[] result = authTokenChecker.getMoreAuthInfo(auth_token_uuid);
						if (result[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Auth Token Info Success : Token is enabled /", auth_token_uuid));
							
							// 결과 설정
							obj.addProperty(signupV.RESULT, result[0]);
							obj.addProperty("uuid", URLEncoder.encode(result[1], "UTF-8"));
							obj.addProperty("autotoken", URLEncoder.encode(result[2], "UTF-8"));
							obj.addProperty(signupV.ID, URLEncoder.encode(result[3], "UTF-8"));
							obj.addProperty(signupV.NAME, URLEncoder.encode(result[4], "UTF-8"));
							obj.addProperty(signupV.EMAIL, URLEncoder.encode(result[5], "UTF-8"));
							obj.addProperty(signupV.BIRTHDAY, URLEncoder.encode(result[6], "UTF-8"));
							obj.addProperty("regdate", URLEncoder.encode(result[7], "UTF-8"));
							out.println(gson.toJson(obj));
							
							// 연결 종료
							rs.close();
							stat.close();
							cont.Close();
							rl = null;
							sql = null;	
						}else {
							// 로그 출력
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Not defined token"));
							
							// 결과 설정
							obj.addProperty(signupV.RESULT, result[0]);
							out.println(gson.toJson(obj));
							
							// 연결 종료
							rs.close();
							stat.close();
							cont.Close();
							rl = null;
							sql = null;
						}
					}else {
						// 로그 출력
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : Not defined token"));
						
						// 결과 설정
						obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
						out.println(gson.toJson(obj));
						
						// 연결 종료
						rs.close();
						stat.close();
						cont.Close();
						rl = null;
						sql = null;
					}
				}else {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Auth Token Info Error : 파라미터 == Null"));
					
					// 결과 설정
					obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
					out.println(gson.toJson(obj));
					
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
				}
			}
		}else {
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Auth Token Check Error : DB 접근 오류!"));
			
			// 결과 설정
			obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
			out.println(gson.toJson(obj));
		}
	}
}catch (Exception ex) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv8(clientIP, "Auth Token Check Error :", ex.toString()));
	
	// 결과 설정
	obj.addProperty(signupV.RESULT, AuthTokenChecker.AUTH_RESULT_FAIL);
	out.println(gson.toJson(obj));
}
%>