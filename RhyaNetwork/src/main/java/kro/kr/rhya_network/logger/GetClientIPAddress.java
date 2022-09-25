package kro.kr.rhya_network.logger;

import javax.servlet.http.HttpServletRequest;

public class GetClientIPAddress {
	public static String getClientIp(HttpServletRequest req) {
		 
	    String[] header_IPs = {
	                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED"
	                , "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED"
	                , "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"
	             } ;
	     
	    for (String header : header_IPs) {
	        String ip = req.getHeader(header);
	         
	        if (ip != null && !"unknown".equalsIgnoreCase(ip) && ip.length() != 0) {
	            return ip;
	        }
	    }
	     
	    return req.getRemoteAddr() ;
	}
}
