<%@ page import="kro.kr.rhya_network.util.LoginChecker"%>
<%@ page import="kro.kr.rhya_network.page.JspPageInfo"%>

<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
//��Ű ����
Cookie c3 = new Cookie("RhyaAutoLoginCookie_UserUUID", null);
// ��Ű�� ��� �߰�
c3.setPath("/");
// ��Ű�� ������ �߰�
c3.setComment("UserUUID");
// ��Ű ��ȿ�Ⱓ�� ����
c3.setMaxAge(0);
// ��Ű ����
Cookie c4 = new Cookie("RhyaAutoLoginCookie_ToeknUUID", null);
// ��Ű�� ��� �߰�
c4.setPath("/");
// ��Ű�� ������ �߰�
c4.setComment("TokenUUID");
// ��Ű ��ȿ�Ⱓ�� ����
c4.setMaxAge(0);
// ��������� ��Ű�� �߰�
response.addCookie(c3);
response.addCookie(c4);


session.removeAttribute(LoginChecker.LOGIN_SESSION_NAME);
response.sendRedirect(JspPageInfo.GetJspPageURL(request, 12));
%>