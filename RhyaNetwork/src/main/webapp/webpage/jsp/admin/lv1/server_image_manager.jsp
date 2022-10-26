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
// �ʿ� ����
final int requirePermissionLevel = 1;
// Body ��� ����
boolean isPrintHTML = true;
//�����ͺ��̽� ������
int settingValue = 0;

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

// ���� ������ �ҷ�����
try {
	// ������ �ʱ�ȭ
	JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
	if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager)) { // ������ �ʱ�ȭ ����
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
				// ������ �ݿ� �۾�
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
			// ���� ������ ������ ����
			ServerMainImageManager serverMainImageManager = new ServerMainImageManager();
			settingValue = serverMainImageManager.getServerMainSate();	
		}
	}else {
		rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP ������ �ʱ�ȭ �� ������ �߻��Ͽ����ϴ�."));
	}
}catch (Exception ex) {
	ex.printStackTrace();
	
	// �α� �ۼ�
	rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(GetClientIPAddress.getClientIp(request), String.format("������ ������ �۾� ó�� �� �� �� ���� ���� �߻�! [%s]", ex.toString())));
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
function task() {
	var cbx1 = $('#mode1').is(':checked');
	var cbx2 = $('#mode2').is(':checked');
	
	if (!(cbx1 == true && cbx2 == true) && !(cbx1 == false && cbx2 == false)) {
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
		    		    title: "�۾� ó�� ����!",
		    		    text: "�ش� ������ ������ �ݿ��Ǿ����ϴ�.",
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
<strong style="font-size: 20px">RHYA.Network Web server background main image manager</strong>

<strong>&lt;��� �ʿ� ����&gt;</strong>
LEVEL 1

<strong>&lt;���� ����&gt;</strong>
�ش� ������ RHYA.Network�� Main �̹����� ��� Ÿ���� ������ �� �ִ� �����Դϴ�.
�����ڰ� ������ �� �ִ� Ÿ�����δ� Type A, B�� �ֽ��ϴ�.

Type A: ������ ������ �⺻ �̹��� ���
Type B: ���� ���� ����Ǿ��ִ� ��ȭ�� ������ �̹��� ���

<strong>&lt;�۾�&gt;</strong>
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