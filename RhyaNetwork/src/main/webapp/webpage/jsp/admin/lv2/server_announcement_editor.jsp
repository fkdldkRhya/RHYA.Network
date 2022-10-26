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
// �ʿ� ����
final int requirePermissionLevel = 2;
// Body ��� ����
boolean isPrintHTML = true;
//�����ͺ��̽� ������
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

//Rhya �ΰ� ���� ����
RhyaLogger rl = new RhyaLogger();
//Rhya �ΰ� ����
rl.JspName = request.getServletPath();
rl.LogConsole = true;
rl.LogFile = true;

// Ŭ���̾�Ʈ ������
String clientIP = GetClientIPAddress.getClientIp(request);

// �α��� Ȯ��
String[] login_session = (String[]) session.getAttribute(LoginChecker.LOGIN_SESSION_NAME);
if (login_session != null) {
	// �ڵ� �α���
	String[] auto_login_result = LoginChecker.IsAutoLogin(RhyaAES.AES_Decode(login_session[1]), RhyaAES.AES_Decode(login_session[0]), response, true);
	
	if (auto_login_result[0].equals(LoginChecker.LOGIN_RESULT_SUCCESS)) {
		// ���� Ȯ��
		UserPermissionChecker userPermissionChecker = new UserPermissionChecker();
		
		if (Integer.parseInt(auto_login_result[9]) < requirePermissionLevel) {
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, String.format("���� ����, LEVEL %d �̻��� ������ �ʿ��մϴ�.", requirePermissionLevel)));
			
			// ������ �̵�
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		
			return;
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "�α����� �Ǿ����� �ʽ��ϴ�."));
		
		// ������ �̵�
		RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
	  	rd.forward(request,response);
	  	
	  	return;
	}
}else {
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "�α����� �Ǿ����� �ʽ��ϴ�."));
	
	// ������ �̵�
	RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
  	rd.forward(request,response);
  	
  	return;
}

//���� ������ �ҷ�����
try {
	// ������ �ʱ�ȭ
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Announcement_Editor)) { // ������ �ʱ�ȭ ����
		// ó�� �۾� ����
		String mode = request.getParameter("mode");
		if (mode != null) {
			// JSON ��ȯ ���
			final String successMessage = "success";
		    final String failMessage = "fail";
			// JSON ���
			final String keyName_Result = "result";
			final String keyName_Message = "message";
			// JSON ����
			Gson gson = new Gson();
			JsonObject obj = new JsonObject();
			
			switch (Integer.parseInt(mode)) {
			// ������ ���� �۾�
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
			
			// ������ ���� �۾�
			case 2: {
				// ������ ó�� �۾�
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
			
			// ������ ���� �۾�
			case 3: {
				// ������ ó�� �۾�
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
			// ���� ������ ��ó��
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
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP ������ �ʱ�ȭ �� ������ �߻��Ͽ����ϴ�."));
	}
}catch (Exception ex) {
	ex.printStackTrace();
	
	// �α� �ۼ�
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request), String.format("������ ������ �۾� ó�� �� �� �� ���� ���� �߻�! [%s]", ex.getMessage())));
	// ������ �̵�
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
	
	// �ε� �� �޽��� ���
	Swal.fire({
        title: 'Please Wait!',
        html: 'ó�� ��...',
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
	    		    title: "�۾� ó�� ����!",
	    		    text: "�ش� ���������� �߰��Ǿ����ϴ�.",
	    		    icon: "success"
	    		});	
			}else {
		    	Swal.fire({
	    		    title: "�۾� ó�� ����!",
	    		    text: "�� �� ���� ���� �߻�!",
	    		    icon: "error"
	    		});
			}
		},
		
		error: function(){
			Swal.close();
	    	Swal.fire({
    		    title: "�۾� ó�� ����!",
    		    text: "Ajax ��� ����! �ٽ� �õ��� �ּ���.",
    		    icon: "error"
    		});
		}
	});
}

function deleteButton() {
	// �ε� �� �޽��� ���
	Swal.fire({
        title: 'Please Wait!',
        html: 'ó�� ��...',
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
	    		    title: "�۾� ó�� ����!",
	    		    text: "�ش� ���������� �����Ǿ����ϴ�.",
	    		    icon: "success"
	    		});	
			}else {
		    	Swal.fire({
	    		    title: "�۾� ó�� ����!",
	    		    text: "�� �� ���� ���� �߻�!",
	    		    icon: "error"
	    		});
			}
		},
		
		error: function(){
			Swal.close();
	    	Swal.fire({
    		    title: "�۾� ó�� ����!",
    		    text: "Ajax ��� ����! �ٽ� �õ��� �ּ���.",
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
	
	// �ε� �� �޽��� ���
	Swal.fire({
        title: 'Please Wait!',
        html: 'ó�� ��...',
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
	    		    title: "�۾� ó�� ����!",
	    		    text: "�ش� ���������� ���� ������ ����Ǿ����ϴ�.",
	    		    icon: "success"
	    		});	
			}else {
		    	Swal.fire({
	    		    title: "�۾� ó�� ����!",
	    		    text: "�� �� ���� ���� �߻�!",
	    		    icon: "error"
	    		});
			}
		},
		
		error: function(){
			Swal.close();
	    	Swal.fire({
    		    title: "�۾� ó�� ����!",
    		    text: "Ajax ��� ����! �ٽ� �õ��� �ּ���.",
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
�ش� ������ RHYA.Network���� ������ �����Դϴ�.
���� ������ �������� ������ ��ĥ �� �ֽ��ϴ�. ���� ����Ͻô� �����ںе鲲 ������ ���Ǹ� �ʿ�� �մϴ�.
</pre>

<br>

<pre style="font-family: 'Noto Sans KR', sans-serif;">
<strong style="font-size: 20px">RHYA.Network Web server announcement manager</strong>

<strong>&lt;��� �ʿ� ����&gt;</strong>
LEVEL 2

<strong>&lt;���� ����&gt;</strong>
������ ���������� ����, ����, ���� ���� �۾��� ó���� �� �ִ� �����Դϴ�.

<strong>&lt;�۾�&gt;</strong>
</pre>

<table border="1" style="font-family: 'Noto Sans KR', sans-serif;">
	<th>�ε���</th>
	<th>�������� ����</th>
	<th>���� ��¥</th>
	<th>���� �����</th>
	
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
Full index length : <%=announcementDataVOList.size()%>��

Start index
<input type="number" id="moveStartInputValue" value="<%=startPage%>"/>
End index
<input type="number" id="moveEndInputValue" value="<%=endPage%>"/>

<button onclick="moveButton(moveStartInputValue.value, moveEndInputValue.value)">Move</button>
</pre>

<br></br>

���� <input type="text" id="titleTextbox" value="<%=title%>" style="width:400px;"/>
<br></br>
��¥ <input type="text" id="dateTextbox" value="<%=date%>" style="width:400px;"/>
<br></br>
�����
<input type="checkbox" id="isShowCbx" id="checkboxForContentHidden" <%out.print(isHidden == 1 ? "checked" : "");%>/>
<br></br>
����
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