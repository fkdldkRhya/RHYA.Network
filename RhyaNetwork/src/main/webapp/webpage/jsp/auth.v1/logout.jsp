<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
//쿠키 생성
Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", null);
// 쿠키에 경로 추가
c3.setPath("/");
// 쿠키에 설명을 추가
c3.setComment("UserUUID");
// 쿠키 유효기간을 설정
c3.setMaxAge(0);
// 쿠키 생성
Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", null);
// 쿠키에 경로 추가
c4.setPath("/");
// 쿠키에 설명을 추가
c4.setComment("TokenUUID");
// 쿠키 유효기간을 설정
c4.setMaxAge(0);
// 응답헤더에 쿠키를 추가
response.addCookie(c3);
response.addCookie(c4);


session.removeAttribute(LoginChecker.LOGIN_SESSION_NAME);
response.sendRedirect(JspPageInfo.GetJspPageURL(request, 12));
%>