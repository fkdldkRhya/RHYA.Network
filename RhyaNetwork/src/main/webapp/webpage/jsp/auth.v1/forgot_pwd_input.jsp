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
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
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
		<title>RHYA.Network Forgot Password</title>
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
	<% PageParameter.ForgotPWD forgotpwdV = new PageParameter.ForgotPWD(); %>
	<%
	// 파라미터 가져오기
	String user_uuid = request.getParameter("u_uuid");
	String email_uuid = request.getParameter("e_uuid");
	//Rhya 로거 변수 선언
	RhyaLogger rl = new RhyaLogger();
	// Rhya 로거 설정
	rl.JspName = request.getServletPath();
	rl.LogConsole = true;
	rl.LogFile = true;
	
	// URL 전용 필터
	URLFilter urlFilter = new URLFilter();
	
	// 클라이언트 아이피
	String clientIP = GetClientIPAddress.getClientIp(request);
	
	
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
	
	
	// Null 확인
	if (user_uuid == null ||
		email_uuid == null) {
		// 로그 출력
		rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 입력값은 Null이 포함되면 안 됩니다."));
		
		// 페이지 이동
		response.sendRedirect(JspPageInfo.GetJspPageURL(request, 2));
		
		return;
	}
	
	// 복호화
	user_uuid = urlFilter.GetFilter(user_uuid);
	email_uuid = urlFilter.GetFilter(email_uuid);
	user_uuid = RhyaAES.AES_Decode(user_uuid);
	email_uuid = RhyaAES.AES_Decode(email_uuid);
	
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
	
	<script src="https://www.google.com/recaptcha/api.js?render=<%=captchaV.reCaptChaPublicKey%>"></script>
	<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
	<script type="text/javascript">
		const reCaptcha_Key = '<%=captchaV.reCaptChaPublicKey%>';
		const usr_pw_input_id = 'Password';
		const usr_pwc_input_id = 'PasswordC';
		const enc_int_key_id = 'INTRandom';
		
		
		function noSpaceInput(obj) {
			var str_space = /\s/;
			if (str_space.exec(obj.value)) {
				obj.focus();
				obj.value = obj.value.replace(' ', '');
			}
			return;
		}
		
		
		function ResetPWD() {
			// 변수 선언
			var usr_pw = $('#' + usr_pw_input_id).val();
			var usr_pwc = $('#' + usr_pwc_input_id).val();
			var enc_int_keys = $('#' + enc_int_key_id).val();
			var uuid_user = '<%=user_uuid%>';
			var uuid_auth = '<%=email_uuid%>';
			
			// 입력 확인
			if (!((usr_pw.length > 0) &&
				  (usr_pwc.length > 0))) {
				return;
			}
			
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
			if (!(usr_pwc.length > 0)) {
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
			if (!(usr_pwc == usr_pw)) {
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
			
			
			// 토큰 생성
			grecaptcha.ready(function() {
				grecaptcha.execute(reCaptcha_Key, {action: 'forgotpwd_input'}).then(function(token) {
					// AJAX 결과
					var ajax_result_json;
					
					
					usr_pw = encodeURI(usr_pw, "UTF-8");
					usr_pwc = encodeURI(usr_pwc, "UTF-8");
					uuid_user = encodeURI(uuid_user, "UTF-8");
					uuid_auth = encodeURI(uuid_auth, "UTF-8");
					
					
					// Ajax
					$.ajax({
						url: "<%=JspPageInfo.GetJspPageURL(request, 8)%>",
						type: "POST",
						data:{
							"<%=forgotpwdV.PASSWORD%>" : usr_pw,
							"<%=forgotpwdV.PASSWORD_C%>" : usr_pwc,
							"<%=forgotpwdV.UUID_USER%>" : uuid_user,
							"<%=forgotpwdV.UUID_AUTH%>" : uuid_auth,
							"<%=forgotpwdV.INT_KEY%>" : enc_int_keys,
							"<%=forgotpwdV.RE_CHAPT_CHA%>" : token
						},
						
						success: function(result){
							Swal.close();
							ajax_result_json = JSON.parse(result);
							if (ajax_result_json.<%=forgotpwdV.RESULT%> == '<%=forgotpwdV.RST_SUCCESS%>') {
						    	Swal.fire({
					    		    title: "비밀번호 변경 성공!",
					    		    text: "비밀번호 변경을 성공했습니다. 로그인 화면으로 전환됩니다.",
					    		    icon: "success"
					    		}).then((result) => {
				    				location.href = '<%=JspPageInfo.GetJspPageURL(request, 0)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>';
					    		});
							}else {
						    	Swal.fire({
					    		    title: "비밀번호 변경 실패!",
					    		    html: ajax_result_json.<%=forgotpwdV.MSG%>,
					    		    icon: "error"
					    		});
							}
						},
						
						error: function(){
							Swal.close();
					    	Swal.fire({
				    		    title: "비밀번호 변경 실패!",
				    		    text: "Ajax 통신 실패! 다시 시도해 주세요.",
				    		    icon: "error"
				    		});
						}
					});
				});
			});
		}
	</script>
	
	<%
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
		// 데이터베이스 접속 오류 처리
		cont.Close();
		cont = null;
		sql = null;
		// 로그 작성
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
		// 페이지 이동
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		// 데이터베이스 접속 오류 처리
		cont.Close();
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
		stat.setInt(1, JspPageInfo.PageID_User_Account_ForgotPW_Input);
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
			// 예외 처리
			try {
				// 로그 출력
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { "u_uuid", "e_uuid" },
																		  new String[] { user_uuid, email_uuid }));
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
				stat.setString(1, user_uuid);
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
					rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "인증 실패 : 해당 정보를 가진 계정이 존재하지 않습니다."));
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
				// 시간 확인
				if (DateTimeChecker.isTime_H(get_email_date, 5)) {
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
	
	//정수형 랜덤키 생성
	java.util.Random random = new java.util.Random();
	int randomInt = random.nextInt();
	//양수로 변경
	randomInt = Math.abs(randomInt);
	//정수형 랜덤키 설정
	session.setAttribute(ParameterManipulation.INTRandomKeySession, RhyaAES.AES_Encode(Integer.toString(randomInt)));
	%>
	
	
	<body>
		<div class="limiter">
			<div class="container-login100">
				<div class="wrap-login100">
					<form class="login100-form validate-form" onsubmit="return false">
						<input type="hidden" id="INTRandom" value="${_INT_WEB_Key_}" />
						
						<span class="login100-form-title p-b-48">
							<img src="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" width="72" height="72">
						</span>
						
						<!-- Encode html code [ Start ] -->
						<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/js/auth_v1_forgot_pwd_input.js"></script>
						<!-- Encode html code [ End ] -->
	
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