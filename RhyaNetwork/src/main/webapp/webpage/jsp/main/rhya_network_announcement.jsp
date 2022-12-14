<%@page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Random"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="kro.kr.rhya_network.captcha.reCaptChaInfo"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>
<%@ page import="kro.kr.rhya_network.page.PageParameter"%>
<%@ page import="kro.kr.rhya_network.security.RhyaAES"%>
<%@ page import="kro.kr.rhya_network.security.ParameterManipulation"%>
<%@ page import="kro.kr.rhya_network.logger.RhyaLogger"%>
<%@ page import="kro.kr.rhya_network.logger.GetClientIPAddress"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseInfo"%>
<%@ page import="kro.kr.rhya_network.database.DatabaseConnection"%>
<%@ page import="kro.kr.rhya_network.utils.db.DatabaseManager"%>
<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.util.RhyaAnnouncementVO"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Announcement</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="apple-touch-icon" sizes="180x180" href="<%=request.getContextPath()%>/webpage/resources/icon/apple_touch_logo_icon.png">
		<link rel="icon" type="image/png" sizes="32x32" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_32x32.png">
		<link rel="icon" type="image/png" sizes="16x16" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_16x16.png">
		<link rel="manifest" href="<%=request.getContextPath()%>/webpage/res/icon/site.webmanifest">
		<link rel="mask-icon" href="<%=request.getContextPath()%>/webpage/res/icon/server_logo.svg" color="#5bbad5">
		<meta name="msapplication-TileColor" content="#da532c">
		<meta name="theme-color" content="#ffffff">
		<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;700&display=swap" rel="stylesheet">
		<link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/bootstrap.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/fontawesome-all.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/aos.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/swiper.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/style.css" rel="stylesheet">
	    
	</head>
	
	
	<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
	<script src="https://kit.fontawesome.com/f1def33959.js" crossorigin="anonymous"></script>
	
	
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
	// Rhya ?????? ?????? ??????
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
		// ?????????????????? ?????? ?????? ??????
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
		// ?????? ??????
		cont = null;
		sql = null;
		// ????????? ??????
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		// ?????????????????? ?????? ?????? ??????
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
		// ?????? ??????
		cont = null;
		sql = null;
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
		sql.append("=");
		sql.append("?;");
	
		
		// ?????? ??????
		stat = cont.GetConnection().prepareStatement(sql.toString());
		stat.setInt(1, JspPageInfo.PageID_Rhya_Network_Announcement);
		// ?????? ?????? StringBuilder ?????????
		sql.delete(0,sql.length());
		// ?????? ??????
		rs = stat.executeQuery();
		// ?????? ?????? ??????
		int state = 0;
		if (rs.next()) {
			state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
		}
		// ?????? ??????
		rs.close();
		stat.close();
		cont.Close();
		sql = null;
		// ?????? ?????? - ?????? ??????
		if (!JspPageInfo.JspPageStateManager(state)) {
			// ????????? ??????
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
			return;
		}
	}

	
	ArrayList<RhyaAnnouncementVO> rhyaAnnouncementVOS = new ArrayList<RhyaAnnouncementVO>();
	String userName = null;
	boolean isLoginUser = false;
	
	
	try {
		// Auth token ??????
		boolean isAuthUser = false;
		try {
			String getAuthToken = request.getParameter("authToken");
			if (getAuthToken != null) {
				AuthTokenChecker authTokenChecker = new AuthTokenChecker();
				String[] authResult = authTokenChecker.getMoreAuthInfo(getAuthToken);
				if (authResult[0].equals(AuthTokenChecker.AUTH_RESULT_SUCCESS)) {
					isAuthUser = true;
					userName = authResult[4];
					
					isLoginUser = true;
				}
			}
		}catch (Exception ex) {}
		
		
		if (!isAuthUser) {
			// -------------- ????????? ?????? --------------
			LoginChecker.AutoLoginTask(rl, session, request, response, false, false, null, isCreateTokenTOF);
			// ----------------------------------------
			
			//????????? ????????? ??????
			Random random = new Random();
			int randomInt = random.nextInt();
			//????????? ??????
			randomInt = Math.abs(randomInt);
			//????????? ????????? ??????
			session.setAttribute(ParameterManipulation.INTRandomKeySession, RhyaAES.AES_Encode(Integer.toString(randomInt)));
			
			
			// ?????? ????????? ??????
			String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
			if (login_session != null) {
				// ?????? ?????????
				String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
				// ?????? ?????????
				userName = auto_login_result[4];
				
				isLoginUser = true;
			}
		}
		
		
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		try {
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM announcement ORDER BY DATE DESC;");
			databaseConnection.setResultSet();
			
			int index = 0;
			while (databaseConnection.getResultSet().next()) {
				if (databaseConnection.getResultSet().getInt("is_show") == 0) {
					RhyaAnnouncementVO announcementVO = new RhyaAnnouncementVO();
					announcementVO.setUuid(databaseConnection.getResultSet().getString("uuid"));
					announcementVO.setTitle(databaseConnection.getResultSet().getString("title"));
					announcementVO.setMessage(databaseConnection.getResultSet().getString("message"));
					announcementVO.setDate(databaseConnection.getResultSet().getDate("date"));
					
					rhyaAnnouncementVOS.add(index, announcementVO);
					
					index++;
				}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			databaseConnection.allClose();
		}
	}catch (Exception ex) {
		ex.printStackTrace();
		
		// ?????? ??????
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request),"?????? ????????? ?????? : LOGIN_SESSION == null"));
		// ?????? ????????? ?????? --> ????????? ????????? ??????
		response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0));	
	}
	%>


	<body>
		<!-- Navigation -->
	    <nav id="navbar" class="navbar navbar-expand-lg fixed-top navbar-dark" aria-label="Main navigation">
	        <div class="container">
	            <!-- Text Logo - Use this if you don't have a graphic logo -->
	            <a class="navbar-brand logo-text" href="<%=JspPageInfo.GetJspPageURL(request, 12)%>">RHYA.Network</a>
	
	            <button class="navbar-toggler p-0 border-0" type="button" id="navbarSideCollapse" aria-label="Toggle navigation">
	                <span class="navbar-toggler-icon"></span>
	            </button>
	
	            <div class="navbar-collapse offcanvas-collapse" id="navbarsExampleDefault">
	                <ul class="navbar-nav ms-auto navbar-nav-scroll">
	                	<li class="nav-item">
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 12)%>">Home</a>
	                    </li>
	                	<li class="nav-item">
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 21)%>">????????????</a>
	                    </li>
	                    <li class="nav-item">
	                        <a class="nav-link" href="https://github.com/fkdldkRhya">????????? GitHub</a>
	                    </li>
						<%
	                    if (isLoginUser) {
	                    %>
	                    <li class="nav-item dropdown">
	                        <a class="nav-link dropdown-toggle" id="dropdown01" data-bs-toggle="dropdown" aria-expanded="false"><%=userName%></a>
	                        <ul class="dropdown-menu" aria-labelledby="dropdown01">
	                            <li><a class="dropdown-item" href="<%=JspPageInfo.GetJspPageURL(request, 16)%>">?????? ??????</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="dropdown-item" href="<%=JspPageInfo.GetJspPageURL(request, 11)%>">????????????</a></li>
	                        </ul>
	                    </li>
	                    <%}else {%>
	                    <li class="nav-item">
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 0)%>">?????????</a>
	                    </li>
	                    <%}%>
	                </ul>
	            </div> <!-- end of navbar-collapse -->
	        </div> <!-- end of container -->
	    </nav> <!-- end of navbar -->
	    <!-- end of navigation -->
		
		
	    <!-- Header -->
	    <header class="ex-header">
	        <div class="container">
	            <div class="row">
	                <div class="col-xl-10 offset-xl-1">
	                    <h1>Announcement</h1>
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </header> <!-- end of ex-header -->
	    <!-- end of header -->


	    <!-- Basic -->
	    <div class="ex-basic-1 pt-5 pb-5">
	        <div class="container">
	            <div class="row">
	                <div class="col-xl-10 offset-xl-1">
						<%
							for (int i = 0; i < rhyaAnnouncementVOS.size(); i ++) {
								RhyaAnnouncementVO rhyaAnnouncementVO = rhyaAnnouncementVOS.get(i);
								out.println("<h2 class=\"mt-5 mb-3\">#INDEX#. <strong>#DATE#</strong> #TITLE#</h2>"
										.replace("#INDEX#", String.valueOf(i + 1))
										.replace("#DATE#", rhyaAnnouncementVO.getDate().toString())
										.replace("#TITLE#", rhyaAnnouncementVO.getTitle()));
								out.println(rhyaAnnouncementVO.getMessage());
							}
						%>
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </div> <!-- end of ex-basic-1 -->
	    <!-- end of basic -->
	
	
		<!-- Footer -->
		<script type="text/javascript">
			var homeURL = '<%=JspPageInfo.GetJspPageURL(request, 12)%>';
			function moveHome() {
				location.href = homeURL;
			}
		</script>
       	<%
   		DatabaseManager.DatabaseConnection databaseConnection2 = new DatabaseManager.DatabaseConnection();
   		try {
   			databaseConnection2.init();
   			databaseConnection2.connection();
   			databaseConnection2.setPreparedStatement("SELECT rhya_network_bottom_other,rhya_network_licenses FROM rhya_network_web_html;");
   			databaseConnection2.setResultSet();
   			
   			if (databaseConnection2.getResultSet().next()) {
   				out.println(databaseConnection2.getResultSet().getString("rhya_network_bottom_other"));
   				out.println(databaseConnection2.getResultSet().getString("rhya_network_licenses"));
   			}
   		}catch (Exception ex) {
   			ex.printStackTrace();
   		}finally {
   			databaseConnection2.allClose();
   		}
       	%>
	
	    
	    <!-- Back To Top Button -->
	    <button onclick="topFunction()" id="myBtn">
	        <img src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/up-arrow.png" alt="alternative">
	    </button>
	    <!-- end of back to top button -->
	    
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/bootstrap.min.js"></script><!-- Bootstrap framework -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/purecounter.min.js"></script> <!-- Purecounter counter for statistics numbers -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/swiper.min.js"></script><!-- Swiper for image and text sliders -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/aos.js"></script><!-- AOS on Animation Scroll -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/script.js"></script>  <!-- Custom scripts -->
	</body>
</html>