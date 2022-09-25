package kro.kr.rhya_network.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.Gson;

public class IPLocationGet {
	@SuppressWarnings("unchecked")
	public String GetLocateByIp(String ip) {
		Gson gson = new Gson();
		HashMap<String, Object> resultMap = null;
		
		try {
			//URL url = new URL("http://ip-api.com/json/naver.com");
			URL url = new URL("http://ip-api.com/json/".concat(ip));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			int responseCode = con.getResponseCode();
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((inputLine = in.readLine()) != null) 
				responseBuffer.append(inputLine);
			
			in.close();
			if(200 == responseCode) {
				resultMap = gson.fromJson(responseBuffer.toString(), HashMap.class);
				
				if("success".equals(resultMap.get("status"))){
					StringBuilder sb = new StringBuilder();
					sb.append(resultMap.get("country"));
					sb.append(" / ");
					sb.append(resultMap.get("regionName"));
					
					return sb.toString();
				}else {
					return "[NULL]";
				}
			}else {
				return "[NULL]";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			return "[NULL]";
		}	
	}
}
