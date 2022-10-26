<%@page import="java.net.URLEncoder"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Random"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>
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
<%@ page import="kro.kr.rhya_network.admintool.ServerMainImageManager"%>
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Home</title>
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
	<script type="text/javascript" src="https://kit.fontawesome.com/f1def33959.js" crossorigin="anonymous"></script>

	
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
		cont = null;
		sql = null;
		// 페이지 이동
		response.sendRedirect(JspPageInfo.ERROR_PAGE_PATH_HTTP_500);
		
		return;
	}catch (ClassNotFoundException ex2) {
		// 데이터베이스 접속 오류 처리
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
		// 연결 종료
		cont = null;
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
		stat.setInt(1, JspPageInfo.PageID_Rhya_Network_Main);
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
		sql = null;
		// 상태 확인 - 결과 처리
		if (!JspPageInfo.JspPageStateManager(state)) {
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
			return;
		}
	}

	// HTML 설정 변수
	boolean isLoginUser = false;
	String userName = null;
	String up_version = null;
	String up_version_for_windows = null;
	String oa_version = null;
	String todayRandomImageURL1 = String.format("%s?todayrandomimage=%d", JspPageInfo.GetJspPageURL(request, 23), 1);
	String todayRandomImageURL2 = String.format("%s?todayrandomimage=%d", JspPageInfo.GetJspPageURL(request, 23), 2);
	String todayRandomImageURL3 = String.format("%s?todayrandomimage=%d", JspPageInfo.GetJspPageURL(request, 23), 3);
	int userPermission = 0;
	int mainImageSetting = 0;
	
	try {
		// -------------- 로그인 확인 --------------
		LoginChecker.AutoLoginTask(rl, session, request, response, false, false, null, isCreateTokenTOF);
		// -------------------------------------
		
		// 메인 이미지 상태 확인
		ServerMainImageManager serverMainImageManager = new ServerMainImageManager();
		mainImageSetting = serverMainImageManager.getServerMainSate();
		
		//정수형 랜덤키 생성
		Random random = new Random();
		int randomInt = random.nextInt();
		//양수로 변경
		randomInt = Math.abs(randomInt);
		//정수형 랜덤키 설정
		session.setAttribute(ParameterManipulation.INTRandomKeySession, RhyaAES.AES_Encode(Integer.toString(randomInt)));
		
		
		// 자동 로그인 확인
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		if (login_session != null) {
			// 자동 로그인
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			// 출력 데이터
			userName = auto_login_result[4];
			
			isLoginUser = true;
			userPermission = Integer.parseInt(auto_login_result[9]);	
		}
		
		
		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
		try {
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM utaite_info WHERE main_key = 1;");
			databaseConnection.setResultSet();
			if (databaseConnection.getResultSet().next()) {
				up_version = databaseConnection.getResultSet().getString("version");
				up_version_for_windows = databaseConnection.getResultSet().getString("version_for_windows");
			}
			
			databaseConnection.closeResultSet();
			databaseConnection.closePreparedStatement();
			
			databaseConnection.setPreparedStatement("SELECT * FROM online_attendance_app;");
			databaseConnection.setResultSet();
			
			if (databaseConnection.getResultSet().next()) {
				oa_version = databaseConnection.getResultSet().getString("version");
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			databaseConnection.allClose();
		}
	}catch (Exception ex) {
		ex.printStackTrace();
		
		// 로그 작성
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request),"자동 로그인 실패 : LOGIN_SESSION == null"));
		// 자동 로그인 실패 --> 로그인 페이지 이동
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
	                	<%
	                    if (userPermission >= 1) {
                        %>
                        <li class="nav-item dropdown">
	                        <a class="nav-link dropdown-toggle" id="dropdown01" data-bs-toggle="dropdown" aria-expanded="false">관리자 메뉴</a>
	                        <ul class="dropdown-menu" aria-labelledby="dropdown01">
	                        	<%
	                        	// 관리자 메뉴 내용 생성
	                        	StringBuilder task_builder = new StringBuilder();
	                        	
	                        	// LEVEL 1 권한 관리자 메뉴 내용
	                        	task_builder.append("<li readonly><span readonly class=\"dropdown-item\"><strong>LEVEL 1 작업</strong></span></li>");
	                        	task_builder.append(System.lineSeparator());
	                        	task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">서버 이미지 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager)));
	                        	task_builder.append(System.lineSeparator());
	                        	task_builder.append("<li><div class=\"dropdown-divider\"></div></li>");
                        		final String LEVEL_1_ADMIN_TASK = task_builder.toString();
                        		// LEVEL 2 권한 관리자 메뉴 내용
                        		task_builder.setLength(0);
	                        	task_builder.append("<li readonly><span readonly class=\"dropdown-item\"><strong>LEVEL 2 작업</strong></span></li>");
	                        	task_builder.append(System.lineSeparator());
	                        	task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">공지사항 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)));
	                        	task_builder.append(System.lineSeparator());
	                        	task_builder.append("<li><div class=\"dropdown-divider\"></div></li>");
                        		final String LEVEL_2_ADMIN_TASK = task_builder.toString();
                        		// LEVEL 3 권한 관리자 메뉴 내용
                        		task_builder.setLength(0);
	                        	task_builder.append("<li readonly><span readonly class=\"dropdown-item\"><strong>LEVEL 3 작업</strong></span></li>");
	                        	task_builder.append(System.lineSeparator());
	                        	// task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">우타이테 플레이어 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_Admin)));
                        		// task_builder.append(System.lineSeparator());
                        		// task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">온라인 출석부 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_Admin)));
                        		// task_builder.append(System.lineSeparator());
                        		// task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">서울북부지원교육청 알리미 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_Admin)));
                        		// task_builder.append(System.lineSeparator());
	                        	task_builder.append("<li><div class=\"dropdown-divider\"></div></li>");
                        		final String LEVEL_3_ADMIN_TASK = task_builder.toString();
                        		// LEVEL 4 권한 관리자 메뉴 내용
                        		task_builder.setLength(0);
                        		task_builder.append("<li readonly><span readonly class=\"dropdown-item\"><strong>LEVEL 4 작업</strong></span></li>");
	                        	task_builder.append(System.lineSeparator());
	                        	// task_builder.append(String.format("<li><a class=\"dropdown-item\" href=\"%s\" target=\"_blank\">서버 상태 관리자</a></li>", JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager_Admin)));
                        		// task_builder.append(System.lineSeparator());
                        		final String LEVEL_4_ADMIN_TASK = task_builder.toString();
                        		
                        		// 권한 비교
                        		switch (userPermission) {
                        		// LEVEL 1 권한 관리자 메뉴
                        		case 1: {
                        			out.println(LEVEL_1_ADMIN_TASK);
                        			break;
                        		}
                        		// LEVEL 2 권한 관리자 메뉴
                        		case 2: {
                        			out.println(LEVEL_1_ADMIN_TASK);
                        			out.println(LEVEL_2_ADMIN_TASK);
                        			break;
                        		}
                        		// LEVEL 3 권한 관리자 메뉴
                        		case 3: {
                        			out.println(LEVEL_1_ADMIN_TASK);
                        			out.println(LEVEL_2_ADMIN_TASK);
                        			out.println(LEVEL_3_ADMIN_TASK);
                        			break;
                        		}
                        		// LEVEL 4 권한 관리자 메뉴
                        		case 4: {
                        			out.println(LEVEL_1_ADMIN_TASK);
                        			out.println(LEVEL_2_ADMIN_TASK);
                        			out.println(LEVEL_3_ADMIN_TASK);
                        			out.println(LEVEL_4_ADMIN_TASK);
                        			break;
                        		}
                        		}
	                        	%>
	                        </ul>
	                    </li>
                        <%
                        }
                        %>
	                
	                	<li class="nav-item">
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 21)%>">공지사항</a>
	                    </li>
	                    
	                    <li class="nav-item dropdown">
	                        <a class="nav-link dropdown-toggle" id="dropdown01" data-bs-toggle="dropdown" aria-expanded="false">서비스</a>
	                        <ul class="dropdown-menu" aria-labelledby="dropdown01">
	                            <li><a class="nav-link" href="#online_attendance">온라인 출석부</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="nav-link" href="#utaite_player">우타이테 플레이어</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="nav-link" href="#bbedu_alert" style="width:255px">서울북부지원교육청 알리미</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="nav-link" href="https://rhya-network.kro.kr/RhyaNetwork/other_service_download?package=2">OpenVPN 클라이언트</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="nav-link" href="https://rhya-network.kro.kr/RhyaNetwork/other_service_download?package=3">WanaCrypt0r 랜섬웨어</a></li>
	                        </ul>
	                    </li>
	                    
	                    <li class="nav-item">
	                        <a class="nav-link" href="https://github.com/fkdldkRhya">개발자 정보</a>
	                    </li>
	                    
	                    <%
	                    if (isLoginUser) {
	                    %>
	                    <li class="nav-item dropdown">
	                        <a class="nav-link dropdown-toggle" id="dropdown01" data-bs-toggle="dropdown" aria-expanded="false"><%=userName%></a>
	                        <ul class="dropdown-menu" aria-labelledby="dropdown01">
	                            <li><a class="dropdown-item" href="<%=JspPageInfo.GetJspPageURL(request, 16)%>">계정 설정</a></li>
	                            <li><div class="dropdown-divider"></div></li>
	                            <li><a class="dropdown-item" href="<%=JspPageInfo.GetJspPageURL(request, 11)%>">로그아웃</a></li>
	                        </ul>
	                    </li>
	                    <%}else {%>
	                    <li class="nav-item">
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 0)%>">로그인</a>
	                    </li>
	                    <%}%>
	                </ul>
	            </div> <!-- end of navbar-collapse -->
	        </div> <!-- end of container -->
	    </nav> <!-- end of navbar -->
	    <!-- end of navigation -->
	
	
	    <!-- Home -->
	    <!-- Background Random Image API: style="background-image: url(<%=JspPageInfo.GetJspPageURL(request, 23)%>?type=2);" -->
	    <!-- No uploaded only: <a class="btn" href="#" onclick="updateMsgBox()">Download App</a> -->
	    <section style="background-image: url(<%=mainImageSetting == 0 ? String.format("%s/webpage/resources/assets/main/assets/images/background.jpg", request.getContextPath()) : String.format("%s?type=2", JspPageInfo.GetJspPageURL(request, 23))%>);" class="home py-5 d-flex align-items-center" id="header">
	        <div class="container text-light py-5"  data-aos="fade-right"> 
	            <h1 class="headline"><span class="home_text">RHYA.Network</span><br>Welcome to rhya server</h1>
	            
	            <p class="para para-light py-3"></p>
	            
	            <div class="d-flex align-items-center">
	                <p class="p-2"><i class="fas fa-school fa-lg"></i></p>
	                <p>Online Attendance 온라인 출석부</p>  
	            </div>
	            <div class="d-flex align-items-center">
	                <p class="p-2"><i class="fas fa-music fa-lg"></i></p>
	                <p>Utaite Player 우타이테 플레이어</p>  
	            </div>
	            <div class="d-flex align-items-center">
	                <p class="p-2"><i class="fas fa-bullhorn fa-lg"></i></p>
	                <p>서울북부지원교육청 알리미</p>  
	            </div>
	        </div> <!-- end of container -->
	    </section> <!-- end of home -->

	
	    <section class="about d-flex align-items-center text-light py-5" id="online_attendance">
	        <div class="container" >
	            <div class="row d-flex align-items-center">
	                <div class="col-lg-7" data-aos="fade-right">
	                    <p>Online Attendance</p>
	                    <h1><strong>온라인 출석부</strong></h1>
	                    <p class="py-2 para-light">해당 서비스는 기존 출석부를 온라인에서 사용할 수 있게 만든 서비스입니다. 이 온라인 출석부를 이용하여 기존 출석부보다 편리하게 사용할 수 있습니다.</p>
	                    <p class="py-2 para-light">Android만 실행할 수 있습니다. RHYA.Network 통합 계정 인증 Auth.V1을 사용하여 사용자의 계정이 안전하게 보호됩니다.</p>
	                    <br>
	                    <p class="py-2 para-light">Version <%=oa_version%></p>
	                    <div class="my-3">
	                        <a class="btn" href="https://rhya-network.kro.kr/RhyaNetwork/online_attendance_manager?mode=13">Download APK</a>
	                    </div>
	                </div>
	                <div class="col-lg-5 text-center py-4 py-sm-0" data-aos="fade-down"> 
	                    <img class="img-fluid" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/online_attendance.png" alt="about" >
	                </div>
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </section> <!-- end of about -->
	    
	    
	    <section class="about d-flex align-items-center text-light py-4" id="utaite_player">
	        <div class="container" >
	            <div class="row d-flex align-items-center">
	                <div class="col-lg-7" data-aos="fade-right">
	                    <p>Utaite Player</p>
	                    <h1><strong>우타이테 플레이어</strong></h1>
	                    <p class="py-2 para-light">해당 서비스는 멜론처럼 우타이테(일본 노래)를 들을 수 있게 만든 서비스입니다. 이 서비스는 아직 개발이 진행 중인 베타 버전 앱입니다.</p>
	                    <p class="py-2 para-light">Android, Windows 크로스 플랫폼을 지원합니다. RHYA.Network 통합 계정 인증 Auth.V1을 사용하여 사용자의 계정이 안전하게 보호됩니다.</p>
	                    <p class="py-2 para-light"><strong>License</strong></p>
	                    <p class="py-2 para-light">Website : <a href="https://twitter.com/Runamonet/status/1259002648184807424">Twitter Runamonet Arknight Texas</a></p>
	                    <p class="py-2 para-light"><strong>Related features</strong></p>
	                    <div class="d-flex align-items-center py-2">
                            <i class="fas fa-caret-right"></i>
                            <a href="<%=JspPageInfo.GetJspPageURL(request, 32)%>"><p class="ms-3">우타이테 플레이어 노래 신청하러 가기</p></a>
	                    </div>
	                    <div class="d-flex align-items-center py-2">
                            <i class="fas fa-caret-right"></i>
                            <a href="<%=JspPageInfo.GetJspPageURL(request, 36)%>"><p class="ms-3">우타이테 플레이어 이용권 신청하러 가기</p></a>
	                    </div>
	                    <div class="d-flex align-items-center py-2">
                            <i class="fas fa-caret-right"></i>
                            <a href="<%=JspPageInfo.GetJspPageURL(request, 31)%>"><p class="ms-3">우타이테 플레이어 오픈 소스 라이선스</p></a>
	                    </div>
	                    <br>
	                    <p class="py-2 para-light">Version(APK) <%=up_version%> / Version(EXE) <%=up_version_for_windows%></p>
	                    <div class="my-5">
	                        <a class="btn" href="https://rhya-network.kro.kr/RhyaNetwork/utaite_player_manager?mode=7">Download APK</a>
	                        <a class="btn" style="margin-left: 20px;" href="https://rhya-network.kro.kr/RhyaNetwork/utaite_player_manager?mode=17">Download EXE</a>
	                    </div>
	                </div>
	                <div class="col-lg-5 text-center py-4 py-sm-0" data-aos="fade-down"> 
	                    <img class="img-fluid" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/utaite_player.png" alt="about" >
	                </div>
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </section> <!-- end of about -->
	    
	    
		<section class="about d-flex align-items-center text-light py-4" id="bbedu_alert">
	        <div class="container" >
	            <div class="row d-flex align-items-center">
	                <div class="col-lg-7" data-aos="fade-right">
	                    <p>Seoul Bukbu District Office of Education Alerts</p>
	                    <h1><strong>서울북부지원교육청 알리미</strong></h1>
	                    <p class="py-2 para-light">해당 서비스는 학원, 교습소를 대상으로 만들어진 앱입니다. 해당 앱을 사용하여 공지를 보내거나 볼 수 있고 각 학원에서 메시지를 작성해 전송할 수 있습니다.</p>
	                    <p class="py-2 para-light">Android: 일반 사용자 전용, Firebase Google 로그인을 사용하여 간편하고 안전하게 사용할 수 있습니다.</p>
	                    <p class="py-2 para-light">Windows: 관리자 전용, 관리자 전용 도구를 사용하시려면 전용 인증 키가 필요합니다.</p>
	                    <p class="py-2 para-light"><strong>License</strong></p>
	                    <p class="py-2 para-light">Website : <a href="https://www.flaticon.com/kr/free-icon/chalkboard_5886118">Flaticon Icongeek26 칠판 무료 아이콘</a></p>
	                    <br>
	                    <div class="my-5">
	                        <a class="btn" href="https://rhya-network.kro.kr/RhyaNetwork/other_service_download?package=0">Download APK</a>
	                        <a class="btn" style="margin-left: 20px;" href="https://rhya-network.kro.kr/RhyaNetwork/other_service_download?package=1">Download MSI</a>
	                    </div>
	                </div>
	                <div class="col-lg-5 text-center py-4 py-sm-0" data-aos="fade-down"> 
	                    <img class="img-fluid" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/bbedu_alert.png" alt="about" >
	                </div>
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </section> <!-- end of about -->
	
	
		<!-- Plans -->
	    <section class="plans d-flex align-items-center py-5" id="plans">
	        <div class="container text-light" >
	            <div class="text-center pb-4">
	                <p>RANDOM IMAGE</p>
	                <h2 class="py-2">Today random image</h2>
	                <p class="para-light">매일 매일 새로운 일러스트/이미지를 만나보세요. PIXIV Daily Top 50 이미지도 만나보실 수 있습니다. 이전 이미지들은 1개월 동안 다시 나오지 않습니다. 또한 해당 이미지는 매일 오전 12:00분에 갱신됩니다.</p>
	            </div>
	            <div class="row gy-4" data-aos="zoom-in">
	                <div class="col-lg-4">
	                    <div class="card bg-transparent px-4">
	                        <h4 class="py-2">Today random image 1</h4>
	                        <p class="py-3">오늘의 1번째 랜덤 이미지를 확인해보세요.</p>
	                        
	                       	<img 
	                        	src="<%=todayRandomImageURL1%>"
	                        	style="object-fit: cover; max-width: 300; max-height: 300;"
	                            loading="lazy"
	                            alt="Anime Image"
	                            width="100%"
	                            height="300"/>
	                            
	                        <div class="my-3">
	                            <a class="btn" onClick="showRandomImageForMsgBox('<%=todayRandomImageURL1%>');">VIEW MORE</a>
	                        </div>
	                    </div>  
	                </div>
	
   	                <div class="col-lg-4">
	                    <div class="card bg-transparent px-4">
	                        <h4 class="py-2">Today random image 2</h4>
	                        <p class="py-3">오늘의 2번째 랜덤 이미지를 확인해보세요.</p>
	                        
	                        <img 
	                        	src="<%=todayRandomImageURL2%>"
	                        	style="object-fit: cover; max-width: 300; max-height: 300;"
	                            loading="lazy"
	                            alt="Anime Image"
	                            width="100%"
	                            height="300"/>
	                            
	                        <div class="my-3">
	                            <a class="btn" onClick="showRandomImageForMsgBox('<%=todayRandomImageURL2%>');">VIEW MORE</a>
	                        </div>
	                    </div>  
	                </div>
	
	                <div class="col-lg-4">
	                    <div class="card bg-transparent px-4">
	                        <h4 class="py-2">Today random image 3</h4>
	                        <p class="py-3">오늘의 3번째 랜덤 이미지를 확인해보세요.</p>
	                        
	                        <img 
	                        	src="<%=todayRandomImageURL3%>"
	                        	style="object-fit: cover; max-width: 300; max-height: 300;"
	                            loading="lazy"
	                            alt="Anime Image"
	                            width="100%"
	                            height="300"/>

	                        <div class="my-3">
	                            <a class="btn" onClick="showRandomImageForMsgBox('<%=todayRandomImageURL3%>');">VIEW MORE</a>
	                        </div>
	                    </div>  
	                </div>
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </section> <!-- end of plans -->
	    
	
		<!-- MEMBER -->
	    <div class="slider-1 testimonial text-light d-flex align-items-center">
	        <div class="container">
	            <div class="row">
	                <div class="text-center w-lg-75 m-auto pb-4">
	                    <p>MEMBER</p>
	                    <h2 class="py-2">RHYA.Network Development Team</h2>
	                    <p class="para-light">라이아 네트워크 서비스 개발팀 팀원을 소개합니다.</p>
	                </div>
	            </div> <!-- end of row -->
	            <div class="row p-2" data-aos="zoom-in">
	                <div class="col-lg-12">
	
	                    <!-- Card Slider -->
	                    <div class="slider-container">
	                        <div class="swiper-container card-slider">
	                            <div class="swiper-wrapper">
	                                
	                                <!-- Slide -->
	                                <div class="swiper-slide">
	                                    <div class="testimonial-card p-4">
	                                        <p>마른 하늘에 고양이 발바닥</p>
	                                        <br>
	                                        <p>sihun.choi@email.rhya-network.kro.kr</p>
	                                        <div class="d-flex pt-4">
	                                            <div>
	                                                <img class="avatar" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/choi_si_hun.jpg" alt="RHYA">
	                                            </div>
	                                            <div class="ms-3 pt-2">
	                                                <h6>CHOI SI-HUN</h6>
	                                                <p>Project Manager</p>
	                                            </div>
	                                        </div>
	                                    </div>
	                                </div> <!-- end of swiper-slide -->
	                                <!-- end of slide -->
	        
	        	                    <!-- Slide -->
	                                <div class="swiper-slide">
	                                    <div class="testimonial-card p-4">
	                                        <p>마른 하늘에 고양이 발바닥</p>
	                                        <br>
	                                        <p>sihun.choi@email.rhya-network.kro.kr</p>
	                                        <div class="d-flex pt-4">
	                                            <div>
	                                                <img class="avatar" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/choi_si_hun.jpg" alt="RHYA">
	                                            </div>
	                                            <div class="ms-3 pt-2">
	                                                <h6>CHOI SI-HUN</h6>
	                                                <p>Developer</p>
	                                            </div>
	                                        </div>
	                                    </div>
	                                </div> <!-- end of swiper-slide -->
	                                <!-- end of slide -->
	                                
	                                <!-- Slide -->
	                                <div class="swiper-slide">
	                                    <div class="testimonial-card p-4">
	                                        <p>마른 하늘에 고양이 발바닥</p>
	                                        <br>
	                                        <p>sihun.choi@email.rhya-network.kro.kr</p>
	                                        <div class="d-flex pt-4">
	                                            <div>
	                                                <img class="avatar" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/choi_si_hun.jpg" alt="RHYA">
	                                            </div>
	                                            <div class="ms-3 pt-2">
	                                                <h6>CHOI SI-HUN</h6>
	                                                <p>Designer</p>
	                                            </div>
	                                        </div>
	                                    </div>
	                                </div> <!-- end of swiper-slide -->
	                                <!-- end of slide -->
	        
	                                <!-- Slide -->
	                                <div class="swiper-slide">
 										<div class="testimonial-card p-4">
	                                        <p>즐겁게 함게 노동할 팀원을 모집합니다.</p>
	                                        <br>
	                                        <p>Welcome to RHYA.Network!</p>
	                                        <br>
	                                        <p>신청 및 상담 : sihun.choi@email.rhya-network.kro.kr</p>
	                                        <p></p>
	                                        <br>
	                                    </div>  
	                                </div> <!-- end of swiper-slide -->
	                                <!-- end of slide -->
	
	                            </div> <!-- end of swiper-wrapper -->
	        
	                            <!-- Add Arrows -->
	                            <div class="swiper-button-next"></div>
	                            <div class="swiper-button-prev"></div>
	                            <!-- end of add arrows -->
	                        </div> <!-- end of swiper-container -->
	                    </div> <!-- end of slider-container -->
	                    <!-- end of card slider -->
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </div> <!-- end of testimonials -->
	
	
		<!-- Footer -->
	    <section class="footer text-light">
	        <div class="container">
	            <div class="row" data-aos="fade-right">
	                <div class="col-lg-3 py-4 py-md-5">
	                    <div class="d-flex align-items-center">
	                        <h4 class="">Get In Touch</h4>
	                    </div>
	                    
	                    <div class="p-2"><i class="far fa-envelope fa-3x"></i></div>
	                    <div class="ms-2">
	                        <h6>SEND US MESSAGE</h6>
	                        <p>	sihun.choi@email.rhya-network.kro.kr</p>
	                    </div>
	                    
	                    <br>
	                    
	                    <div class="p-2"><i class="fas fa-code fa-3x"></i></div>
	                    <div class="ms-2">
	                        <h6>Developer</h6>
	                        <p><a href="https://github.com/fkdldkRhya"> CHOI SI-HUN GitHub</a></p>
	                    </div>
	                </div> <!-- end of col -->
					
	                <div class="col-lg-3 py-4 py-md-5">
	                    <div>
	                        <h4 class="py-2">Quick Links</h4>
	                        <div class="d-flex align-items-center py-2">
	                            <i class="fas fa-caret-right"></i>
	                            <a href="<%=JspPageInfo.GetJspPageURL(request, 21)%>"><p class="ms-3">공지사항</p></a>
	                        </div>
	                        <div class="d-flex align-items-center py-2">
	                            <i class="fas fa-caret-right"></i>
	                            <a href="https://github.com/fkdldkRhya"><p class="ms-3">개발자 GitHub</p></a>
	                        </div>
	                    </div>
	                </div> <!-- end of col -->
	
	                <div class="col-lg-3 py-4 py-md-5">
	                    <div>
	                        <h4 class="py-2">Useful Links</h4>
	                        <div class="d-flex align-items-center py-2">
	                            <i class="fas fa-caret-right"></i>
	                            <a href="<%=request.getContextPath()%>/webpage/jsp/main/rhya_network_pp.jsp" target="_blank"><p class="ms-3">Privacy & terms</p></a>
	                        </div>
	                    </div>
	                </div> <!-- end of col -->
	
	                <div class="col-lg-3 py-4 py-md-5">
	                    <div class="d-flex align-items-center">
	                        <h4>Bug report</h4>
	                    </div>
	                    <p class="py-3 para-light">버그 또는 계정 관련 문제 발생 시 위의 이메일 주소를 통해 개발자에게 연락해주세요.</p>
	                    
	                    <div class="ms-2">
							<img class="img-fluid" src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/RNLogo.png" alt="about" >
	                    </div>
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
    	</section> <!-- end of footer -->
	
       	<%
   		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
   		try {
   			databaseConnection.init();
   			databaseConnection.connection();
   			databaseConnection.setPreparedStatement("SELECT rhya_network_licenses FROM rhya_network_web_html;");
   			databaseConnection.setResultSet();
   			
   			if (databaseConnection.getResultSet().next()) {
   				out.println(databaseConnection.getResultSet().getString("rhya_network_licenses"));
   			}
   		}catch (Exception ex) {
   			ex.printStackTrace();
   		}finally {
   			databaseConnection.allClose();
   		}
       	%>
	
	    
	    <!-- Back To Top Button -->
	    <button onclick="topFunction()" id="myBtn">
	        <img src="<%=request.getContextPath()%>/webpage/resources/assets/main/assets/images/up-arrow.png" alt="alternative">
	    </button>
	    <!-- end of back to top button -->
	
	    
	    <!-- Scripts -->
	    <script type="text/javascript">
	    	function updateMsgBox() {
		    	Swal.fire({
	    		    title: "서비스 준비 중!",
	    		    text: "서비스 준비 중입니다. 앞으로 조금만 더 기다려주세요.",
	    		    icon: "info"
	    		});
			}
	    	
	    	function showRandomImageForMsgBox(url) {
	    		Swal.fire({
	    			  imageUrl: url,
	    			  imageAlt: 'Anime Image',
    			});
	    	}
	    </script>
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/bootstrap.min.js"></script><!-- Bootstrap framework -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/purecounter.min.js"></script> <!-- Purecounter counter for statistics numbers -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/swiper.min.js"></script><!-- Swiper for image and text sliders -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/aos.js"></script><!-- AOS on Animation Scroll -->
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/script.js"></script>  <!-- Custom scripts -->
		<script type="text/javascript" src='<%=request.getContextPath()%>/webpage/resources/assets/main/js/pageTransitionAnim.js'></script>
	</body>
</html>