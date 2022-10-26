package kro.kr.rhya_network.page;

import javax.servlet.http.HttpServletRequest;

public class JspPageInfo {
	// URL 정보
	public static final String ServerURL = "https://rhya-network.kro.kr";

	// 페이지 아이디
	/* RHYA.Network 로그인, 회원가입, 비밀번호 찾기 */
	public static final int PageID_User_Account_Sign_In = 0;
	public static final int PageID_User_Account_Sign_Up = 1;
	public static final int PageID_User_Account_Sign_In_Task = 2;
	public static final int PageID_User_Account_Sign_Up_Task = 3;
	public static final int PageID_User_Account_Sign_Up_Id_Task = 4;
	public static final int PageID_User_Account_ForgotPW = 5;
	public static final int PageID_User_Account_ForgotPW_Input = 6;
	public static final int PageID_User_Account_ForgotPW_Task = 7;
	public static final int PageID_User_Account_ForgotPW_Input_Task = 8;
	public static final int PageID_User_Account_Sign_Up_Email = 9;
	public static final int PageID_User_Account_Sign_In_Callback = 10;
	public static final int PageID_User_Account_Logout = 11;
	/* 메인 페이지 */
	public static final int PageID_Rhya_Network_Main = 12;
	/* RHYA.Network terms */
	public static final int PageID_Rhya_Network_Terms = 13;
	/* RHYA.Network auth token 발급 */
	public static final int PageID_Rhya_Network_Auth_Get_Token = 14;
	public static final int PageID_Rhya_Network_Auth_Check_Token = 15;
	/* RHYA.Network 사용자 계정 정보 변경 */
	public static final int PageID_User_Account_Info_Edit = 16;
	public static final int PageID_User_Account_Info_Edit_Task = 17;
	/* RHYA.Network auth token 정보 가져오기 */
	public static final int PageID_User_Account_Auth_Get_Info_Token = 18;
	/* RHYA.Network utaite player */
	public static final int PageID_Rhya_Utaite_Player = 19;
	/* RHYA.Network 사용자 계정 정보 변경 */
	public static final int PageID_User_Account_Info_Edit_PW_Task = 20;
	/* RHYA.Network 공지사항 */
	public static final int PageID_Rhya_Network_Announcement = 21;
	/* RHYA.Network 페이지 접근 차단 */
	public static final int PageID_Rhya_Network_Page_Blocked = 22;
	/* RHYA.Network 랜덤 애니 사진 */
	public static final int PageID_Random_Anim_Image = 23;
	/* 우타이테 플레이어 이미지 */
	public static final int PageID_Utaite_Player_Get_Image = 24;
	/* 온라인 출석부 이미지 */
	public static final int PageID_Online_Attendance_Image = 25;
	/* 온라인 출석부 계정 동기화 전용 페이지 */
	public static final int PageID_Online_Attendance_Account_Sync = 26;
	public static final int PageID_Online_Attendance_Account_Sync_Email_Task = 27;
	/* RHYA.Network online attendance */
	public static final int PageID_Online_Attendance = 28;
	/* RHYA.Network 서버 정보 페이지 */
	public static final int PageID_Rhya_Network_Server_Info = 29;
	/* RHYA.Network IP 허용 작업 처리 페이지 */
	public static final int PageID_Rhya_IP_Access_Allow = 30;
	/* RHYA.Network 우타이테 플레이어 오폰소스 라이센스 페이지 */
	public static final int PageID_Utaite_Player_Licenses = 31;
	/* RHYA.Network 우타이테 플레이어 노래 신청 페이지 */
	public static final int PageID_Utaite_Player_Song_Add_Manager = 32;
	/* RHYA.Network 우타이테 플레이어 노래 신청 [파일 업로드] 페이지 */
	public static final int PageID_Utaite_Player_Song_Add_Manager_File_Upload = 33;
	/* RHYA.Network 우타이테 플레이어 노래 신청 페이지 [관리자] 작업 */
	public static final int PageID_Utaite_Player_Song_Add_Manager_Admin = 34;
	/* RHYA.Network 우타이테 플레이어 노래 신청 페이지 [관리자] 작업 처리 */
	public static final int PageID_Utaite_Player_Song_Add_Manager_Admin_Task = 35;
	/* RHYA.Network 우타이테 플레이어 이용권 신청 페이지 */
	public static final int PageID_Utaite_Player_Licenses_Application = 36;
	/* RHYA.Network 우타이테 플레이어 이용권 신청 페이지 작업 처리 */
	public static final int PageID_Utaite_Player_Licenses_Application_Task = 37;
	/* RHYA.Network 우타이테 플레이어 이용권 신청 페이지 [관리자] 작업 처리 */
	public static final int PageID_Utaite_Player_Licenses_Application_Admin_Task = 38;
	/* RHYA.Network 프로그램 다운로더 */
	public static final int PageID_Other_Service_Downloader = 39;
	/* RHYA.Network 프로그램 다운로더 */
	public static final int PageID_Rhya_Network_VPN_Access_Manager = 40;
	/* RHYA.Network 사용자 계정을 모든 기기에서 로그아웃 작업 진행 */
	public static final int PageID_Rhya_Network_Logout_All_Edit = 41;
	/* RHYA.Network Auth Token을 이용한 사용자 권한 확인 */
	public static final int PageID_Rhya_Network_Auth_Token_For_Check_User_Permission = 42;
	/* RHYA.Network 관리자 도구 - RHYA.Network Web server background main image manager */
	public static final int PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager = 43;
	/* RHYA.Network 관리자 도구 - RHYA.Network Web server announcement editor */
	public static final int PageID_Rhya_Network_Admin_Tool_Announcement_Editor = 44;
	
	// 오류 페이지 경로
	/* HTTP 400 에러 */
	public static final String ERROR_PAGE_PATH_HTTP_400 = "/RhyaNetwork/webpage/error_page_400.html";
	/* HTTP 403 에러 */
	public static final String ERROR_PAGE_PATH_HTTP_403 = "/RhyaNetwork/webpage/error_page_403.html";
	/* HTTP 404 에러 */
	public static final String ERROR_PAGE_PATH_HTTP_404 = "/RhyaNetwork/webpage/error_page_404.html";
	/* HTTP 408 에러 */
	public static final String ERROR_PAGE_PATH_HTTP_408 = "/RhyaNetwork/webpage/error_page_408.html";
	/* HTTP 500 에러 */
	public static final String ERROR_PAGE_PATH_HTTP_500 = "/RhyaNetwork/webpage/error_page_500.html";
	
	
	
	// 페이지 상태 관리 함수
	public static boolean JspPageStateManager(int Istate) {
		if (Istate == 0) {
			// 허용
			return true;
		}else if (Istate == 1) {
			// 접근 거부
			return false;
		}else {
			return true;
		}
	}
	
	
	
	// 페이지 URL 가지고 오는 함수
	public static String GetJspPageURL(HttpServletRequest req, int id) {
		StringBuilder sb = new StringBuilder();
		
		if (id == PageID_User_Account_Sign_In) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_in.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_Up) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_up.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_In_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_in_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_Up_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_up_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_Up_Email) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_up_email.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_Up_Id_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/sign_up_id_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_ForgotPW) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/forgot_pwd.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_ForgotPW_Input) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/forgot_pwd_input.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_ForgotPW_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/forgot_pwd_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_ForgotPW_Input_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/forgot_pwd_input_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Main) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/main/rhya_network.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Terms) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/main/rhya_network_pp.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Auth_Get_Token) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/auth_token.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Auth_Check_Token) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/auth_token_checker.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Info_Edit) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/edit_my_account.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Sign_In_Callback) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/callback.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Logout) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/logout.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Info_Edit_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/edit_my_account_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Auth_Get_Info_Token) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/auth_info.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Utaite_Player) {
			sb.append(req.getContextPath());
			sb.append("/utaite_player_manager");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_User_Account_Info_Edit_PW_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/edit_my_account_pwd_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Announcement) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/main/rhya_network_announcement.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Page_Blocked) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/main/rhya_network_block.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Random_Anim_Image) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/utils/anim_image_random.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Online_Attendance_Image) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/utils/online_attendance_image.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Online_Attendance_Account_Sync) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/online_attendance_account_sync.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Online_Attendance_Account_Sync_Email_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/online_attendance_account_sync_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Server_Info) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/server_info.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_IP_Access_Allow) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/ip_allow.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Licenses) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_open_sources_licenses.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Song_Add_Manager) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_song_add.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Song_Add_Manager_File_Upload) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_song_add_upload.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Song_Add_Manager_Admin) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_song_add_admin.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Song_Add_Manager_Admin_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_song_add_admin_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Licenses_Application) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_licenses_application.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Licenses_Application_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_licenses_application_task.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Utaite_Player_Licenses_Application_Admin_Task) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/service/utaite_player_licenses_application_admin.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Other_Service_Downloader) {
			sb.append(req.getContextPath());
			sb.append("/other_service_download");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_VPN_Access_Manager) {
			sb.append(req.getContextPath());
			sb.append("/vpn_access_manager");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Logout_All_Edit) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/edit_my_account_logout.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Auth_Token_For_Check_User_Permission) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/auth.v1/auth_permission_checker.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Admin_Tool_Server_Background_Image_Manager) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/admin/lv1/server_image_manager.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}else if (id == PageID_Rhya_Network_Admin_Tool_Announcement_Editor) {
			sb.append(req.getContextPath());
			sb.append("/webpage/jsp/admin/lv2/server_announcement_editor.jsp");
			String Url = sb.toString();
			sb = null;
			return Url;
		}

		return null;
	}
	
	
	
	// 페이지 Ajax 통신 결과 출력 함수
	public static String GetAjaxResult(String[] result, String split) {
		StringBuilder sb = new StringBuilder();
		sb.append(split);
		
		for (int i = 0 ; i < result.length; i ++) {
			sb.append(result[i]);
			sb.append(split);
		}
		
		String returnT = sb.toString();
		sb = null;
		return returnT;
	}
}
