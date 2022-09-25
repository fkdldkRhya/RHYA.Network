<%@ page import="kro.kr.rhya_network.utils.db.DatabaseManager"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
   pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title>RHYA.Network Utaite Player Licenses</title>
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


	<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/purecounter.min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/swiper.min.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/aos.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/webpage/resources/assets/main/js/script.js"></script>
	
	
	<body>
	    <!-- Navigation -->
	    <nav id="navbar" class="navbar navbar-expand-lg fixed-top navbar-dark" aria-label="Main navigation">
	        <div class="container">
	            <a class="navbar-brand logo-text">RHYA.Network</a>
	        </div> <!-- end of container -->
	    </nav> <!-- end of navbar -->
	    <!-- end of navigation -->
	
	
	    <!-- Header -->
	    <header class="ex-header">
	        <div class="container">
	            <div class="row">
	                <div class="col-xl-10 offset-xl-1">
	                    <h1>Utaite Player Licenses</h1>
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
	            		DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
	            		try {
	            			databaseConnection.init();
	            			databaseConnection.connection();
	            			databaseConnection.setPreparedStatement("SELECT * FROM utaite_open_sources_licenses;");
	            			databaseConnection.setResultSet();
	            			
	            			while (databaseConnection.getResultSet().next())
	            				out.println(databaseConnection.getResultSet().getString("context"));
	            		}catch (Exception ex) {
	            			ex.printStackTrace();
	            		}finally {
	            			databaseConnection.allClose();
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
	</body>
</html>