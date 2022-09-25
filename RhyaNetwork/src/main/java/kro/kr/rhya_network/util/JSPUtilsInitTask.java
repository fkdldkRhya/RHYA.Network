package kro.kr.rhya_network.util;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kro.kr.rhya_network.database.DatabaseConnection;
import kro.kr.rhya_network.database.DatabaseInfo;
import kro.kr.rhya_network.logger.GetClientIPAddress;
import kro.kr.rhya_network.logger.RhyaLogger;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.security.IPBlockChecker;

public class JSPUtilsInitTask {
	public boolean initTask(RhyaLogger rl, HttpServletRequest request, HttpServletResponse response, int pageID) throws ServletException, IOException, SQLException {
		// ���� �ۼ� StringBuilder
		StringBuilder sql = new StringBuilder();

		// Ŭ���̾�Ʈ ������
		String clientIP = GetClientIPAddress.getClientIp(request);

		// �����ͺ��̽� Ŀ���� ���� ����
		DatabaseConnection cont = new DatabaseConnection();
		// �����ͺ��̽� ���� ���� ���� ����
		PreparedStatement stat = null;
		ResultSet rs = null;


		//------------------------------------------------
		if (!IPBlockChecker.isIPBlock(clientIP)) {
			// �α� ���
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP ���� ��Ͽ� �ִ� ȣ��Ʈ�� ������ �õ��Ͽ����ϴ�. �ش� ȣ��Ʈ�� ������ �ý����� �ź��߽��ϴ�."));
			// ������ �̵�
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
			rd.forward(request,response);
			return false;
		}
		//------------------------------------------------


		// �����ͺ��̽� ���� ���� ó��
		try {
			// �����ͺ��̽� ����
			cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
							DatabaseInfo.DATABASE_CONNECTION_URL,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		}catch (SQLException ex1) {
			// �����ͺ��̽� ���� ���� ó��
			cont.Close();
			sql = null;
			// �α� �ۼ�
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
			// ������ �̵�
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		  	
			return false;
		}catch (ClassNotFoundException ex2) {
			// �����ͺ��̽� ���� ���� ó��
			cont.Close();
			sql = null;
			// �α� �ۼ�
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
			// ������ �̵�
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		  	
			return false;
		}

		// ������ ���� Ȯ��
		if (cont != null) {
			// ���� ����
			sql.append("SELECT * FROM ");
			sql.append(DatabaseInfo.DATABASE_TABLE_NAME_JSP_PAGE_SETTING);
			sql.append(" WHERE ");
			sql.append(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_ID);
			sql.append("=");
			sql.append("?;");

			
			// ���� ����
			stat = cont.GetConnection().prepareStatement(sql.toString());
			stat.setInt(1, pageID);
			// ���� ���� StringBuilder �ʱ�ȭ
			sql.delete(0,sql.length());
			// ���� ����
			rs = stat.executeQuery();
			// ���� ���� ���
			int state = 0;
			if (rs.next()) {
				state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
			}
			// ���� Ȯ�� - ��� ó��
			if (!JspPageInfo.JspPageStateManager(state)) {
				// ���� ����
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;

				// ������ �̵�
				RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
			  	rd.forward(request,response);
				return false;
			}
		}

		return true;
	}
}
