<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="kro.kr.rhya_network.security.RhyaSHA512"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.SelfXSSFilter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
//변수 클레스 선언
PageParameter.SignUp signupV = new PageParameter.SignUp();
reCaptChaInfo captchaV = new reCaptChaInfo();
RhyaSHA512 rhyaSHA512 = new RhyaSHA512();

// URL 직접 접근 확인
String strReferer = request.getHeader("referer");
if(strReferer == null) {
	// 포워드
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
	return;
}

//Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
//Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

//쿼리 작성 StringBuilder
StringBuilder sql = new StringBuilder();

//클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

//데이터베이스 커넥터 변수 선언
DatabaseConnection cont = new DatabaseConnection();
//데이터베이스 쿼리 실행 변수 선언
PreparedStatement stat = null;
ResultSet rs = null;


//------------------------------------------------
if (!IPBlockChecker.isIPBlock(clientIP)) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP 차단 목록에 있는 호스트가 접속을 시도하였습니다. 해당 호스트의 접속을 시스템이 거부했습니다."));
	// 페이지 이동
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	rd.forward(request,response);
	return;
}
//------------------------------------------------


//데이터베이스 접속 예외 처리
try {
	// 데이터베이스 접속
	cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
					DatabaseInfo.DATABASE_CONNECTION_URL,
					DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
					DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
}catch (SQLException ex1) {
	// 데이터베이스 접속 오류 처리
	cont.Close();
	// 로그 작성
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
	// 페이지 이동
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
}catch (ClassNotFoundException ex2) {
	// 데이터베이스 접속 오류 처리
	cont.Close();
	// 로그 작성
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
	// 페이지 이동
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
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
	stat.setInt(1, JspPageInfo.PageID_Rhya_Network_Logout_All_Edit);
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
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		signupV = null;
		captchaV = null;
		
		// 페이지 이동
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
		
		return;
	}
}

//출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();

//파라미터 가져오기
String key = request.getParameter(signupV.INT_KEY);
String token = request.getParameter(signupV.RE_CHAPT_CHA);

//Null 확인
if (key == null ||
	token == null) {
	
	// 결과 설정
	obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
	obj.addProperty(signupV.MSG, "입력값은 Null이 포함되면 안 됩니다.");
	// 로그 출력
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "로그아웃(ALL) 요청 실패 : 입력값은 Null이 포함되면 안 됩니다."));
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	signupV = null;
	captchaV = null;
	
	return;
}

//예외 처리
try {
	// 세션 데이터 가지고 오기
	String int_random_key_org = (String) session.getAttribute(ParameterManipulation.INTRandomKeySession);
	// 세션 데이터 복호화
	int_random_key_org = RhyaAES.AES_Decode(int_random_key_org);
	// 파라미터 복호화
	key = RhyaAES.AES_Decode(key);
	// 로그 출력
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { signupV.INT_KEY, signupV.RE_CHAPT_CHA },
															  new String[] { key, token }));
	// 정수형 인증키 비교
	if (!int_random_key_org.equals(key)) {
		// 결과 설정
		obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
		obj.addProperty(signupV.MSG, "인증키가 일치하지 않습니다.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "로그아웃(ALL) 요청 실패 : 인증키가 일치하지 않습니다."));
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		signupV = null;
		captchaV = null;
		
		return;
	}

	// 자동 로그인 확인
	String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
	if (login_session != null) {
		// 자동 로그인
		String[] result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
		if (result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
			// 쿼리 생성
			sql.append("UPDATE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
			sql.append(" SET ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
			sql.append("= ? WHERE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
			sql.append(" = ?;");
			// 데이터베이스 접속
			stat.close();
			stat = cont.GetConnection().prepareStatement(sql.toString());
			stat.setString(1, null);
			stat.setString(2, result[1]);
			// 쿼리 실행
			stat.executeUpdate();
			// 연결 종료
			stat.close();
			
			sql.setLength(0);

			// 데이터베이스 접속
			stat.close();
			stat = cont.GetConnection().prepareStatement("DELETE FROM auth_token WHERE user_uuid = ?");
			stat.setString(1, result[1]);
			// 쿼리 실행
			stat.executeUpdate();
			
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "로그아웃(ALL) 요청 성공!"));
			// 결과 설정
			obj.addProperty(signupV.RESULT, signupV.RST_SUCCESS);

			// 결과 출력
			out.println(gson.toJson(obj));
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			rl = null;
			sql = null;
			signupV = null;
			
			return;	
		}else {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인 사용자가 아닙니다"));
			// 결과 설정
			obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
			obj.addProperty(signupV.MSG, "로그인 사용자가 아닙니다");
			// 결과 출력
			out.println(gson.toJson(obj));
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			rl = null;
			sql = null;
			signupV = null;
			captchaV = null;	
		}
	}else {
		if (request.getParameter("auth") != null) {
			AuthTokenChecker authTokenChecker = new AuthTokenChecker();
			String[] result = authTokenChecker.getMoreAuthInfo(request.getParameter("auth"));
			
			if (result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				// 쿼리 생성
				sql.append("UPDATE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
				sql.append(" SET ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID);
				sql.append("= ? WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
				sql.append(" = ?;");
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, null);
				stat.setString(2, result[1]);
				// 쿼리 실행
				stat.executeUpdate();
				// 연결 종료
				stat.close();
				
				sql.setLength(0);

				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement("DELETE FROM auth_token WHERE user_uuid = ?");
				stat.setString(1, result[1]);
				// 쿼리 실행
				stat.executeUpdate();
				
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "로그아웃(ALL) 요청 성공!"));
				// 결과 설정
				obj.addProperty(signupV.RESULT, signupV.RST_SUCCESS);

				// 결과 출력
				out.println(gson.toJson(obj));
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				signupV = null;
				
				return;	
			}else {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인 사용자가 아닙니다"));
				// 결과 설정
				obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
				obj.addProperty(signupV.MSG, "로그인 사용자가 아닙니다");
				// 결과 출력
				out.println(gson.toJson(obj));
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				signupV = null;
				captchaV = null;	
			}
		}else {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인 사용자가 아닙니다"));
			// 결과 설정
			obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
			obj.addProperty(signupV.MSG, "로그인 사용자가 아닙니다");
			// 결과 출력
			out.println(gson.toJson(obj));
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			rl = null;
			sql = null;
			signupV = null;
			captchaV = null;	
		}
	}
}catch (Exception ex) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// 결과 설정
	obj.addProperty(signupV.RESULT, signupV.RST_FAIL);
	obj.addProperty(signupV.MSG, "알 수 없는 오류 발생");
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	signupV = null;
	captchaV = null;
}
%>