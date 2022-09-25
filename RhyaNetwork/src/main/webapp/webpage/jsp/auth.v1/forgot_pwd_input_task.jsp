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
<%@ page import="kro.kr.rhya_network.security.RhyaSHA512"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.DateTimeChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>

<%
//변수 클레스 선언
PageParameter.ForgotPWD forgotpwdV = new PageParameter.ForgotPWD();
reCaptChaInfo captchaV = new reCaptChaInfo();
RhyaSHA512 rhyaSHA512 = new RhyaSHA512();

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
	stat.setInt(1, JspPageInfo.PageID_User_Account_ForgotPW_Input_Task);
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
String pw = URLDecoder.decode(request.getParameter(forgotpwdV.PASSWORD), "UTF-8");
String pwc = URLDecoder.decode(request.getParameter(forgotpwdV.PASSWORD_C), "UTF-8");
String uuid = URLDecoder.decode(request.getParameter(forgotpwdV.UUID_USER), "UTF-8");
String auth = URLDecoder.decode(request.getParameter(forgotpwdV.UUID_AUTH), "UTF-8");
String key = request.getParameter(forgotpwdV.INT_KEY);
String token = request.getParameter(forgotpwdV.RE_CHAPT_CHA);

//Null 확인
if (pw == null ||
    pwc == null ||
	key == null ||
	token == null) {
	
	// 결과 설정
	obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
	obj.addProperty(forgotpwdV.MSG, "입력값은 Null이 포함되면 안 됩니다.");
	// 로그 출력
	rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 입력값은 Null이 포함되면 안 됩니다."));
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
	rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { forgotpwdV.PASSWORD, forgotpwdV.PASSWORD_C, forgotpwdV.INT_KEY, forgotpwdV.RE_CHAPT_CHA },
															  new String[] { pw, pwc, key, token }));
	// XSS 필터링
	key = SelfXSSFilter.TextXSSFilter(key);
	// 정수형 인증키 비교
	if (!int_random_key_org.equals(key)) {
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "인증키가 일치하지 않습니다.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 인증키가 일치하지 않습니다."));
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
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
	sql.append(",");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
	sql.append(" FROM ");
	sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
	sql.append(" WHERE ");
	sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
	sql.append(" = ?;");
	// 데이터베이스 접속
	stat.close();
	stat = cont.GetConnection().prepareStatement(sql.toString());
	stat.setString(1, uuid);
	// 쿼리 실행
	rs = stat.executeQuery();
	// 쿼리 실행 결과
	String get_email_uuid = null;
	String get_email_date = null;
	if (rs.next()) {
		// 결과 설정
		get_email_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID);
		get_email_date = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE);
	}
	// 결과 확인
	if (get_email_date == null ||
		get_email_uuid == null) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 데이터베이스 결괏값은 Null이 포함되면 안 됩니다."));
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "데이터베이스 결괏값은 Null이 포함되면 안 됩니다.");
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
	if (!auth.equals(get_email_uuid)) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 해당 정보를 가진 계정이 존재하지 않습니다."));
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
		
		return;
	}
	// 비밀번호 확인
	if (!rhyaSHA512.getSHA512(pw).equals(rhyaSHA512.getSHA512(pwc))) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 비밀번호와 비밀번호 확인에 입력된 값이 일치하지 않습니다."));
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "비밀번호와 비밀번호 확인에 입력된 값이 일치하지 않습니다.");
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
		return;
	}
	if (!(pw.length() > 7)) {
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "비밀번호는 최소 8글자 이상 입력해 주세요.");
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "회원가입 실패 : 비밀번호는 최소 8글자 이상 입력해 주세요."));
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
	// 시간 확인
	if (DateTimeChecker.isTime_H(get_email_date, 5)) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 변경 : 이메일 인증 성공!"));
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
		stat.setString(1, null);
		stat.setString(2, null);
		stat.setString(3, uuid);
		// 쿼리 실행
		stat.executeUpdate();
		// 쿼리 생성 StringBuilder 초기화
		sql.delete(0,sql.length());
		// 쿼리 생성
		sql.append("UPDATE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER);
		sql.append(" SET ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_USER_PW);
		sql.append(" = ? WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_UUID);
		sql.append(" = ?;");
		// 데이터베이스 접속
		stat.close();
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setString(1, rhyaSHA512.getSHA512(pw));
		stat.setString(2, uuid);
		// 쿼리 실행
		stat.executeUpdate();
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 변경 : 비밀번호 변경 성공!"));
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
	}else {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "비밀번호 변경 실패 : 시간 지남"));
		// 결과 설정
		obj.addProperty(forgotpwdV.RESULT, forgotpwdV.RST_FAIL);
		obj.addProperty(forgotpwdV.MSG, "해당 요청 만료 시간이 지났습니다.");
		// 결과 출력
		out.println(gson.toJson(obj));
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		
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

