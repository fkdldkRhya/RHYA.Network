<%@ page import="java.sql.*"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="com.google.gson.JsonObject"%>
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
<%@ page import="kro.kr.rhya_network.util.JSPUtilsInitTask"%>
<%@ page import="kro.kr.rhya_network.util.UserPermissionChecker"%>
<%@ page import="kro.kr.rhya_network.admintool.ServerMainImageManager"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<%
// 필요 권한
final int requirePermissionLevel = 1;
// Body 출력 여부
boolean isPrintHTML = true;
//데이터베이스 데이터
int settingValue = 0;

//Rhya 로거 변수 선언
RhyaLogger rl = new RhyaLogger();
//Rhya 로거 설정
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// 클라이언트 아이피
String clientIP = GetClientIPAddress.getClientIp(request);

// 로그인 확인
String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
if (login_session != null) {
	// 자동 로그인
	String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
	
	if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
		// 권한 확인
		UserPermissionChecker userPermissionChecker = new UserPermissionChecker();
		
		if (Integer.parseInt(auto_login_result[9]) < requirePermissionLevel) {
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, String.format("권한 부족, LEVEL %d 이상의 권한이 필요합니다.", requirePermissionLevel)));
			
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		
			return;
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인이 되어있지 않습니다."));
		
		// 페이지 이동
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
	  	
	  	return;
	}
}else {
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "로그인이 되어있지 않습니다."));
	
	// 페이지 이동
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
  	
  	return;
}

// 설정 데이터 불러오기
try {
	// 페이지 초기화
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager)) { // 페이지 초기화 성공
		// 처리 작업 구분
		String mode = request.getParameter("mode");
		if (mode != null) {
			// JSON 반환 결과
			final String successMessage = "success";
		    final String failMessage = "fail";
			// JSON 결과
			final String keyName_Result = "result";
			final String keyName_Message = "message";
			// JSON 변수
			Gson gson = new Gson();
			JsonObject obj = new JsonObject();
			
			switch (Integer.parseInt(mode)) {
				// 데이터 반영 작업
				case 1: {
					isPrintHTML = false;
					
					String value = request.getParameter("value");
					int valueForInt = Integer.parseInt(value);
					
					ServerMainImageManager serverMainImageManager = new ServerMainImageManager();
					serverMainImageManager.setServerMainSate(valueForInt);
					
					obj.addProperty(keyName_Result, successMessage);
					out.println(gson.toJson(obj));
					
					break;
				}
			}
		}else {
			// 설정 데이터 관리자 선언
			ServerMainImageManager serverMainImageManager = new ServerMainImageManager();
			settingValue = serverMainImageManager.getServerMainSate();	
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
	}
}catch (Exception ex) {
	ex.printStackTrace();
	
	// 로그 작성
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request), String.format("관리자 페이지 작업 처리 중 알 수 없는 오류 발생! [%s]", ex.toString())));
	// 페이지 이동
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	rd.forward(request,response);
}
%>

<%if(isPrintHTML){%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>RHYA.Network Manager Tool</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Do+Hyeon&family=Kanit:wght@300&family=Noto+Sans+KR:wght@400;500&family=Rubik:ital,wght@1,300&display=swap" rel="stylesheet">
</head>

<script src="<%=request.getContextPath()%>/webpage/resources/assets/user_account/vendor/jquery/jquery-3.2.1.min.js"></script>
<script type="text/javascript" src='https://cdn.jsdelivr.net/npm/sweetalert2@10'></script>
<script type="text/javascript">
function task() {
	var cbx1 = $('#mode1').is(':checked');
	var cbx2 = $('#mode2').is(':checked');
	
	if (!(cbx1 == true && cbx2 == true) && !(cbx1 == false && cbx2 == false)) {
		// 로딩 중 메시지 출력
		Swal.fire({
	        title: 'Please Wait!',
	        html: '처리 중...',
	        allowOutsideClick: false,
	        didOpen: () => {
	        	Swal.showLoading()
	        }
	    });
		
		// Ajax
		$.ajax({
			url: "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager)%>",
			type: "POST",
			data:{
				"mode" : "1",
				"value" : cbx1 ? 0 : 1
			},
			
			success: function(result){
				Swal.close();
				ajax_result_json = JSON.parse(result);
				if (ajax_result_json.result == 'success') {
			    	Swal.fire({
		    		    title: "작업 처리 성공!",
		    		    text: "해당 설정이 서버에 반영되었습니다.",
		    		    icon: "success"
		    		});	
				}else {
			    	Swal.fire({
		    		    title: "작업 처리 실패!",
		    		    text: "알 수 없는 오류 발생!",
		    		    icon: "error"
		    		});
				}
			},
			
			error: function(){
				Swal.close();
		    	Swal.fire({
	    		    title: "작업 처리 실패!",
	    		    text: "Ajax 통신 실패! 다시 시도해 주세요.",
	    		    icon: "error"
	    		});
			}
		});
	}
}
</script>

<body>
<h1 style="font-family: 'Kanit', sans-serif; font-size: 40px">RHYA.Network Manager Tool</h1>
<pre style="font-family: 'Do Hyeon', sans-serif; font-size: 20px">
해당 도구는 RHYA.Network서버 관리자 도구입니다.
따라서 서버에 직접적인 영향을 미칠 수 있습니다. 따라서 사용하시는 관리자분들께 각별한 주의를 필요로 합니다.
</pre>

<br>

<pre style="font-family: 'Noto Sans KR', sans-serif;">
<strong style="font-size: 20px">RHYA.Network Web server background main image manager</strong>

<strong>&lt;사용 필요 권한&gt;</strong>
LEVEL 1

<strong>&lt;도구 설명&gt;</strong>
해당 도구는 RHYA.Network의 Main 이미지의 출력 타입을 변경할 수 있는 도구입니다.
관리자가 설정할 수 있는 타입으로는 Type A, B가 있습니다.

Type A: 서버에 지정된 기본 이미지 사용
Type B: 서버 내에 저장되어있는 고화질 무작위 이미지 사용

<strong>&lt;작업&gt;</strong>
<input id="mode1" type="radio" name="mode" <%=settingValue == 0 ? "checked" : ""%> onClick="task()">Type A</input>
<input id="mode2" type="radio" name="mode" <%=settingValue == 1 ? "checked" : ""%> onClick="task()">Type B</input>
</pre>

<br></br>

<pre style="font-family: 'Rubik', sans-serif;">
Font desing Copyright Kanit. Designed by Cadson Demak
Font desing Copyright Do Rubik. Designed by Hubert and Fischer, Meir Sadan, Cyreal
Font desing Copyright Do Hyeon. Designed by Woowahan Brothers
Font desing Copyright Noto Sans Korean.

Copyright 2022 RHYA.Network. All rights reserved.
</pre>
</body>
</html>
<%}%>