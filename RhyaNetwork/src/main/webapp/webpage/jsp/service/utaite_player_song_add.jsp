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
<%@ page import="kro.kr.rhya_network.util.AuthTokenChecker"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
   pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Utaite Player Song Add</title>
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
		stat.setInt(1, JspPageInfo.PageID_Utaite_Player_Song_Add_Manager);
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

	
	boolean isLoginUser = false;
	String userName = null;

	try {
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
		
		// ???????????? ??????
		String authToken = request.getParameter("auth");
		
		// ?????? ????????? ??????
		String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
		if (login_session != null) {
			// ?????? ?????????
			String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
			// ?????? ?????????
			userName = auto_login_result[4];	
			
			isLoginUser= true;
		}else {
			if (authToken != null) {
				AuthTokenChecker authTokenChecker = new AuthTokenChecker();
				// ?????? ?????????
				String[] auto_login_result = authTokenChecker.getMoreAuthInfo(authToken);
				// ?????? ????????? ??????
				if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
					// ?????? ?????????
					userName = auto_login_result[4];	
					
					isLoginUser= true;
				}else {
					// ????????? ??????
					response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0).concat("?rpid=32"));
				}
			}else {
				// ????????? ??????
				response.sendRedirect(JspPageInfo.GetJspPageURL(request, 0).concat("?rpid=32"));
			}
		}
	}catch (Exception ex) {
		ex.printStackTrace();
		
		// ?????? ??????
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request),"?????? ????????? ?????? : LOGIN_SESSION == null"));
	}
	%>
	
	<% PageParameter.SignIn signinV = new PageParameter.SignIn(); %>
	
	<script>
		var urlValue = '<%=JspPageInfo.GetJspPageURL(request, 33)%>';
		function addSongManagerFunc() {
			var songName = $('#songNameInput').val();
			var singerName = $('#singerNameInput').val();
			var songWriterName = $('#songWriterNameInput').val();
			var songTag = $('#songTagInput').val();
			var songImage = $('#songImageInput');
			var songMp3 = $('#songMp3Input')
			var singerLyrics = $('#songLyricsInput').val();
			
			// ?????? ??????
			if ((songName.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '?????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((singerName.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '???????????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((songWriterName.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '????????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((songTag.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '?????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if (!songImage.val()) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '?????? ????????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if (!songMp3.val()) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: 'MP3 ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((singerLyrics.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '?????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			
			
			// ?????? ??? ????????? ??????
			Swal.fire({
		        title: 'Please Wait!',
		        html: '?????? ???...',
		        allowOutsideClick: false,
		        didOpen: () => {
		        	Swal.showLoading()
		        }
		    });
			
			songName = encodeURI(songName, "UTF-8");
			singerName = encodeURI(singerName, "UTF-8");
			songWriterName = encodeURI(songWriterName, "UTF-8");
			songTag = encodeURI(songTag, "UTF-8");
			singerLyrics = encodeURI(singerLyrics, "UTF-8");

			var form = new FormData();
		    form.append("sname", songName);
		    form.append("aname", singerName);
		    form.append("swname", songWriterName);
		    form.append("stag", songTag);
		    form.append("simage", songImage[0].files[0]);
		    form.append("smp3", songMp3[0].files[0]);
		    form.append("slyrics", singerLyrics);
		    form.append("version", 0);
		    form.append("auth", "<%=request.getParameter("auth")%>");
		    
		    $.ajax({
		        url : urlValue
		       ,type : "POST"
		       ,processData : false
		       ,contentType : false
		       ,data : form
		       ,success:function(result) {
		    	   ajax_result_json = JSON.parse(result);
				   if (ajax_result_json.result == 'S') {
				       Swal.close();
				       Swal.fire({
			    	       title: "?????? ?????? ??????!",
			    		   html: "?????? ????????? ??????????????? ?????????????????????. ?????? ????????? ???????????? ?????? ??? ???????????????.",
			    		   icon: "success"
			           });
			           
			           $('#songNameInput').val('');
			           $('#singerNameInput').val('');
			           $('#songWriterNameInput').val('');
			           $('#songTagInput').val('');
			           $('#songImageInput').val('');
			           $('#songMp3Input').val('');
			           $('#songLyricsInput').val('');
				   }else {
				       Swal.close();
				       Swal.fire({
			    	       title: "?????? ?????? ??????!",
			    		   html: "?????? ???????????? ?????? ??? ????????? ?????????????????????. ?????? ????????? ????????????.",
			    		   icon: "error"
			           });
			       }
		       }
		       ,error: function () 
		       { 
					Swal.close();
			    	Swal.fire({
		    		    title: "?????? ?????? ??????!",
		    		    html: "?????? ???????????? ?????? ??? ????????? ?????????????????????. ?????? ????????? ????????????.",
		    		    icon: "error"
		    		});
		       }
		   });
		}





		function addSongManagerFuncForSep() {
			var songName = $('#songNameInput_for_sep').val();
			var singerName = $('#singerNameInput_for_sep').val();
			var youtubeUrl = $('#youtubeUrlInput_for_sep').val();

			
			// ?????? ??????
			if ((songName.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '?????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((singerName.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: '???????????? ????????? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			if ((youtubeUrl.length <= 0)) {
				// ?????? ????????? ??????
				Swal.fire({
					  position: 'top-end',
					  icon: 'error',
					  title: 'Youtube URL??? ????????? ?????????.',
					  showConfirmButton: false,
					  timer: 1500
				});
				return;
			}
			
			
			songName = encodeURI(songName, "UTF-8");
			singerName = encodeURI(singerName, "UTF-8");
			youtubeUrl = encodeURI(youtubeUrl, "UTF-8");

			var form = new FormData();
		    form.append("sname", songName);
		    form.append("aname", singerName);
		    form.append("surl", youtubeUrl);
		    form.append("version", 1);
		    form.append("auth", "<%=request.getParameter("auth")%>");
		    
		    $.ajax({
		        url : urlValue
		       ,type : "POST"
		       ,processData : false
		       ,contentType : false
		       ,data : form
		       ,success:function(result) {
		    	   ajax_result_json = JSON.parse(result);
				   if (ajax_result_json.result == 'S') {
				       Swal.close();
				       Swal.fire({
			    	       title: "?????? ?????? ??????!",
			    		   html: "?????? ????????? ??????????????? ?????????????????????. ?????? ????????? ???????????? ?????? ??? ???????????????.",
			    		   icon: "success"
			           });
			           
			           $('#songNameInput_for_sep').val('');
			           $('#singerNameInput_for_sep').val('');
			           $('#youtubeUrlInput_for_sep').val('');
				   }else {
				       Swal.close();
				       Swal.fire({
			    	       title: "?????? ?????? ??????!",
			    		   html: "?????? ???????????? ?????? ??? ????????? ?????????????????????. ?????? ????????? ????????????.",
			    		   icon: "error"
			           });
			       }
		       }
		       ,error: function () 
		       { 
					Swal.close();
			    	Swal.fire({
		    		    title: "?????? ?????? ??????!",
		    		    html: "?????? ???????????? ?????? ??? ????????? ?????????????????????. ?????? ????????? ????????????.",
		    		    icon: "error"
		    		});
		       }
		   });
		}	
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
	                    <h1>Song Add Manager</h1>
	                </div> <!-- end of col -->
	            </div> <!-- end of row -->
	        </div> <!-- end of container -->
	    </header> <!-- end of ex-header -->
	    <!-- end of header -->
	
	
	    <!-- Basic -->
	    <div class="ex-basic-1 pt-5 pb-5">
	        <div class="container">
	            <div class="row">
					<h2 class="mb-3"><strong>???????????? ???????????? ?????? ?????? ??????</strong></h2>
					<div class="text-box mt-5 mb-5 p-4">
						<p class="p-large">?????? ?????? ????????? ?????????? ???????????? ???????????? ?????? ?????? ????????? ???????????? ????????? ????????? ????????? ?????????. ?????? ?????? ????????? ???????????? ?????? ??? ??????????????? ???????????? ???????????? ???????????? ???????????????. ????????? ???????????? ????????? ????????? ???????????? ????????? ??? ????????????.</p>
						<br>
						<p class="p-large"><strong>??????!</strong></p>
						<p class="p-large">* ????????? ????????? ???????????? ????????? ????????? ?????? ?????? ????????????.</p>
						<p class="p-large">* ?????? ????????? '#'?????? ???????????? ?????????. ?????? ?????? ????????? ???????????? ??? ?????????. (Ex. #??????#??????)</p>
						<p class="p-large">* ?????? ???????????? ?????????, ?????????, ????????? ????????? ?????? ???????????? ????????????.</p>
						<p class="p-large">* ?????? ???????????? 512x512 ????????? ???????????????. ?????? ???????????? ????????? ???????????????.</p>
						<p class="p-large">* ?????? ????????? ??? ????????? ??????????????? ????????? ?????? ????????? ????????? ????????? ?????????.</p>
					</div> <!-- end of text-box -->
					
	            	<div style="width:100%; margin-top: 20px;">
	                	<div class="text-center text-lg-start py-4 pt-lg-0">
	                		<h3 class="mb-3"><strong>?????? ?????? ?????? (MP3 ?????? ??????)</strong></h3>
	                        <p class="para-light">????????? ?????? ????????? ????????? ?????????. ?????? ????????? ?????? ???????????? ?????????.</p>
	                    </div>
	                    
                    	<div>
                            <div class="row" >
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="songNameInput" placeholder="?????? ??????" maxlength="100">
                                    </div>                                
                                </div>
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="singerNameInput" placeholder="???????????? ??????" maxlength="50">
                                    </div>                                 
                                </div>
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="songWriterNameInput" placeholder="????????? ??????" maxlength="30">
                                    </div>                                
                                </div>
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="songTagInput" placeholder="?????? ??????" maxlength="100"/>
                                    </div>                                 
                                </div>
                            </div>
                            <div class="form-group py-1">
                                <input type=file class="form-control form-control-input" id="songImageInput" accept=".jpg, .png">
                                <p class="para-light">*?????? ????????? ??????</p>
                            </div>  
                            <div class="form-group py-1">
                                <input type=file class="form-control form-control-input" id="songMp3Input" accept=".mp3">
                                <p class="para-light">*?????? MP3 ??????</p>
                            </div>
                            <div class="form-group py-2">
                                <textarea class="form-control form-control-input" id="songLyricsInput" rows="6" placeholder="?????? ??????"></textarea>
                            </div>                              
                        </div>
                        <div class="my-3">
                            <a class="btn btn-tertiary" onClick="addSongManagerFunc()">?????? ??????</a>
                        </div>
                    </div> <!-- end of div -->
                    
                    <div style="width:100%; margin-top: 40px;">
	                	<div class="text-center text-lg-start py-4 pt-lg-0">
	                		<h3 class="mb-3"><strong>?????? ?????? ?????? (?????? ?????? ??????)</strong></h3>
	                        <p class="para-light">????????? ?????? ????????? ????????? ?????????. ?????? ????????? ?????? ???????????? ?????????.</p>
	                    </div>
	                    
                    	<div>
                            <div class="row" >
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="songNameInput_for_sep" placeholder="?????? ??????" maxlength="100">
                                    </div>                                
                                </div>
                                <div class="col-lg-6">
                                    <div class="form-group py-2">
                                        <input type="text" class="form-control form-control-input" id="singerNameInput_for_sep" placeholder="???????????? ??????" maxlength="50">
                                    </div>                                 
                                </div>
                      			<div class="form-group py-1">
                                	<input type="text" class="form-control form-control-input" id="youtubeUrlInput_for_sep" placeholder="Youtube URL" maxlength="200">
                            	</div>                             
                        	</div>                   
                        </div>
                        <div class="my-3">
                            <a class="btn btn-tertiary" onClick="addSongManagerFuncForSep()">?????? ??????</a>
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