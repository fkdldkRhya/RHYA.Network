package kro.kr.rhya_network.security;

public class URLFilter {
	// URL ���� ���� - ��ȯ
	public String SetFilter(String Iurl) {
		Iurl = Iurl.replaceAll("\\+", "_st_plus_01_");
		return Iurl;
	}
	
	
	// URL ���� ���� - ��ȣȭ
	public String GetFilter(String Iurl) {
		Iurl = Iurl.replaceAll("_st_plus_01_", "+");
		
		return Iurl;
	}
}
