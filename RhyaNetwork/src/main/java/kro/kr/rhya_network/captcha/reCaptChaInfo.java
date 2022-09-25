package kro.kr.rhya_network.captcha;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class reCaptChaInfo {
	// Google reCaptCha API key
	public final String reCaptChaPublicKey = "6Lcxy-IbAAAAAP0vkUOK__TNG0SyieO5J6m_Ds5Q";
	public final String reCaptChaPrivateKey = "6Lcxy-IbAAAAAIF6OkXm89fzU25LYlYJ6SNO84hE";
	
	
	// Google reCaptCha checker
	public boolean reCaptChaChecker(String token) throws IOException, ParseException {
		// 토큰과 보안키를 가지고 성공 여부를 확인 함
	    HttpURLConnection conn = (HttpURLConnection) new URL("https://www.google.com/recaptcha/api/siteverify").openConnection();
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("secret=");
	    sb.append(reCaptChaPrivateKey);
	    sb.append("&");
	    sb.append("response=");
	    sb.append(token);
	    String parm = sb.toString();
	    sb.delete(0,sb.length());
	    
	    conn.setRequestMethod("POST");
	    conn.setDoOutput(true);
	    
	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	    wr.writeBytes(parm);
	    wr.flush();
	    wr.close();

	    // 결과코드 확인(200 : 성공)
	    int responseCode = conn.getResponseCode();
	    if (responseCode == 200) {
	        // 데이터 추출
	        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
	        BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	sb.append(line);
	        }
	        
	        bis.close();
	        reader.close();
	        conn.disconnect();

	        JSONParser jsonParse = new JSONParser();
	        JSONObject obj =  (JSONObject) jsonParse.parse(sb.toString());
	        sb = null;

	        // 성공 여부
	        if ((boolean) obj.get("success")) {
	        	double score = (double) obj.get("score");
	        	// 점수 비교
	        	if (score >= 0.7) {
	        		// 결과 반환
		        	jsonParse = null;
		        	obj = null;

		        	return true;	
	        	}
	        }
	    }
	    // 결과 반환
	    return false;
	}
}
