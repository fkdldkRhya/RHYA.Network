<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.SelfXSSFilter"%>
<%@ page import="kro.kr.rhya_network.security.URLFilter"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.DateTimeChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<!-- Copyright (c) 2018 by Colorlib (https://colorlib.com/wp/template/login-form-v2/) -->
<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Sign Up</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="apple-touch-icon" sizes="180x180" href="<%=request.getContextPath()%>/webpage/resources/icon/apple_touch_logo_icon.png">
		<link rel="icon" type="image/png" sizes="32x32" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_32x32.png">
		<link rel="icon" type="image/png" sizes="16x16" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_16x16.png">
		<link rel="manifest" href="<%=request.getContextPath()%>/webpage/resources/icon/site.webmanifest">
		<link rel="mask-icon" href="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" color="#5bbad5">
		<meta name="msapplication-TileColor" content="#da532c">
		<meta name="theme-color" content="#ffffff">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/bootstrap/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/fonts/iconic/css/material-design-iconic-font.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/animate/animate.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/css-hamburgers/hamburgers.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/animsition/css/animsition.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/select2/select2.min.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/daterangepicker/daterangepicker.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/css/util.css">
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/webpage/resources/assets/user_account/css/main.css">
	</head>
	
	
	<%
	String redPage = request.getParameter(PageParameter.REDIRECT_PAGE_ID_PARM);
	String ctoken = request.getParameter(PageParameter.IS_CREATE_TOKEN_PARM);
	int loginSuccessPage = 12;
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
	//Rhya 로거 변수 선언
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
	
	// URL 전용 필터
	URLFilter urlFilter = new URLFilter();
	
	
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
	
	
	// 데이터베이스 접속 예외 처리
	try {
		// 데이터베이스 접속
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
	}catch (SQLException ex1) {
		cont.Close();
		// 데이터베이스 접속 오류 처리
		cont = null;
		sql = null;
		// 로그 작성
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
		// 페이지 이동
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		cont.Close();
		// 데이터베이스 접속 오류 처리
		cont = null;
		sql = null;
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
		sql.append("= ?;");
		
		// 쿼리 설정
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setInt(1, JspPageInfo.PageID_User_Account_Sign_Up_Email);
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
			
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
			return;
		}else {
			// 파라미터 가져오기
			String user_uuid = request.getParameter("u_uuid");
			String email_uuid = request.getParameter("e_uuid");
			// Null 확인
			if (user_uuid == null ||
				email_uuid == null) {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 입력값은 Null이 포함되면 안 됩니다."));
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				
				// 페이지 이동
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
				
				return;
			}
			
			// 예외 처리
			try {
				// 복호화
				user_uuid = urlFilter.GetFilter(user_uuid);
				email_uuid = urlFilter.GetFilter(email_uuid);
				user_uuid = RhyaAES.AES_Decode(user_uuid);
				email_uuid = RhyaAES.AES_Decode(email_uuid);
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { "u_uuid", "e_uuid" },
																		  new String[] { user_uuid, email_uuid }));
				// 쿼리 생성
				sql.append("SELECT ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_UUID);
				sql.append(",");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_DATE);
				sql.append(",");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION);
				sql.append(" FROM ");
				sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
				sql.append(" WHERE ");
				sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
				sql.append(" = ?;");
				// 데이터베이스 접속
				stat.close();
				stat = cont.GetConnection().prepareStatement(sql.toString());
				stat.setString(1, user_uuid);
				// 쿼리 실행
				rs = stat.executeQuery();
				// 쿼리 실행 결과
				String get_email_uuid = null;
				String get_email_date = null;
				int get_email_checker = 0;
				if (rs.next()) {
					// 결과 설정
					get_email_uuid = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_UUID);
					get_email_date = rs.getString(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_DATE);
					get_email_checker = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION);
				}
				// 결과 확인
				if (get_email_date == null ||
					get_email_uuid == null) {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 데이터베이스 결괏값은 Null이 포함되면 안 됩니다."));
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
					
					// 페이지 이동
					response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
					
					return;
				}
				if (!email_uuid.equals(get_email_uuid)) {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 이메일 UUID가 일치하지 않습니다."));
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
					
					// 페이지 이동
					response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
					
					return;
				}
				// 활성화 확인
				if (get_email_checker == 1) {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 이미 계정이 활성화되었습니다."));
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
					
					// 페이지 이동
					response.sendRedirect(JspPageInfo.GetJspPageURL(request, 2));
					
					return;
				}
				// 시간 확인
				if (DateTimeChecker.isTime_H(get_email_date, 24)) {
					// 쿼리 생성 StringBuilder 초기화
					sql.delete(0,sql.length());
					// 쿼리 생성
					sql.append("UPDATE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_NAME_USER_INFO);
					sql.append(" SET ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION);
					sql.append(" = ? WHERE ");
					sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_USER_INFO_UUID);
					sql.append("= ?;");
					// 데이터베이스 접속
					stat.close();
					stat = cont.GetConnection().prepareStatement(sql.toString());
					stat.setInt(1, 1);
					stat.setString(2, user_uuid);
					// 쿼리 실행
					stat.executeUpdate();
					// 로그 출력
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "인증 성공 : 이메일 인증 성공!"));
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
				}else {
					// 로그 출력
					rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "인증 실패 : 시간 지남"));
					// 연결 종료
					rs.close();
					stat.close();
					cont.Close();
					rl = null;
					sql = null;
					
					// 페이지 이동
					response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
					
					return;
				}
			}catch (Exception ex) {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, ex.toString()));
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				
				// 페이지 이동
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
				
				return;
			}
		}
	}
	%>
	
	
	<body>
		<div class="limiter">
			<div class="container-login100">
				<div class="wrap-login100">
					<form class="login100-form validate-form" onsubmit="return false">
					
						<span class="login100-form-title p-b-48">
							<img src="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" width="72" height="72">
						</span>
						
						<span class="login100-form-title p-b-26">
							Successful sign up!
						</span>
	
						<div class="container-login100-form-btn">
							<div class="wrap-login100-form-btn">
								<div class="login100-form-bgbtn"></div>
								<button type="submit" class="login100-form-btn" id="SignupButton" onclick="location.href='<%=JspPageInfo.GetJspPageURL(request, 0)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>'">
									Sign in
								</button>
							</div>
						</div>
	
						<div class="text-center p-t-115">
							<span class="txt2">RHYA.Network / &copy; Colorlib</span>
						</div>
					</form>
				</div>
			</div>
		</div>
	
	
		<div id="dropDownSelect1"></div>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/jquery/jquery-3.2.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/animsition/js/animsition.min.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/bootstrap/js/popper.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/bootstrap/js/bootstrap.min.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/select2/select2.min.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/daterangepicker/moment.min.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/daterangepicker/daterangepicker.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/countdowntime/countdowntime.js"></script>
		<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/js/main.js"></script>
	</body>
</html>