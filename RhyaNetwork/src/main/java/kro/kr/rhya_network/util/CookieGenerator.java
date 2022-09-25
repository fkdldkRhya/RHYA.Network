package kro.kr.rhya_network.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import kro.kr.rhya_network.logger.RhyaLogger;

public class CookieGenerator {
	// ��Ű ���� �Լ�
	public void createCookie(HttpServletResponse rsp, RhyaLogger rl, String clientIP, String cookieName, String cookieValue, String cookiePath, String cookieComment, int maxAge) {
		// ��Ű ����
	    Cookie cookie = new Cookie(cookieName, cookieValue);
	    // ��Ű�� ��� �߰�
	    cookie.setPath(cookiePath);
	    // ��Ű�� ������ �߰�
	    cookie.setComment(cookieComment);
	    // ��Ű ��ȿ�Ⱓ�� ����
	    cookie.setMaxAge(60);
	    // ��������� ��Ű�� �߰�
	    rsp.addCookie(cookie);
	    
	    // ��Ű ���
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Cookie Generator : Cookie ���� ����!"));
	}
}
