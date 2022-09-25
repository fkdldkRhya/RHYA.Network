<%@ page import="java.sql.*"%>
<%@ page import="java.util.Random"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
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
	
	<% reCaptChaInfo captchaV = new reCaptChaInfo(); %>
	<% PageParameter.SignUp signupV = new PageParameter.SignUp(); %>
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
		const sign_up_button_id = 'SignupButton';
		const enc_int_key_id = 'INTRandom';
		const id_checker_id = 'IDChecker';
		const id_checker_value_id = 'IDCheckerTOF';
		const ko_en_num_pattern = /^[가-힣]{2,5}$/;
		const en_num_pattern = /^[0-9A-Za-z]+$/;
		const birthday_pattern = /^(19[0-9][0-9]|20\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$/
		const email_pattern = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
		
		
		function noSpaceInput_ID(obj) {
			var id_checker = document.getElementById(id_checker_value_id);
			id_checker.value = 'False';
			var str_space = /\s/;
			if (str_space.exec(obj.value)) {
				obj.focus();
				obj.value = obj.value.replace(' ', '');
			}
			return;
		}
		
		
		function noSpaceInput(obj) {
			var str_space = /\s/;
			if (str_space.exec(obj.value)) {
				obj.focus();
				obj.value = obj.value.replace(' ', '');
			}
			return;
		}
		
		
		function removeEmojis(str) {
			const regex = /(?:[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff])/g;
			return str.replace(regex, '');
		}
		
		
		function SignUpIDCheckFun() {
			// 변수 선언
			var usr_id = $('#' + usr_id_input_id).val();
			var enc_int_keys = $('#' + enc_int_key_id).val();
			
			
			// 입력 확인
			if (!(usr_id.length > 0)) {
				return;
			}
			
			
			// 공백 제거
			usr_id = usr_id.replace(/\s/gi, "");
			
			
			// 길이 확인
			if (!(usr_id.length > 4)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 최소 5글자 이상 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_id.length <= 60)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 최대 60글자까지 입력할 수 있습니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			
			
			// 특수 문자 확인
			if (!en_num_pattern.test(usr_id)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 영문, 숫자만 사용할 수 있습니다.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			
			
			// 이모티콘 제거
			usr_id = removeEmojis(usr_id);
			
			
			// 로딩 중 메시지 출력
			Swal.fire({
	            title: 'Please Wait!',
	            html: '처리 중...',
	            allowOutsideClick: false,
	            didOpen: () => {
	            	Swal.showLoading()
	            }
	        });	
			
			
			// AJAX 결과
			var ajax_result_json;
			var ajax_result_title = "아이디 중복 확인 실패";
			var ajax_result_text = "알 수 없는 오류";
			var ajax_result_icon = "error";
			
			
			// Ajax
			$.ajax({
				url: "<%=JspPageInfo.GetJspPageURL(request, 4)%>",
				type: "POST",
				data:{
					"<%=signupV.ID%>" : usr_id,
					"<%=signupV.INT_KEY%>" : enc_int_keys
				},
				
				success: function(result){
					Swal.close();
					ajax_result_json = JSON.parse(result);
					if (ajax_result_json.<%=signupV.RESULT%> == '<%=signupV.RST_SUCCESS%>') {
						$('#' + id_checker_value_id).val("True");
						ajax_result_title = "사용 가능한 아이디";
						ajax_result_text = "입력하신 아이디는 사용 가능합니다.";
						ajax_result_icon = "success";
					}else {
						$('#' + id_checker_value_id).val("False");
						ajax_result_title = "사용 불가능한 아이디";
						ajax_result_text = ajax_result_json.<%=signupV.MSG%>;
						ajax_result_icon = "error";
					}
				},
				
				error: function(){
					Swal.close();
					ajax_result_title = "아이디 중복 확인 실패!";
					ajax_result_text = "Ajax 통신 실패! 다시 시도해 주세요.";
					ajax_result_icon = "error";
				},
				
			    complete : function() {
			    	Swal.fire({
		    		    title: ajax_result_title,
		    		    text: ajax_result_text,
		    		    icon: ajax_result_icon
		    		});
			    }
			});
		}
		
		
		function SignUpFun() {
			// 변수 선언
			var usr_name = $('#' + usr_name_input_id).val();
			var usr_id = $('#' + usr_id_input_id).val();
			var usr_id_tof = $('#' + id_checker_value_id).val();
			var usr_email = $('#' + usr_email_input_id).val();
			var usr_birthday = $('#' + usr_birthday_input_id).val();
			var usr_pw = $('#' + usr_pw_input_id).val();
			var usr_confirm_pw = $('#' + usr_confirm_pw_input_id).val();
			var enc_int_keys = $('#' + enc_int_key_id).val();
			
			
			// 입력 확인
			if (!((usr_name.length > 0) &&
				(usr_id.length > 0) &&
				(usr_email.length > 0) &&
				(usr_birthday.length > 0) &&
				(usr_pw.length > 0) &&
				(usr_confirm_pw.length > 0))) {
				return;
			}
			
			
			// 공백 제거
			usr_name = $.trim(usr_name);
			usr_id = usr_id.replace(/\s/gi, "");
			usr_email = usr_email.replace(/\s/gi, "");
			usr_birthday = usr_birthday.replace(/\s/gi, "");
			usr_pw = usr_pw.replace(/\s/gi, "");
			usr_confirm_pw = usr_confirm_pw.replace(/\s/gi, "");
			
			
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
			if (!(usr_id.length > 4)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 최소 5글자 이상 입력해 주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			if (!(usr_id.length <= 60)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 최대 60글자까지 입력할 수 있습니다.',
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
			
			
			// 특수 문자 확인
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
			if (!en_num_pattern.test(usr_id)) {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디는 영문, 숫자만 사용할 수 있습니다.',
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
			
			
			// 아이디 중복 확인
			if (usr_id_tof != "True") {
				// 오류 메시지 출력
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '아이디 중복확인을 해주세요.',
					  showConfirmButton: false,
					  timer: 1500
				});
				
				// 종료
				return;
			}
			
			
			// 이모티콘 제거
			usr_name = removeEmojis(usr_name);
			usr_id = removeEmojis(usr_id);
			usr_email = removeEmojis(usr_email);
			usr_birthday = removeEmojis(usr_birthday);
			usr_pw = removeEmojis(usr_pw);
			usr_confirm_pw = removeEmojis(usr_confirm_pw);
			
			
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
				grecaptcha.execute(reCaptcha_Key, {action: 'signup'}).then(function(token) {
					// AJAX 결과
					var ajax_result_json;
					var ajax_result_title = "회원가입 실패";
					var ajax_result_text = "알 수 없는 오류";
					var ajax_result_icon = "error";
					
					
					usr_name = encodeURI(usr_name, "UTF-8");
					usr_id = encodeURI(usr_id, "UTF-8");
					usr_email = encodeURI(usr_email, "UTF-8");
					usr_birthday = encodeURI(usr_birthday, "UTF-8");
					usr_pw = encodeURI(usr_pw, "UTF-8");
					
					
					// Ajax
					$.ajax({
						url: "<%=JspPageInfo.GetJspPageURL(request, 3)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>",
						type: "POST",
						data:{
							"<%=signupV.NAME%>": usr_name,
							"<%=signupV.ID%>" : usr_id,
							"<%=signupV.EMAIL%>" : usr_email,
							"<%=signupV.BIRTHDAY%>" : usr_birthday,
							"<%=signupV.PASSWORD%>" : usr_pw,
							"<%=signupV.INT_KEY%>" : enc_int_keys,
							"<%=signupV.RE_CHAPT_CHA%>" : token
						},
						
						success: function(result){
							Swal.close();
							ajax_result_json = JSON.parse(result);
							if (ajax_result_json.<%=signupV.RESULT%> == '<%=signupV.RST_SUCCESS%>') {
								ajax_result_title = "회원가입 성공!";
								ajax_result_text = "메일에 발송된 링크를 통하여 계정을 활성화해주세요.";
								ajax_result_icon = "success";
							}else {
								ajax_result_title = "회원가입 실패";
								ajax_result_text = ajax_result_json.<%=signupV.MSG%>;
								ajax_result_icon = "error";
							}
						},
						
						error: function(){
							Swal.close();
							ajax_result_title = "회원가입 실패!";
							ajax_result_text = "Ajax 통신 실패! 다시 시도해 주세요.";
							ajax_result_icon = "error";
						},
						
					    complete : function() {
					    	Swal.fire({
				    		    title: ajax_result_title,
				    		    text: ajax_result_text,
				    		    icon: ajax_result_icon
				    		}).then((result) => {
				    			if (ajax_result_icon == 'success') {
				    				location.href = '<%=JspPageInfo.GetJspPageURL(request, 0)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>';
				    				
				    				return;
				    			}
				    		});
					    }
					});
				});
			});
		}
	</script>
	
	
	<%
	// Null 대입
	signupV = null;
	captchaV = null;
	
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
		rl = null;
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
		rl = null;
		sql = null;
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
		stat.setInt(1, JspPageInfo.PageID_User_Account_Sign_Up);
		// 쿼리 생성 StringBuilder 초기화
		sql.delete(0,sql.length());
		// 쿼리 실행
		rs = stat.executeQuery();
		// 쿼리 실행 결과
		int state = 0;
		if (rs.next()) {
			state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
		}
		// 연결 종료
		rs.close();
		stat.close();
		cont.Close();
		rl = null;
		sql = null;
		// 상태 확인 - 결과 처리
		if (!JspPageInfo.JspPageStateManager(state)) {
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
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
	
	
	<body>
		<div class="limiter">
			<div class="container-login100">
				<div class="wrap-login100">
					<form class="login100-form validate-form" onsubmit="return false">
						<input type="hidden" id="INTRandom" value="${_INT_WEB_Key_}"/>
						<input type="hidden" id="IDCheckerTOF" value="False" />
					
						<span class="login100-form-title p-b-48">
							<img src="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" width="72" height="72">
						</span>
						
						<!-- Encode html code [ Start ] -->
						<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/js/auth_v1_sign_up.js"></script>
						<!-- Encode html code [ End ] -->
	
						<div class="text-center p-t-115">
							<span class="txt1">Already have an account?</span>
							<a class="txt2" href="<%=JspPageInfo.GetJspPageURL(request, 0)%>?<%=PageParameter.REDIRECT_PAGE_ID_PARM%>=<%=loginSuccessPage%>&<%=PageParameter.IS_CREATE_TOKEN_PARM%>=<%=isCreateToken%>">로그인</a>
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