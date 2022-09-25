package kro.kr.rhya_network.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kro.kr.rhya_network.data_buffer.DataBufferManager;
import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.util.JSPUtilsInitTask;

/**
 * Servlet implementation class DataBufferUserManager
 */
@WebServlet("/data_buffer_user_manager")
public class DataBufferUserManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataBufferUserManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		// Rhya �ΰ� ���� ����
		RhyaLogger rl = new RhyaLogger();
		// Rhya �ΰ� ����
		rl.JspName = request.getServletPath();
		rl.LogConsole = true;
		rl.LogFile = true;

		// Ŭ���̾�Ʈ ������
		String clientIP = GetClientIPAddress.getClientIp(request);

		// ��� ���
		Gson gson = new Gson();
		JsonObject obj = new JsonObject();
		final String successMessage = "success";
		final String failMessage = "fail";
		final String keyName_Result = "result";
		final String keyName_Message = "message";
		
		try {
			JSPUtilsInitTask jspUtilsInitTask = new JSPUtilsInitTask();
			if (jspUtilsInitTask.initTask(rl, request, response, JspPageInfo.PageID_Online_Attendance)) {
				// ��ɾ�
				int command = Integer.parseInt(request.getParameter("mode"));
				// ��ɾ� ����
				switch (command) {
					default: {
						obj.addProperty(keyName_Result, failMessage);
						obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���.", "UTF-8"));
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "�� �� ����Դϴ�. mode �Ķ���͸� Ȯ���� �ּ���."));
						break;
					}
					
					/**
					 * Data-Buffer ������ : 0
					 * 
					 * ���� :
					 * 		������ ���
					 * 
					 * �Ķ���� :
					 * 		default  --> �⺻ ������ �Է�
					 */
					case 0: {
						String getParmInput = request.getParameter("default");
						getParmInput = URLDecoder.decode(getParmInput, "UTF-8");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String key = dataBufferManager.createBuffer(getParmInput);
						
						obj.addProperty(keyName_Result, successMessage);
						obj.addProperty(keyName_Message, key);
						PrintWriter out = response.getWriter(); 
						out.println(gson.toJson(obj));
						
						rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer ������ ���� ����! / RequestCode: ", key));
						break;
					}
					
					
					/**
					 * Data-Buffer ������ : 1
					 * 
					 * ���� :
					 * 		������ ���
					 * 
					 * �Ķ���� :
					 * 		request  --> ��û Ű
					 * 		input    --> �Է� ������
					 * 		index    --> index
					 */
					case 1: {
						// �Ķ���� ����
						String getParmRequestCode = request.getParameter("request");
						String getParmInput = request.getParameter("input");
						String getParmIndex = request.getParameter("index");
						getParmInput = URLDecoder.decode(getParmInput, "UTF-8");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String value = dataBufferManager.addBuffer(getParmRequestCode, Integer.parseInt(getParmIndex), getParmInput);
						
						if (value != null) {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty(keyName_Message, value);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "Data-Buffer ������ �߰� ����! / RequestCode: ", getParmRequestCode, ", Input: ", getParmInput, ", Index: ", String.valueOf(getParmIndex)));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv12(clientIP, "Data-Buffer ������ �߰� ����! [NotFoundRequestCode] / RequestCode: ", getParmRequestCode, ", Input: ", getParmInput, ", Index: ", String.valueOf(getParmIndex)));
						}
						
						break;
					}
					
					
					/**
					 * Data-Buffer ������ : 2
					 * 
					 * ���� :
					 * 		������ �ҷ�����
					 * 
					 * �Ķ���� :
					 * 		request  --> ��û Ű
					 */
					case 2: {
						String getParmRequestCode = request.getParameter("default");
						
						DataBufferManager dataBufferManager = new DataBufferManager();
						String value = dataBufferManager.getBuffer(getParmRequestCode);
						
						if (value == null) {
							obj.addProperty(keyName_Result, successMessage);
							obj.addProperty(keyName_Message, value);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer ������ ��� ����! / RequestCode: ", getParmRequestCode));
						}else {
							obj.addProperty(keyName_Result, failMessage);
							PrintWriter out = response.getWriter(); 
							out.println(gson.toJson(obj));
							
							rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv8(clientIP, "Data-Buffer ������ ��� ����! [NoFoundRequestCode] / RequestCode: ", getParmRequestCode));
						}
						
						break;
					}
				}
			}else {
				rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "JSP ������ �ʱ�ȭ �� ������ �߻��Ͽ����ϴ�."));
			}
		}catch (Exception ex) {
			// TODO: handle exception
			obj.addProperty(keyName_Result, failMessage);
			obj.addProperty(keyName_Message, URLEncoder.encode("�� �� ���� ������ �߻� �Ͽ����ϴ�. ".concat(ex.getMessage()), "UTF-8"));
			PrintWriter out = response.getWriter(); 
			out.println(gson.toJson(obj));
			
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, ex.toString()));
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
