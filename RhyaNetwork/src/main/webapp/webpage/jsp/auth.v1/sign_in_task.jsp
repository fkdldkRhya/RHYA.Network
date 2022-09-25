<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.SelfXSSFilter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaSHA512"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPAccessChecker"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>

<%
String ctoken = request.getParameter(PageParameter.IS_CREATE_TOKEN_PARM);
int isCreateToken = 0;
boolean isCreateTokenTOF = true;
if (ctoken != null) {
	isCreateToken = Integer.parseInt(ctoken);
	if (isCreateToken != 0) {
		isCreateTokenTOF = true;
	}else {
		isCreateTokenTOF = false;
	}
}
%>

<%
// 변수 클레스 선언
PageParameter.SignIn signinV = new PageParameter.SignIn();
reCaptChaInfo captchaV = new reCaptChaInfo();
RhyaSHA512 rhyaSHA512 = new RhyaSHA512();

// Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
// Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// 쿼리 작성 StringBuilder
StringBuilder sql = new StringBuilder();

// 클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

// 데이터베이스 커넥터 변수 선언
DatabaseConnection cont = new DatabaseConnection();
// 데이터베이스 쿼리 실행 변수 선언
PreparedStatement stat = null;
ResultSet rs = null;

//URL 직접 접근 확인
String strReferer = request.getHeader("referer");
if(strReferer == null) {
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	rd.forward(request,response);
	return;
}


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


// 데이터베이스 접속 예외 처리
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

// 페이지 상태 확인
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
	stat.setInt(1, JspPageInfo.PageID_User_Account_Sign_In_Task);
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
		signinV = null;
		captchaV = null;
		
		// 페이지 이동
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
		return;
	}
}

// 출력 결과
Gson gson = new Gson();
JsonObject obj = new JsonObject();

// 파라미터 가져오기
String id = URLDecoder.decode(request.getParameter(signinV.ID), "UTF-8");
String pw = URLDecoder.decode(request.getParameter(signinV.PASSWORD), "UTF-8");
String remember_me = URLDecoder.decode(request.getParameter(signinV.REMEMBER_ME), "UTF-8");
String key = request.getParameter(signinV.INT_KEY);
String token = request.getParameter(signinV.RE_CHAPT_CHA);

// Null 확인
if (id == null ||
	key == null ||
	token == null) {
	
	// 결과 설정
	obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
	obj.addProperty(signinV.MSG, "입력값은 Null이 포함되면 안 됩니다.");
	// 로그 출력
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "로그인 실패 : 입력값은 Null이 포함되면 안 됩니다."));
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	signinV = null;
	captchaV = null;
	
	return;
}

// 예외 처리
try {
	// 세션 데이터 가지고 오기
	String int_random_key_org = (String) session.getAttribute(ParameterManipulation.INTRandomKeySession);
	// 세션 데이터 복호화
	int_random_key_org = RhyaAES.AES_Decode(int_random_key_org);
	// 파라미터 복호화
	key = RhyaAES.AES_Decode(key);
	// 로그 출력
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { signinV.ID, signinV.PASSWORD, signinV.INT_KEY, signinV.RE_CHAPT_CHA },
															  new String[] { id, pw, key, token }));
	// XSS 필터링
	id = SelfXSSFilter.TextXSSFilter(id);
	key = SelfXSSFilter.TextXSSFilter(key);
	// 정수형 인증키 비교
	if (!int_random_key_org.equals(key)) {
		// 결과 설정
		obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
		obj.addProperty(signinV.MSG, "인증키가 일치하지 않습니다.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "로그인 실패 : 인증키가 일치하지 않습니다."));
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		signinV = null;
		captchaV = null;
		
		return;
	}
	// reCaptCha 검사
	if (!captchaV.reCaptChaChecker(token)) {
		// 결과 설정
		obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
		obj.addProperty(signinV.MSG, "Google reCAPTCHA v3를 통과하지 못했습니다.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "Google reCAPTCHA v3를 통과하지 못했습니다."));
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		captchaV = null;
		
		return;
	}
	// 공백 제거
	id = id.replaceAll(" ", "");
	pw = pw.replaceAll(" ", "");
	// 변형
	pw = rhyaSHA512.getSHA512(pw);
	// 로그인
	String[] result = LoginChecker.IsLoginUser(id, pw, response, isCreateTokenTOF);
	// 결과 비교
	if (result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
		// 데이터 암호화
		String enc_value_1 = RhyaAES.AES_Encode(result[1]);
		String enc_value_2 = RhyaAES.AES_Encode(result[2]);
		
		// 허용 아이피 확인
		IPAccessChecker ipAccessChecker = new IPAccessChecker();
		String ipResult = ipAccessChecker.isAccessIP(request, response, id, pw, result[1], clientIP);
		if (ipResult.equals(ipAccessChecker.DB_RESULT_SUCCESS)) {
			if (remember_me != null) {
				if (remember_me.equals("true")) {
					// Cookie 등록
					// 쿠키 생성
				    Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", enc_value_1);
				    // 쿠키에 경로 추가
				    c3.setPath("/");
				    // 쿠키에 설명을 추가
				    c3.setComment("UserUUID");
				    // 쿠키 유효기간을 설정
				    c3.setMaxAge(60 * 60 * 24 * 30);
					// 쿠키 생성
				    Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", enc_value_2);
				    // 쿠키에 경로 추가
				    c4.setPath("/");
				    // 쿠키에 설명을 추가
				    c4.setComment("TokenUUID");
				    // 쿠키 유효기간을 설정
				    c4.setMaxAge(60 * 60 * 24 * 30);
				    // 응답헤더에 쿠키를 추가
				    response.addCookie(c3);
				    response.addCookie(c4);		
				}else {
					//쿠키 생성
					Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", null);
					// 쿠키에 경로 추가
					c3.setPath("/");
					// 쿠키에 설명을 추가
					c3.setComment("UserUUID");
					// 쿠키 유효기간을 설정
					c3.setMaxAge(0);
					// 쿠키 생성
					Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", null);
					// 쿠키에 경로 추가
					c4.setPath("/");
					// 쿠키에 설명을 추가
					c4.setComment("TokenUUID");
					// 쿠키 유효기간을 설정
					c4.setMaxAge(0);
					// 응답헤더에 쿠키를 추가
					response.addCookie(c3);
					response.addCookie(c4);
				}
			}else {
				//쿠키 생성
				Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", null);
				// 쿠키에 경로 추가
				c3.setPath("/");
				// 쿠키에 설명을 추가
				c3.setComment("UserUUID");
				// 쿠키 유효기간을 설정
				c3.setMaxAge(0);
				// 쿠키 생성
				Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", null);
				// 쿠키에 경로 추가
				c4.setPath("/");
				// 쿠키에 설명을 추가
				c4.setComment("TokenUUID");
				// 쿠키 유효기간을 설정
				c4.setMaxAge(0);
				// 응답헤더에 쿠키를 추가
				response.addCookie(c3);
				response.addCookie(c4);
			}
		    
			// Session 등록
			session.setAttribute(LoginChecker.LOGIN_SESSION_NAME, new String[] { enc_value_1, enc_value_2 } );
			// 결과 설정
			obj.addProperty(signinV.RESULT, signinV.RST_SUCCESS);
			obj.addProperty(signinV.MSG, "");
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "로그인 성공 : " , result[1]));
			// 결과 출력
			out.println(gson.toJson(obj));
		}else {
			// 결과 설정
			obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
			
			obj.addProperty(signinV.MSG, "새로운 기기에서 접속하여 임시로 차단되었습니다. 이메일을 확인해 주세요.");

			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "로그인 실패!"));
			// 결과 출력
			out.println(gson.toJson(obj));
		}
	}else {
		// 결과 설정
		obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
		obj.addProperty(signinV.MSG, result[1]);
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "로그인 실패!"));
		// 결과 출력
		out.println(gson.toJson(obj));
	}
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	signinV = null;
	captchaV = null;
}catch (Exception ex) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// 결과 설정
	obj.addProperty(signinV.RESULT, signinV.RST_FAIL);
	obj.addProperty(signinV.MSG, "알 수 없는 오류 발생");
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	signinV = null;
	captchaV = null;
}
%>
    