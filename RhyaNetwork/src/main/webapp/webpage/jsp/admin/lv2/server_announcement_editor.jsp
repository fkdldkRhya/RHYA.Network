<%@page import="java.net.URLDecoder"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ArrayList"%>
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
<%@ page import="kro.kr.rhya_network.utils.announcement.AnnouncementDataVO"%>
<%@ page import="kro.kr.rhya_network.admintool.ServerMainImageManager"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<%
// 필요 권한
final int requirePermissionLevel = 2;
// Body 출력 여부
boolean isPrintHTML = true;
//데이터베이스 데이터
ArrayList<AnnouncementDataVO> announcementDataVOList = new ArrayList<AnnouncementDataVO>();

String startPageStr = request.getParameter("sIndex");
String endPageStr = request.getParameter("eIndex");
String selectedUUID = request.getParameter("uuid");
String title = "";
String date = "";
String message = "";
int isHidden = 0;
int startPage = 0;
int endPage = 0;
if (startPageStr != null && endPageStr != null) {
	startPage = Integer.parseInt(startPageStr);
	endPage = Integer.parseInt(endPageStr);
}

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

//설정 데이터 불러오기
try {
	// 페이지 초기화
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)) { // 페이지 초기화 성공
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
			// 데이터 제거 작업
			case 1: {
				isPrintHTML = false;
				
				String uuid = request.getParameter("uuid");
				
				DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
				databaseConnection.init();
				databaseConnection.connection();
				databaseConnection.setPreparedStatement("DELETE FROM announcement WHERE uuid = ?");
				databaseConnection.getPreparedStatement().setString(1, uuid);
				databaseConnection.executeUpdate();
				databaseConnection.allClose();
				
				obj.addProperty(keyName_Result, successMessage);
				out.println(gson.toJson(obj));
				
				break;
			}
			
			// 데이터 변경 작업
			case 2: {
				// 데이터 처리 작업
				isPrintHTML = false;
				
				String uuid = request.getParameter("uuid");
				
				DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
				databaseConnection.init();
				databaseConnection.connection();
				databaseConnection.setPreparedStatement("UPDATE announcement SET title = ?, message = ?, is_show = ?, date = ?  WHERE uuid = ?");
				databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(request.getParameter("title"), "UTF-8"));
				databaseConnection.getPreparedStatement().setString(2, URLDecoder.decode(request.getParameter("message"), "UTF-8"));
				databaseConnection.getPreparedStatement().setInt(3, Integer.parseInt(request.getParameter("is_show")));
				databaseConnection.getPreparedStatement().setString(4, URLDecoder.decode(request.getParameter("date"), "UTF-8"));
				databaseConnection.getPreparedStatement().setString(5, uuid);
				databaseConnection.executeUpdate();
				databaseConnection.allClose();
				
				obj.addProperty(keyName_Result, successMessage);
				out.println(gson.toJson(obj));
				
				break;
			}
			
			// 데이터 생성 작업
			case 3: {
				// 데이터 처리 작업
				isPrintHTML = false;
				
				DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
				databaseConnection.init();
				databaseConnection.connection();
				databaseConnection.setPreparedStatement("INSERT INTO announcement (title, message, is_show) VALUE (?, ?, ?)");
				databaseConnection.getPreparedStatement().setString(1, URLDecoder.decode(request.getParameter("title"), "UTF-8"));
				databaseConnection.getPreparedStatement().setString(2, URLDecoder.decode(request.getParameter("message"), "UTF-8"));
				databaseConnection.getPreparedStatement().setInt(3, Integer.parseInt(request.getParameter("is_show")));
				databaseConnection.executeUpdate();
				databaseConnection.allClose();
				
				obj.addProperty(keyName_Result, successMessage);
				out.println(gson.toJson(obj));
				
				break;
			}
			}
		}else {
			// 설정 데이터 전처리
			DatabaseManager.DatabaseConnection databaseConnection = new DatabaseManager.DatabaseConnection();
			databaseConnection.init();
			databaseConnection.connection();
			databaseConnection.setPreparedStatement("SELECT * FROM announcement ORDER BY date DESC;");
			databaseConnection.setResultSet();
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
			
			while (databaseConnection.getResultSet().next()) {
				String uuidTemp = databaseConnection.getResultSet().getString("uuid");
				String titleTemp = databaseConnection.getResultSet().getString("title");
				String messageTemp = databaseConnection.getResultSet().getString("message");
				int isShowTemp = databaseConnection.getResultSet().getInt("is_show");
				Date dateTemp = databaseConnection.getResultSet().getDate("date");
				
				AnnouncementDataVO announcementDataVO = new AnnouncementDataVO();
				announcementDataVO.uuid = uuidTemp;
				announcementDataVO.title = titleTemp;
				announcementDataVO.message = messageTemp;
				announcementDataVO.isShow = isShowTemp;
				announcementDataVO.date = dateTemp;
				
				announcementDataVOList.add(announcementDataVO);
			}
			
			for (AnnouncementDataVO announcementDataVO : announcementDataVOList) {
				if (selectedUUID != null && announcementDataVO.uuid.equals(selectedUUID)) {
					title = announcementDataVO.title;
					message = announcementDataVO.message;
					date = simpleDateFormat.format(announcementDataVO.date);
					isHidden = announcementDataVO.isShow;
					
					break;
				}
			}
			
			databaseConnection.allClose();
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP 페이지 초기화 중 오류가 발생하였습니다."));
	}
}catch (Exception ex) {
	ex.printStackTrace();
	
	// 로그 작성
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request), String.format("관리자 페이지 작업 처리 중 알 수 없는 오류 발생! [%s]", ex.getMessage())));
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
function selectRadioButtonForIndex(value) {
	window.location.href = "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)%>?uuid=" + value + "&sIndex=<%=startPage%>&eIndex=<%=endPage%>";
}

function createButton() {
	var title = $('#titleTextbox').val();
	var message = $('#messageTexArea').val();
	var isShow = $('#isShowCbx').is(":checked") == true ? 1 : 0;
	
	title = encodeURI(title, "UTF-8");
	message = encodeURI(message, "UTF-8");
	
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
		url: "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)%>",
		type: "POST",
		data:{
			"mode" : "3",
			"title" : title,
			"is_show" : isShow,
			"message" : message
		},
		
		success: function(result){
			Swal.close();
			ajax_result_json = JSON.parse(result);
			if (ajax_result_json.result == 'success') {
		    	Swal.fire({
	    		    title: "작업 처리 성공!",
	    		    text: "해당 공지사항이 추가되었습니다.",
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

function deleteButton() {
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
		url: "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)%>",
		type: "POST",
		data:{
			"mode" : "1",
			"uuid" : "<%=selectedUUID%>"
		},
		
		success: function(result){
			Swal.close();
			ajax_result_json = JSON.parse(result);
			if (ajax_result_json.result == 'success') {
		    	Swal.fire({
	    		    title: "작업 처리 성공!",
	    		    text: "해당 공지사항이 삭제되었습니다.",
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

function saveButton() {
	var title = $('#titleTextbox').val();
	var date = $('#dateTextbox').val();
	var message = $('#messageTexArea').val();
	var isShow = $('#isShowCbx').is(":checked") == true ? 1 : 0;
	
	title = encodeURI(title, "UTF-8");
	date = encodeURI(date, "UTF-8");
	message = encodeURI(message, "UTF-8");
	
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
		url: "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)%>",
		type: "POST",
		data:{
			"mode" : "2",
			"title" : title,
			"is_show" : isShow,
			"date" : date,
			"message" : message,
			"uuid" : "<%=selectedUUID%>"
		},
		
		success: function(result){
			Swal.close();
			ajax_result_json = JSON.parse(result);
			if (ajax_result_json.result == 'success') {
		    	Swal.fire({
	    		    title: "작업 처리 성공!",
	    		    text: "해당 공지사항의 변경 사항이 적용되었습니다.",
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

function moveButton(start, end) {
	window.location.href = "<%=JspPageInfo.GetJspPageURL(request, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)%>?sIndex=" + start + "&eIndex=" + end;
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
<strong style="font-size: 20px">RHYA.Network Web server announcement manager</strong>

<strong>&lt;사용 필요 권한&gt;</strong>
LEVEL 2

<strong>&lt;도구 설명&gt;</strong>
서버의 공지사항을 생성, 수정, 삭제 등의 작업을 처리할 수 있는 도구입니다.

<strong>&lt;작업&gt;</strong>
</pre>

<table border="1" style="font-family: 'Noto Sans KR', sans-serif;">
	<th>인덱스</th>
	<th>공지사항 제목</th>
	<th>공지 날짜</th>
	<th>공지 숨기기</th>
	
	<%
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
	for (int i = startPage; i < endPage; i ++) {
		AnnouncementDataVO announcementDataVO = announcementDataVOList.get(i);
		out.println("<tr><td><center>%INDEX%</center></td><td><center>%TITLE%</center></td><td><center>%DATE%</center></td><td><center>%ISHOW%</center></td><td><input type=\"radio\" name=\"radioForIndex\" onclick=\"selectRadioButtonForIndex('%UUID%')\"/></td></tr>"
				.replace("%INDEX%", String.valueOf(i))
				.replace("%TITLE%", announcementDataVO.title)
				.replace("%DATE%", simpleDateFormat.format(announcementDataVO.date))
				.replace("%ISHOW%", announcementDataVO.isShow == 0 ? "False" : "True")
				.replace("%UUID%", announcementDataVO.uuid));
	}
	%>
</table>

<pre>
Full index length : <%=announcementDataVOList.size()%>개

Start index
<input type="number" id="moveStartInputValue" value="<%=startPage%>"/>
End index
<input type="number" id="moveEndInputValue" value="<%=endPage%>"/>

<button onclick="moveButton(moveStartInputValue.value, moveEndInputValue.value)">Move</button>
</pre>

<br></br>

제목 <input type="text" id="titleTextbox" value="<%=title%>" style="width:400px;"/>
<br></br>
날짜 <input type="text" id="dateTextbox" value="<%=date%>" style="width:400px;"/>
<br></br>
숨기기
<input type="checkbox" id="isShowCbx" id="checkboxForContentHidden" <%out.print(isHidden == 1 ? "checked" : "");%>/>
<br></br>
내용
<br></br>
<textarea rows="5" id="messageTexArea" cols="30" name="contents" style="width:400px;"><%=message%></textarea>
<br></br>
<button onclick="deleteButton()">Delete</button>
<button onclick="saveButton()">Save</button>
<button onclick="createButton()">Create</button>

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