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
<%@ page import="kro.kr.rhya_network.security.IPBlockChecker"%>
<%@ page import="kro.kr.rhya_network.utaite_player.UtaitePlayerTicketManager"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
   pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Utaite Player Application Licenses</title>
		<meta charset="EUC-KR" />
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="apple-touch-icon" sizes="180x180" href="<%=request.getContextPath()%>/webpage/resources/icon/apple_touch_logo_icon.png">
		<link rel="icon" type="image/png" sizes="32x32" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_32x32.png">
		<link rel="icon" type="image/png" sizes="16x16" href="<%=request.getContextPath()%>/webpage/resources/icon/logo_16x16.png">
		<link rel="manifest" href="<%=request.getContextPath()%>/webpage/resources/icon/site.webmanifest">
		<link rel="mask-icon" href="<%=request.getContextPath()%>/webpage/resources/icon/server_logo.svg" color="#5bbad5">
		<meta name="msapplication-TileColor" content="#da532c">
		<meta name="theme-color" content="#ffffff">
		<link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/bootstrap.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/fontawesome-all.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/aos.min.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/swiper.css" rel="stylesheet">
	    <link href="<%=request.getContextPath()%>/webpage/resources/assets/main/css/style.css" rel="stylesheet">
	</head>
	
	
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
		stat.setInt(1, JspPageInfo.PageID_Utaite_Player_Licenses_Application);
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

	
	boolean isLoginUser = false;
	String userName = null;
	String userID = null;
	String ticketDate = null;
	String ticketState = null;

	try {
		// -------------- 로그인 확인 --------------
		LoginChecker.AutoLoginTask(rl, session, request, response, false, false, null, isCreateTokenTOF);
		// ----------------------------------------
		
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
			userID = auto_login_result[3];
			
			isLoginUser= true;
			
			UtaitePlayerTicketManager utaitePlayerTicketManager = new UtaitePlayerTicketManager();
			
			// 이용권 불러오기
			if (utaitePlayerTicketManager.isAccessCheck(auto_login_result[1])) {
				ticketState = "이용권 승인 허용";
			}else {
				// 이용권 상태 불러오기
				ticketDate = utaitePlayerTicketManager.ticketApplicationState(auto_login_result[1]);
				if (ticketDate == null) {
					ticketState = "이용권 신청 거부";
				}else {
					ticketState = "이용권 승인 대기 중";
				}	
			}
		}else {
			// 페이지 이동
			response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0).concat("?rpid=36"));
		}
	}catch (Exception ex) {
		ex.printStackTrace();
		
		// 로그 작성
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request),"자동 로그인 실패 : LOGIN_SESSION == null"));
	}
	%>


	<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
	<script>
		var urlValue = '<%=JspPageInfo.GetJspPageURL(request, 37)%>';
		
		eval(function(p,a,c,k,e,r){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)r[e(c)]=k[c]||e(c);k=[function(e){return r[e]}];e=function(){return'\\w+'};c=1};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p}('5 h(){0.1({4:\'i j!\',6:\'처리 중...\',k:l,m:()=>{0.n()}});$.o({p:q,r:"s",b:5(a){0.c();7=t.u(a);v(7.w==\'x\'){0.1({4:"이용권 신청 성공!",6:"이용권 신청이 성공적으로 완료되었습니다. 신청 수락은 최대 2~3일이 소요될 수 있습니다.",8:"b"}).d((a)=>{e.f.g()})}y{0.1({4:"이용권 신청 실패!",6:7.z,8:"9"}).d((a)=>{e.f.g()})}},9:5(){0.c();0.1({4:"이용권 신청 실패!",A:"B 통신 실패! 다시 시도해 주세요.",8:"9"})}})}',38,38,'Swal|fire|||title|function|html|ajax_result_json|icon|error||success|close|then|window|location|reload|ticketApplicationFunc|Please|Wait|allowOutsideClick|false|didOpen|showLoading|ajax|url|urlValue|type|POST|JSON|parse|if|result|S|else|message|text|Ajax'.split('|'),0,{}))
	</script>


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
	                        <a class="nav-link" href="<%=JspPageInfo.GetJspPageURL(request, 21)%>">공지사항</a>
	                    </li>
	                    <li class="nav-item">
	                        <a class="nav-link" href="https://github.com/fkdldkRhya">개발자 GitHub</a>
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
		
		
	    <!-- Header -->
	    <header class="ex-header">
	        <div class="container">
	            <div class="row">
	                <div class="col-xl-10 offset-xl-1">
	                    <h1>Licenses Application</h1>
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </header> <!-- end of ex-header -->
	    <!-- end of header -->
	
	
	    <!-- Basic -->
	    <div class="ex-basic-1 pt-5 pb-5">
	        <div class="container">
	            <div class="row">
					<h2 class="mb-3"><strong>우타이테 플레이어 이용권 신청</strong></h2>
					<div class="text-box mt-5 mb-5 p-4">
						<p class="p-large">우타이테 플레이어를 사용하시려면 로그인하신 계정에 이용 권한이 부여되어있어야 합니다. 아래 상태를 통해 이용권 상태를 확인할 수 있습니다. 만약 이용권이 없다면 아래 신청 버튼을 눌러 주세요. 사유가 부적절할 경우에는 이용권 신청이 거절될 수 있습니다. 신청 소요 시간은 최대 2~3일이 소요될 수 있습니다.</p>
						<br>
						<p class="p-large"><strong>이용권 상태</strong></p>
						<p class="p-large">* 이용권 승인 허용 - 이용 권한 부여됨</p>
						<p class="p-large">* 이용권 신청 거부 - 이용권 신청을 하지 않았거나 신청이 거부됨</p>
						<p class="p-large">* 이용권 승인 대기 중 - 이용권 승인을 기다리는 중</p>
					</div> <!-- end of text-box -->
					
	            	<div style="width:100%; margin-top: 20px;">
	                	<div class="text-center text-lg-start py-4 pt-lg-0">
	                		<h3 class="mb-3"><strong>이용권 상태 확인</strong></h3>
	                        <p class="para-light">이용권 상태를 확인할 수 있습니다. 만약 거절되어있다면 아래 버튼을 눌러 신청해보세요.</p>
	                    </div>
	                    
                    	<div>
                    	    <div class="form-group py-1">
                            	<input type="text" class="form-control form-control-input" id="ticketState" value="<%=userID%>" readonly>
                                <p class="para-light">*신청 계정</p>
                            </div>  
                            <div class="form-group py-1">
                            	<input type="text" class="form-control form-control-input" id="ticketState" value="<%=ticketState%>" readonly>
                                <p class="para-light">*이용권 상태</p>
                            </div>  
                            <div class="form-group py-1">
                            	<input type="text" class="form-control form-control-input" id="ticketDate" value="<%=ticketDate%>" readonly>
                                <p class="para-light">*이용권 신청 날짜</p>
                            </div>  
                        </div>
                        <div class="my-3">
                            <a class="btn btn-tertiary" onClick="ticketApplicationFunc()">이용권 신청</a>
                        </div>
                    </div> <!-- end of div -->
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
	    
	    
	    <script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/jquery/jquery-3.2.1.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/purecounter.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/swiper.min.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/aos.js"></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/script.js"></script>
		<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
		<script src="https://kit.fontawesome.com/f1def33959.js" crossorigin="anonymous"></script>
	</body>
</html>