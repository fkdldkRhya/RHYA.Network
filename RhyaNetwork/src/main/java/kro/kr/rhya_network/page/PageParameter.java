package kro.kr.rhya_network.page;

import java.util.ArrayList;
import java.util.Arrays;

public class PageParameter {
	// 페이지 이동 파라미터
	public static final String REDIRECT_PAGE_ID_PARM = "rpid";
	// 로그인 토큰 생성
	public static final String IS_CREATE_TOKEN_PARM = "ctoken";
	
	// 회원가입 페이지
	public static class SignUp {
		// 파라미터 이름
		public final String NAME = "name";
		public final String ID = "id";
		public final String EMAIL = "email";
		public final String BIRTHDAY = "birthday";
		public final String PASSWORD = "password";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Json 키 이름
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax 결과
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// 로그인 페이지
	public static class SignIn {
		// 파라미터 이름
		public final String ID = "id";
		public final String PASSWORD = "password";
		public final String REMEMBER_ME = "remember_me";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Cookie 이름
		public final String COOKIE_NAME_USER = "AutoLogin_UserUUID";
		public final String COOKIE_NAME_TOKEN = "AutoLogin_TokenUUID";
		
		// Json 키 이름
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax 결과
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// 비밀번호 변경
	public static class ForgotPWD {
		// 파라미터 이름
		public final String NAME = "name";
		public final String ID = "id";
		public final String EMAIL = "email";
		public final String PASSWORD = "pw";
		public final String PASSWORD_C = "pwc";
		public final String UUID_USER = "uuid";
		public final String UUID_AUTH = "auth";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Json 키 이름
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax 결과
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// Auth token
	public static class AuthToken {
		// 파라미터 이름
		public final String USER = "user";
		public final String TOKEN = "token";
		public final String NAME = "name";
		
		// Cookie 이름
		public final String RESULT_COOKIE = "AuthTokenResult";
		
		// 지원 서비스 이름
		public final ArrayList<String> SERVICE = new ArrayList<String>(
				Arrays.asList(
						"kro_kr_rhya__network_jp__player",
						"kro_kr_rhya__network_online__attendance",
						"kro_kr_rhya__network_vpn_service"));
	}
}
