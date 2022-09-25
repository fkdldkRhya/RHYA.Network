<%@ page import="java.sql.*"%>
<%@ page import="java.util.Random"%>
<%@ page import="java.lang.StringBuilder"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<!-- Copyright (c) 2018 by Colorlib (https://colorlib.com/wp/template/login-form-v2/) -->
<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Edit My Account</title>
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
	
	<% reCaptChaInfo captchaV = new reCaptChaInfo(); %>
	<% PageParameter.SignUp signupV = new PageParameter.SignUp(); %>
	<%
	String isNoRed = request.getParameter("isNoRed");
	String redPage = request.getParameter(PageParameter.REDIRECT_PAGE_ID_PARM);
	String ctoken = request.getParameter(PageParameter.IS_CREATE_TOKEN_PARM);
	int isNoRedInt = 0;
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
	if (isNoRed != null) {
		if (isNoRed.equals("1")) {
			isNoRedInt = 1;
		}
	}
	StringBuilder scriptBuilder = new StringBuilder();
	scriptBuilder.append("location.href = '");
	scriptBuilder.append(JspPageInfo.GetJspPageURL(request, 12));
	scriptBuilder.append("';");
	%>
	
	
	<%
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
		// 데이터베이스 접속 오류 처리
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
		// 연결 종료
		cont.Close();
		cont = null;
		sql = null;
		// 페이지 이동
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		// 데이터베이스 접속 오류 처리
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
		// 연결 종료
		cont.Close();
		cont = null;
		sql = null;
		// 페이지 이동
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}
	
	// 사용자 데이터
	String name = "";
	String id = "";
	String email = "";
	String birthday = "";
	
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
		stat.setInt(1, JspPageInfo.PageID_User_Account_Info_Edit);
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
			sql = null;
			
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
			return;
		}
		
		// 파라미터 입력
		String authToken = request.getParameter("auth");
		
		// 세션 데이터 확인 - 자동 로그인 확인
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		// Null 값 확인
		if (login_session != null) {
			// 자동 로그인
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			// 자동 로그인 확인
			if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				name = auto_login_result[4];
				id = auto_login_result[3];
				email = auto_login_result[5];
				birthday = auto_login_result[6];
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : DB Get Data Success - Session"));
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				sql = null;
			}else {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : Login Fail"));
				
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				sql = null;
				// 페이지 이동
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0));
				
				return;
			}
		}else if (authToken != null) {
			AuthTokenChecker authTokenChecker = new AuthTokenChecker();
			// 자동 로그인
			String[] auto_login_result = authTokenChecker.getMoreAuthInfo(authToken);
			// 자동 로그인 확인
			if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
				name = auto_login_result[4];
				id = auto_login_result[3];
				email = auto_login_result[5];
				birthday = auto_login_result[6];
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : DB Get Data Success - Session"));

			}else {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : Login Fail"));

				sql = null;
				// 페이지 이동
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0));
				
				return;
			}
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : DB Get Data Success - Auth token"));
			
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			sql = null;
		}else {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Edit_My_Account : No Parameter AND No Session"));
			
			// 연결 종료
			rs.close();
			stat.close();
			cont.Close();
			sql = null;
			// 페이지 이동
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0));
			
			return;
		}
	}
	
	//정수형 랜덤키 생성
	Random random = new Random();
	int randomInt = random.nextInt();
	//양수로 변경
	randomInt = Math.abs(randomInt);
	//정수형 랜덤키 설정
	session.setAttribute(ParameterManipulation.INTRandomKeySession, RhyaAES.AES_Encode(Integer.toString(randomInt)));
	%>
	
	<script src="https://www.google.com/recaptcha/api.js?render=<%=captchaV.reCaptChaPublicKey%>"></script>
	<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
	<script type="text/javascript">
		const reCaptcha_Key = '<%=captchaV.reCaptChaPublicKey%>';
		const usr_name_input_id = 'Name';
		const usr_id_input_id = 'ID';
		const usr_email_input_id = 'Email';
		const usr_birthday_input_id = 'Birthday';
		const usr_pw_input_id = 'Password';
		const usr_confirm_pw_input_id = 'ConfirmPassword';
		const usr_pw_o_input_id = 'OriginalPassword';
		const enc_int_key_id = 'INTRandom';
		const ko_en_num_pattern = /^[가-힣]{2,5}$/;
		const en_num_pattern = /^[0-9A-Za-z]+$/;
		const birthday_pattern = /^(19[0-9][0-9]|20\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$/
		const email_pattern = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
		
		
		function noSpaceInput(obj) {
			var str_space = /\s/;
			if (str_space.exec(obj.value)) {
				obj.focus();
				obj.value = obj.value.replace(' ', '');
			}
			return;
		}
		
		
		function LogoutAll() {
			var enc_int_keys = $('#' + enc_int_key_id).val();
			
			grecaptcha.ready(function() {
				grecaptcha.execute(reCaptcha_Key, {action: 'editmyaccount_logoutall'}).then(function(token) {
					// Ajax
					$.ajax({
						<%if (request.getParameter("auth") != null) { %>
						url: "<%=JspPageInfo.GetJspPageURL(request, 41)%>?auth=<%=request.getParameter("auth")%>",
						<%}else {%>
						url: "<%=JspPageInfo.GetJspPageURL(request, 41)%>",
						<%}%>
						type: "POST",
						data:{
							"<%=signupV.INT_KEY%>" : enc_int_keys,
							"<%=signupV.RE_CHAPT_CHA%>" : token
						},
						
						success: function(result){
							Swal.close();
							ajax_result_json = JSON.parse(result);
							if (ajax_result_json.<%=signupV.RESULT%> == '<%=signupV.RST_SUCCESS%>') {
						    	Swal.fire({
					    		    title: "로그아웃 성공!",
					    		    text: "모든 기기에서의 로그아웃 작업이 성공적으로 처리되었습니다.",
					    		    icon: "success"
						    	}).then((result) => {
						    		location.href = '<%=JspPageInfo.GetJspPageURL(request, 0)%>';
					    		});
							}else {
						    	Swal.fire({
					    		    title: "로그아웃 요청 실패!",
					    		    html: ajax_result_json.<%=signupV.MSG%>,
					    		    icon: "error"
						    	});
							}
						},
						
						error: function(){
							Swal.close();
					    	Swal.fire({
				    		    title: "로그아웃 요청 실패!",
				    		    text: "Ajax 통신 실패! 다시 시도해 주세요.",
				    		    icon: "error"
				    		});
						}
					});
				});
			});
		}
		
		function ResetPW() {
			// 변수 선언
			var usr_pw = $('#' + usr_pw_input_id).val();
			var usr_confirm_pw = $('#' + usr_confirm_pw_input_id).val();
			var usr_password_o = $('#' + usr_pw_o_input_id).val();
			var enc_int_keys = $('#' + enc_int_key_id).val();
			
			// 입력 확인
			if (!((usr_pw.length > 0) && (usr_confirm_pw.length > 0) && (usr_password_o.length > 0))) {
				return;
			}
			
			
			usr_pw = usr_pw.replace(/\s/gi, "");
			usr_confirm_pw = usr_confirm_pw.replace(/\s/gi, "");
			usr_password_o = usr_password_o.replace(/\s/gi, "");
			
			
			if (!(usr_pw.length > 7)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '비밀번호는 최소 8글자 이상 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_confirm_pw.length > 0)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '비밀번호 확인을 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_confirm_pw == usr_pw)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '비밀번호 확인을 다시 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			
			
			// 로딩 중 메시지 출력
			Swal.fire({
                title: 'Please Wait!',
                html: '처리 중...',
                allowOutsideClick: false,
                didOpen: () => {
                	Swal.showLoading()
                }
            });	
			
			usr_pw = encodeURI(usr_pw, "UTF-8");
			usr_password_o = encodeURI(usr_password_o, "UTF-8");
			
			grecaptcha.ready(function() {
				grecaptcha.execute(reCaptcha_Key, {action: 'editmyaccount_pw'}).then(function(token) {
					// Ajax
					$.ajax({
						<%if (request.getParameter("auth") != null) { %>
						url: "<%=JspPageInfo.GetJspPageURL(request, 20)%>?auth=<%=request.getParameter("auth")%>&<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>",
						<%}else {%>
						url: "<%=JspPageInfo.GetJspPageURL(request, 20)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>",
						<%}%>
						type: "POST",
						data:{
							"<%=signupV.PASSWORD%>" : usr_pw,
							"user_pw_o" : usr_password_o,
							"<%=signupV.INT_KEY%>" : enc_int_keys,
							"<%=signupV.RE_CHAPT_CHA%>" : token
						},
						
						success: function(result){
							Swal.close();
							ajax_result_json = JSON.parse(result);
							if (ajax_result_json.<%=signupV.RESULT%> == '<%=signupV.RST_SUCCESS%>') {
						    	Swal.fire({
					    		    title: "비밀번호 변경 성공!",
					    		    text: "사용자의 비밀번호를 성공적으로 변경하였습니다.",
					    		    icon: "success"
						    	}).then((result) => {
						    		<%
						    		if (isNoRedInt != 1) {
						    			out.println(scriptBuilder.toString());
						    		}
			    					%>
					    		});
							}else {
						    	Swal.fire({
					    		    title: "비밀번호 변경 요청 실패!",
					    		    html: ajax_result_json.<%=signupV.MSG%>,
					    		    icon: "error"
						    	}).then((result) => {
						    		<%
						    		if (isNoRedInt != 1) {
						    			out.println(scriptBuilder.toString());
						    		}
			    					%>
					    		});
							}
						},
						
						error: function(){
							Swal.close();
					    	Swal.fire({
				    		    title: "비밀번호 변경 요청 실패!",
				    		    text: "Ajax 통신 실패! 다시 시도해 주세요.",
				    		    icon: "error"
				    		});
						}
					});
				});
			});
		}
		
		function Save() {
			// 변수 선언
			var usr_name = $('#' + usr_name_input_id).val();
			var usr_birthday = $('#' + usr_birthday_input_id).val();
			var usr_email = $('#' + usr_email_input_id).val();
			var usr_password_o = $('#' + usr_pw_o_input_id).val();
			var enc_int_keys = $('#' + enc_int_key_id).val();

			
			// 공백 제거
			usr_name = $.trim(usr_name);
			usr_password_o = usr_password_o.replace(/\s/gi, "");
			usr_email = usr_email.replace(/\s/gi, "");
			usr_birthday = usr_birthday.replace(/\s/gi, "");
			
			// 입력 확인
			if (!((usr_name.length > 0) &&
				  (usr_birthday.length > 0) &&
				  (usr_email.length > 0) &&
				  (usr_password_o.length > 0))) {
				return;
			}
			
			// 입력 확인
			if (!(usr_name.length > 1)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이름은 최소 2글자 이상 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_name.length <= 5)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이름은 최대 5글자까지 입력할 수 있습니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			
			if (!(usr_email.length > 2)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이메일을 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_email.length <= 60)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이메일는 최대 60글자까지 입력할 수 있습니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!birthday_pattern.test(usr_birthday)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '생일이 올바른 형식이 아닙니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!ko_en_num_pattern.test(usr_name)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이름에는 한글만 사용할 수 있습니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!email_pattern.test(usr_email)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '이메일이 올바른 형식이 아닙니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			// 로딩 중 메시지 출력
			Swal.fire({
                title: 'Please Wait!',
                html: '처리 중...',
                allowOutsideClick: false,
                didOpen: () => {
                	Swal.showLoading()
                }
            });	
			
			
			// 토큰 생성
			grecaptcha.ready(function() {
				grecaptcha.execute(reCaptcha_Key, {action: 'editmyaccount'}).then(function(token) {
					// AJAX 결과
					var ajax_result_json;
					
					usr_name = encodeURI(usr_name, "UTF-8");
					usr_birthday = encodeURI(usr_birthday, "UTF-8");
					usr_email = encodeURI(usr_email, "UTF-8");
					usr_password_o = encodeURI(usr_password_o, "UTF-8");
					
					
					// Ajax
					$.ajax({
						<%if (request.getParameter("auth") != null) { %>
						url: "<%=JspPageInfo.GetJspPageURL(request, 17)%>?auth=<%=request.getParameter("auth")%>&<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>",
						<%}else {%>
						url: "<%=JspPageInfo.GetJspPageURL(request, 17)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>",
						<%}%>
						type: "POST",
						data:{
							"<%=signupV.NAME%>" : usr_name,
							"<%=signupV.BIRTHDAY%>" : usr_birthday,
							"<%=signupV.EMAIL%>" : usr_email,
							"user_pw_o" : usr_password_o,
							"<%=signupV.INT_KEY%>" : enc_int_keys,
							"<%=signupV.RE_CHAPT_CHA%>" : token
						},
						
						success: function(result){
							Swal.close();
							ajax_result_json = JSON.parse(result);
							if (ajax_result_json.<%=signupV.RESULT%> == '<%=signupV.RST_SUCCESS%>') {
						    	Swal.fire({
					    		    title: "사용자 정보 변경 요청 성공!",
					    		    text: "사용자의 정보를 정상적으로 변경하였습니다.",
					    		    icon: "success"
					    		}).then((result) => {
						    		<%
						    		if (isNoRedInt != 1) {
						    			out.println(scriptBuilder.toString());
						    		}
			    					%>
					    		});
							}else {
						    	Swal.fire({
					    		    title: "사용자 정보 변경 요청 실패!",
					    		    html: ajax_result_json.<%=signupV.MSG%>,
					    		    icon: "error"
					    		}).then((result) => {
						    		<%
						    		if (isNoRedInt != 1) {
						    			out.println(scriptBuilder.toString());
						    		}
			    					%>
					    		});;
							}
						},
						
						error: function(){
							Swal.close();
					    	Swal.fire({
				    		    title: "사용자 정보 변경 요청 실패!",
				    		    text: "Ajax 통신 실패! 다시 시도해 주세요.",
				    		    icon: "error"
				    		});
						}
					});
				});
			});
		}
	</script>
	
	<body>
		<div class="limiter">
			<div class="container-login100">
				<div class="wrap-login100">
					<form class="login100-form validate-form" onsubmit="return false">
						<input type="hidden" id="INTRandom" value="${_INT_WEB_Key_}" />
					
						<span class="login100-form-title p-b-48">
							<img src="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" width="72" height="72">
						</span>
						
						<span class="login100-form-title p-b-26">
							Edit account information
						</span>
						
						<div class="wrap-input100 validate-input">
							<input class="input100" type="text" id="ID" required value="<%=id%>" readonly="readonly">
							<span class="focus-input100" data-placeholder="ID"></span>
						</div>
						
						<div class="wrap-input100 validate-input">
							<input class="input100" type="text" id="Name" required value="<%=name%>">
							<span class="focus-input100" data-placeholder="Name"></span>
						</div>
						
						<div class="wrap-input100 validate-input">
							<input class="input100" type="email" id="Email" onkeyup="noSpaceInput(this)" onchange="noSpaceInput(this)" required value="<%=email%>">
							<span class="focus-input100" data-placeholder="Email"></span>
						</div>
						
						<div class="wrap-input100 validate-input">
							<input class="input100" type="date" id="Birthday" pattern="\d{4}-\d{2}-\d{2}" max="9999-12-31" required value="<%=birthday%>">
							<span class="focus-input100" data-placeholder="Birthday"></span>
						</div>
						
						<div class="wrap-input100 validate-input" data-validate="Password">
							<span class="btn-show-pass">
								<i class="zmdi zmdi-eye"></i>
							</span>
							<input class="input100" type="password" id="Password" onkeyup="noSpaceInput(this);" onchange="noSpaceInput(this);" required > 
							<span class="focus-input100" data-placeholder="Password"></span>
						</div>					
					
						<div class="wrap-input100 validate-input" data-validate="Confirm Password">
							<span class="btn-show-pass">
								<i class="zmdi zmdi-eye"></i>
							</span>
							<input class="input100" type="password" id="ConfirmPassword" onkeyup="noSpaceInput(this);" onchange="noSpaceInput(this);" required>
							<span class="focus-input100" data-placeholder="Confirm Password"></span>
						</div>
						
						<div class="wrap-input100 validate-input" data-validate="Original Password">
							<span class="btn-show-pass">
								<i class="zmdi zmdi-eye"></i>
							</span>
							<input class="input100" type="password" id="OriginalPassword" onkeyup="noSpaceInput(this);" onchange="noSpaceInput(this);" required>
							<span class="focus-input100" data-placeholder="Original Password"></span>
						</div>
						
						<div class="container-login100-form-btn">
							<div class="button-8" id="IDChecker" onclick="ResetPW()">
						        <div class="eff-8"></div>
								<p>비밀번호 변경</p>
							</div>
						</div>
						
						<div class="container-login100-form-btn">
							<div class="wrap-login100-form-btn">
								<div class="login100-form-bgbtn"></div>
								<button type="submit" class="login100-form-btn" id="sign-in-button" onclick="Save()">
									Save
								</button>
							</div>
						</div>
	
						<div class="text-center p-t-115">
							<%if (request.getParameter("backurl") == null) { %>
							<a class="txt1" href="<%=JspPageInfo.GetJspPageURL(request, 12)%>">돌아가기</a>
							<a class="txt1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>
							<%}%>
							<a class="txt1" href="#" onclick="LogoutAll()">모두 로그아웃</a>
							<br><br>
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