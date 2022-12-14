<%@page import="kro.kr.rhya_network.security.IPAccessChecker"%>
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
		<title>RHYA.Network IP Access Allow</title>
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
	//Rhya ?????? ?????? ??????
	RhyaLogger rl = new RhyaLogger();
	// Rhya ?????? ??????
	rl.JspName = request.getServletPath();
	rl.LogConsole = true;
	rl.LogFile = true;

	// ?????? ?????? StringBuilder
	StringBuilder sql = new StringBuilder();

	// ??????????????? ?????????
	String clientIP = GetClientIPAddress.getClientIp(request);

	// ?????????????????? ????????? ?????? ??????
	DatabaseConnection cont = new DatabaseConnection();
	// ?????????????????? ?????? ?????? ?????? ??????
	PreparedStatement stat = null;
	ResultSet rs = null;
	
	// URL ?????? ??????
	URLFilter urlFilter = new URLFilter();
	
	
	// ------------------------------------------------
	if (!IPBlockChecker.isIPBlock(clientIP)) {
		// ?????? ??????
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP ?????? ????????? ?????? ???????????? ????????? ?????????????????????. ?????? ???????????? ????????? ???????????? ??????????????????."));
		// ????????? ??????
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
		return;
	}
	// ------------------------------------------------
	
	
	// ?????????????????? ?????? ?????? ??????
	try {
		// ?????????????????? ??????
		cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
						DatabaseInfo.DATABASE_CONNECTION_URL,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
						DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
	}catch (SQLException ex1) {
		cont.Close();
		// ?????????????????? ?????? ?????? ??????
		cont = null;
		sql = null;
		// ?????? ??????
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
		// ????????? ??????
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		cont.Close();
		// ?????????????????? ?????? ?????? ??????
		cont = null;
		sql = null;
		// ?????? ??????
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
		// ????????? ??????
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}

	// ????????? ?????? ??????
	if (cont != null) {
		// ?????? ??????
		sql.append("SELECT * FROM ");
		sql.append(DatabaseInfo.DATABASE_TABLE_NAME_JSP_PAGE_SETTING);
		sql.append(" WHERE ");
		sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_ID);
		sql.append("= ?;");
		
		// ?????? ??????
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setInt(1, JspPageInfo.PageID_User_Account_Sign_Up_Email);
		// ?????? ?????? StringBuilder ?????????
		sql.delete(0,sql.length());
		// ?????? ??????
		rs = stat.executeQuery();
		// ?????? ?????? ??????
		int state = 0;
		if (rs.next()) {
			state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
		}
		// ?????? ?????? - ?????? ??????
		if (!JspPageInfo.JspPageStateManager(state)) {
			// ?????? ??????
			rs.close();
			stat.close();
			cont.Close();
			rl = null;
			sql = null;
			
			// ????????? ??????
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
			return;
		}else {
			// ???????????? ????????????
			String user_uuid = request.getParameter("uuid");
			String key = request.getParameter("key");
			// Null ??????
			if (user_uuid == null ||
				key == null) {
				// ?????? ??????
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, "?????? ?????? : ???????????? Null??? ???????????? ??? ?????????."));
				// ?????? ??????
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				
				// ????????? ??????
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 3));
				
				return;
			}
			
			// ?????? ??????
			try {
				// ?????????
				user_uuid = urlFilter.GetFilter(user_uuid);
				key = urlFilter.GetFilter(key);
				user_uuid = RhyaAES.AES_Decode(user_uuid);
				key = RhyaAES.AES_Decode(key);
				// ?????? ??????
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv1(clientIP, new String[] { "uuid", "key" },
																		  new String[] { user_uuid, key }));
				IPAccessChecker ipAccessChecker = new IPAccessChecker();
				ipAccessChecker.isAccessIPForSetNeedKey(user_uuid, key);
				
				// ?????? ??????
				rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "?????? IP ?????? ?????? | AUTH KEY:", key));
			}catch (Exception ex) {
				// ?????? ??????
				rl.Log(RhyaLogger.Type.Warning, rl.CreateLogTextv5(clientIP, ex.toString()));
				// ?????? ??????
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;
				
				// ????????? ??????
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
							Successful ip allow!
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