package kro.kr.rhya_network.page;

import java.util.ArrayList;
import java.util.Arrays;

public class PageParameter {
	// ������ �̵� �Ķ����
	public static final String REDIRECT_PAGE_ID_PARM = "rpid";
	// �α��� ��ū ����
	public static final String IS_CREATE_TOKEN_PARM = "ctoken";
	
	// ȸ������ ������
	public static class SignUp {
		// �Ķ���� �̸�
		public final String NAME = "name";
		public final String ID = "id";
		public final String EMAIL = "email";
		public final String BIRTHDAY = "birthday";
		public final String PASSWORD = "password";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Json Ű �̸�
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax ���
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// �α��� ������
	public static class SignIn {
		// �Ķ���� �̸�
		public final String ID = "id";
		public final String PASSWORD = "password";
		public final String REMEMBER_ME = "remember_me";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Cookie �̸�
		public final String COOKIE_NAME_USER = "AutoLogin_UserUUID";
		public final String COOKIE_NAME_TOKEN = "AutoLogin_TokenUUID";
		
		// Json Ű �̸�
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax ���
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// ��й�ȣ ����
	public static class ForgotPWD {
		// �Ķ���� �̸�
		public final String NAME = "name";
		public final String ID = "id";
		public final String EMAIL = "email";
		public final String PASSWORD = "pw";
		public final String PASSWORD_C = "pwc";
		public final String UUID_USER = "uuid";
		public final String UUID_AUTH = "auth";
		public final String RE_CHAPT_CHA = "rechaptcha";
		public final String INT_KEY = "key";
		
		// Json Ű �̸�
		public final String RESULT = "result";
		public final String MSG = "message";
		
		// Ajax ���
		public final String RST_SUCCESS = "S";
		public final String RST_FAIL = "F";
	}
	
	
	// Auth token
	public static class AuthToken {
		// �Ķ���� �̸�
		public final String USER = "user";
		public final String TOKEN = "token";
		public final String NAME = "name";
		
		// Cookie �̸�
		public final String RESULT_COOKIE = "AuthTokenResult";
		
		// ���� ���� �̸�
		public final ArrayList<String> SERVICE = new ArrayList<String>(
				Arrays.asList(
						"kro_kr_rhya__network_jp__player",
						"kro_kr_rhya__network_online__attendance",
						"kro_kr_rhya__network_vpn_service"));
	}
}
