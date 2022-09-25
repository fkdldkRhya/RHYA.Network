package kro.kr.rhya_network.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import kro.kr.rhya_network.logger.RhyaLogger;

public class CookieGenerator {
	// 쿠키 생성 함수
	public void createCookie(HttpServletResponse rsp, RhyaLogger rl, String clientIP, String cookieName, String cookieValue, String cookiePath, String cookieComment, int maxAge) {
		// 쿠키 생성
	    Cookie cookie = new Cookie(cookieName, cookieValue);
	    // 쿠키에 경로 추가
	    cookie.setPath(cookiePath);
	    // 쿠키에 설명을 추가
	    cookie.setComment(cookieComment);
	    // 쿠키 유효기간을 설정
	    cookie.setMaxAge(60);
	    // 응답헤더에 쿠키를 추가
	    rsp.addCookie(cookie);
	    
	    // 쿠키 출력
		rl.Log(RhyaLogger.Type.Info, rl.CreateLogTextv5(clientIP, "Cookie Generator : Cookie 생성 성공!"));
	}
}
