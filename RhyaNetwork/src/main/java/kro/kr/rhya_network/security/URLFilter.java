package kro.kr.rhya_network.security;

public class URLFilter {
	// URL 전용 필터 - 변환
	public String SetFilter(String Iurl) {
		Iurl = Iurl.replaceAll("\\+", "_st_plus_01_");
		return Iurl;
	}
	
	
	// URL 전용 필터 - 복호화
	public String GetFilter(String Iurl) {
		Iurl = Iurl.replaceAll("_st_plus_01_", "+");
		
		return Iurl;
	}
}
