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
		// ��ū�� ����Ű�� ������ ���� ���θ� Ȯ�� ��
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

	    // ����ڵ� Ȯ��(200 : ����)
	    int responseCode = conn.getResponseCode();
	    if (responseCode == 200) {
	        // ������ ����
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

	        // ���� ����
	        if ((boolean) obj.get("success")) {
	        	double score = (double) obj.get("score");
	        	// ���� ��
	        	if (score >= 0.7) {
	        		// ��� ��ȯ
		        	jsonParse = null;
		        	obj = null;

		        	return true;	
	        	}
	        }
	    }
	    // ��� ��ȯ
	    return false;
	}
}
