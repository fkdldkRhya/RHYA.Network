package kro.kr.rhya_network.database;

public class DatabaseInfo {
	// 데이터베이스 서버 정보
	public static final String DATABASE_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	public static final String DATABASE_CONNECTION_URL = "jdbc:mysql://192.168.0.19:3306/rhya_network_server?serverTimezone=UTC";
	public static final String DATABASE_ROOT_ACCOUNT_ID = "RHYA_NETWORK";
	public static final String DATABASE_ROOT_ACCOUNT_PW = "QC-CN7U$fu=Hx>aWgXmz=h^ZW{A/4URZ";
	
	
	// 데이터베이스 테이블 정보
	/* JSP 서버 페이지 상태 관리 */
	public static final String DATABASE_TABLE_NAME_JSP_PAGE_SETTING = "jsp_page_setting";
	public static final String DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_ID = "page_id";
	public static final String DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_NAME = "page_name";
	public static final String DATABASE_TABLE_COLUMN_JSP_PAGE_SETTING_PAGE_STATE = "page_state";
	/* RHYA.Network 계정 정보 */
	public static final String DATABASE_TABLE_NAME_USER = "user";
	public static final String DATABASE_TABLE_NAME_USER_INFO = "user_info";
	public static final String DATABASE_TABLE_COLUMN_USER_UUID = "uuid";
	public static final String DATABASE_TABLE_COLUMN_USER_INFO_UUID = "account_uuid";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_ID = "user_id";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_PW = "user_pw";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_NAME = "user_name";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_EMAIL = "user_email";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_BIRTHDAY = "user_birthday";
	public static final String DATABASE_TABLE_COLUMN_USER_USER_REGDATE = "user_regdate";
	public static final String DATABASE_TABLE_COLUMN_USER_RESET_PW_UUID = "reset_pw_uuid";
	public static final String DATABASE_TABLE_COLUMN_USER_RESET_PW_DATE = "reset_pw_date";
	public static final String DATABASE_TABLE_COLUMN_USER_BLOCKED = "blocked";
	public static final String DATABASE_TABLE_COLUMN_USER_BLOCKED_REASON = "blocked_reason";
	public static final String DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION = "email_authentication";
	public static final String DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_UUID = "email_authentication_uuid";
	public static final String DATABASE_TABLE_COLUMN_USER_EMAIL_AUTHENTICATION_DATE = "email_authentication_date";
	public static final String DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_UUID = "auto_login_uuid";
	public static final String DATABASE_TABLE_COLUMN_USER_AUTO_LOGIN_DATE = "auto_login_date";
	/* RHYA.Network Auth Token */
	public static final String DATABASE_TABLE_NAME_AUTH_TOKEN = "auth_token";
	public static final String DATABASE_TABLE_COLUMN_AUTH_TOKEN = "auth_token";
	public static final String DATABASE_TABLE_COLUMN_AUTH_USER = "user_uuid";
	public static final String DATABASE_TABLE_COLUMN_AUTH_NAME = "name";
	public static final String DATABASE_TABLE_COLUMN_AUTH_TOKEN_DATE = "date";
}
