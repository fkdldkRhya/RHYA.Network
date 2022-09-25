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
		// 쿼리 작성 StringBuilder
		StringBuilder sql = new StringBuilder();

		// 클라이언트 아이피
		String clientIP = GetClientIPAddress.getClientIp(request);

		// 데이터베이스 커넥터 변수 선언
		DatabaseConnection cont = new DatabaseConnection();
		// 데이터베이스 쿼리 실행 변수 선언
		PreparedStatement stat = null;
		ResultSet rs = null;


		//------------------------------------------------
		if (!IPBlockChecker.isIPBlock(clientIP)) {
			// 로그 출력
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv5(clientIP, "IP 차단 목록에 있는 호스트가 접속을 시도하였습니다. 해당 호스트의 접속을 시스템이 거부했습니다."));
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
			rd.forward(request,response);
			return false;
		}
		//------------------------------------------------


		// 데이터베이스 접속 예외 처리
		try {
			// 데이터베이스 접속
			cont.Connection(DatabaseInfo.DATABASE_DRIVER_CLASS_NAME,
							DatabaseInfo.DATABASE_CONNECTION_URL,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_ID,
							DatabaseInfo.DATABASE_ROOT_ACCOUNT_PW);
		}catch (SQLException ex1) {
			// 데이터베이스 접속 오류 처리
			cont.Close();
			sql = null;
			// 로그 작성
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex1.toString()));
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		  	
			return false;
		}catch (ClassNotFoundException ex2) {
			// 데이터베이스 접속 오류 처리
			cont.Close();
			sql = null;
			// 로그 작성
			rl.Log(RhyaLogger.Type.Error, rl.CreateLogTextv4(clientIP, JspPageInfo.ERROR_PAGE_PATH_HTTP_500, ex2.toString()));
			// 페이지 이동
			RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
		  	rd.forward(request,response);
		  	
			return false;
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
			stat.setInt(1, pageID);
			// 쿼리 생성 StringBuilder 초기화
			sql.delete(0,sql.length());
			// 쿼리 실행
			rs = stat.executeQuery();
			// 쿼리 실행 결과
			int state = 0;
			if (rs.next()) {
				state = rs.getInt(DatabaseInfo.DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE);
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
			}
			// 상태 확인 - 결과 처리
			if (!JspPageInfo.JspPageStateManager(state)) {
				// 연결 종료
				rs.close();
				stat.close();
				cont.Close();
				rl = null;
				sql = null;

				// 페이지 이동
				RequestDispatcher rd = request.getRequestDispatcher(JspPageInfo.GetJspPageURL(request, 22).replace(request.getContextPath().subSequence(0, request.getContextPath().length()), ""));
			  	rd.forward(request,response);
				return false;
			}
		}

		return true;
	}
}
