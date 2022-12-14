<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
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
<%@ page import="kro.kr.rhya_network.email.EmailSendDATA"%>
<%@ page import="kro.kr.rhya_network.email.SendEmail"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
String redPage = request.getParameter(PageParameter.REDIRECT_PAGE_ID_PARM);
String ctoken = request.getParameter(PageParameter.IS_CREATE_TOKEN_PARM);
int loginSuccessPage = JspPageInfo.PageID_Rhya_Network_Main;
int isCreateToken = 0;
boolean isCreateTokenTOF = true;
if (redPage != null) {
	loginSuccessPage = Integer.parseInt(redPage);
}
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
//변수 클레스 선언
PageParameter.ForgotPWD forgotpwdV = new PageParameter.ForgotPWD();
reCaptChaInfo captchaV = new reCaptChaInfo();
EmailSendDATA.ForgotPassword emailSendData = new EmailSendDATA.ForgotPassword();
SendEmail sendEmail = new SendEmail();

// URL 직접 접근 확인
String strReferer = request.getHeader("referer");
if(strReferer == null) {
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
	sql = null;
	forgotpwdV = null;
	captchaV = null;
	// 로그 작성
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
	// 페이지 이동
	response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
	
	return;
}catch (ClassNotFoundException ex2) {
	// 데이터베이스 접속 오류 처리
	cont.Close();
	sql = null;
	forgotpwdV = null;
	captchaV = null;
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
	stat.setInt(1, JspPageInfo.PageID_User_Account_ForgotPW_Task);
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
		forgotpwdV = null;
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
String name = URLDecoder.decode(request.getParameter(forgotpwdV.NAME), "UTF-8");
String id = URLDecoder.decode(request.getParameter(forgotpwdV.ID), "UTF-8");
String email = URLDecoder.decode(request.getParameter(forgotpwdV.EMAIL), "UTF-8");
String key = request.getParameter(forgotpwdV.INT_KEY);
String token = request.getParameter(forgotpwdV.RE_CHAPT_CHA);

//Null 확인
if (name == null ||
    id == null ||
	email == null ||
	key == null ||
	token == null) {
	
	// 결과 설정
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "입력값은 Null이 포함되면 안 됩니다.");
	// 로그 출력
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 요청 실패 : 입력값은 Null이 포함되면 안 됩니다."));
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	forgotpwdV = null;
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
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { forgotpwdV.NAME, forgotpwdV.ID, forgotpwdV.EMAIL, forgotpwdV.INT_KEY, forgotpwdV.RE_CHAPT_CHA },
															  new String[] { name, id, email, key, token }));
	// XSS 필터링
	name = SelfXSSFilter.TextXSSFilter(name);
	id = SelfXSSFilter.TextXSSFilter(id);
	key = SelfXSSFilter.TextXSSFilter(key);
	// 정수형 인증키 비교
	if (!int_random_key_org.equals(key)) {
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "인증키가 일치하지 않습니다.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 요청 실패 : 인증키가 일치하지 않습니다."));
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		forgotpwdV = null;
		captchaV = null;
		
		return;
	}
	// reCaptCha 검사
	if (!captchaV.reCaptChaChecker(token)) {
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "Google reCAPTCHA v3를 통과하지 못했습니다.");
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
	// 쿼리 생성
	sql.append("SELECT ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
	sql.append(" FROM ");
	sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
	sql.append(" WHERE ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_ID);
	sql.append(" =?;");
	// 데이터베이스 접속
	stat.close();
	stat = cont.GetConnection().prepareStatement(sql.toString());
	stat.setString(1, id);
	// 쿼리 실행
	rs = stat.executeQuery();
	// 쿼리 결과
	boolean result = rs.next();
	// 결과 처리
	if (result) {
		// 계정 아이디
	 	final String user_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		
		// 계정 정보 확인
		// ===================================================================================
		// 쿼리 생성 StringBuilder 초기화
		sql.delete(0,sql.length());
		// 쿼리 생성
		sql.append("SELECT * ");
		sql.append(" FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" =?;");
		// 데이터베이스 접속
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, user_uuid);
		// 쿼리 실행
		rs = stat.executeQuery();
		rs.next();
		// 데이터 비교
		if (!(rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_NAME).equals(name) &&
			rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_EMAIL).equals(email))) {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 변경 요청 : 해당 정보를 가진 계정이 존재하지 않습니다."));
			// 결과 설정
			obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
			obj.addProperty(forgotpwdV.MSG, "해당 정보를 가진 계정이 존재하지 않습니다.");
			// 결과 출력
			out.println(gson.toJson(obj));
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			rl = null;
			sql = null;
			forgotpwdV = null;
			captchaV = null;
			return;
		}
		// ===================================================================================
		
		// 데이터 정보 생성
		java.util.UUID uuid_ = java.util.UUID.randomUUID();
		final String uuid = uuid_.toString();
		final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
		// 메일 데이터
		final String email_url = emailSendData.Url(request, user_uuid.toString(), uuid.toString(), loginSuccessPage, isCreateToken);
		// 메일 전송
		sendEmail.Send(sendEmail.GetProperties(), emailSendData.Html(id, email_url), emailSendData.Title(id), email);
		// 쿼리 생성 StringBuilder 초기화
		sql.delete(0,sql.length());
		// 쿼리 생성
		sql.append("UPDATE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
		sql.append(" SET ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
		sql.append(" = ?, ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
		sql.append(" = ? WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
		sql.append(" = ?;");
		// 데이터베이스 접속
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, uuid);
		stat.setString(2, date);
		stat.setString(3, user_uuid);
		// 쿼리 실행
		stat.executeUpdate();
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 재설정 요청 성공!"));
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_SUCCESS);
		obj.addProperty(forgotpwdV.MSG, "");
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		forgotpwdV = null;
		
		return;	
	}else {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 변경 요청 : 해당 정보를 가진 계정이 존재하지 않습니다."));
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "해당 정보를 가진 계정이 존재하지 않습니다.");
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		forgotpwdV = null;
		captchaV = null;
		return;
	}
}catch (Exception ex) {
	// 로그 출력
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
	// 결과 설정
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "알 수 없는 오류 발생");
	// 결과 출력
	out.println(gson.toJson(obj));
	// 연결 종료
	rs.close();
	stat.close();
	cont.Close();
	rl = null;
	sql = null;
	forgotpwdV = null;
	captchaV = null;
}
%>