package kro.kr.rhya_network.utils.online_attendance;

public class OnlineAttendanceTeacherVO {
	// 선생님 구분 UUID
	private String uuid;
	// 선생님 이름
	private String name;
	// 선생님 이름 [ 중복 방지 이름 ]
	private String name2;
	// 선생님 이미지 URL [ Auth Token 필요 ]
	private String image;
	// 선생님 설명
	private String description;
	// 선생님 소속 부서 UUID
	private String department1;
	private String department2;
	// 선생님 이메일 주소
	private String email;
	// 선생님 휴대전화 전화번호
	private String mobile_phone;
	// 선생님 부서 내선 전화번호
	private String office_phone;
	// 선생님 역할
	private String position;
	// 선생님 담당 과목
	private String subject;
	// 선생님 소속 학교
	private int school_id;
	// 선생님 데이터 변경 감지
	private int version;
	
	
	
	
	/**
	 * 생성자 
	 * 
	 * @param uuid 선생님 UUID
	 * @param name 선생님 이름
	 * @param name2 선생님 이름 [ 중복 방지 이름 ]
	 * @param image 선생님 이미지 URL
	 * @param description 선생님 설명
	 * @param department1 선생님 소속 부서 UUID
	 * @param department2 선생님 소속 부서 UUID
	 * @param email 선생님 이메일 주소
	 * @param mobile_phone 선생님 휴대전화 전화번호
	 * @param office_phone 선생님 부서 내선 전화번호
	 * @param position 선생님 역할
	 * @param subject 선생님 담당 과목
	 * @param school_id 선생님 소속 학교
	 * @param version 선생님 데이터 변경 감지 버전
	 */
	public OnlineAttendanceTeacherVO(String uuid, String name, String name2, String image, String description,
			String department1, String department2, String email, String mobile_phone, String office_phone,
			String position, String subject, int school_id, int version) {
		
		this.uuid = uuid;
		this.name = name;
		this.name2 = name2;
		this.image = image;
		this.description = description;
		this.department1 = department1;
		this.department2 = department2;
		this.email = email;
		this.mobile_phone = mobile_phone;
		this.office_phone = office_phone;
		this.position = position;
		this.subject = subject;
		this.school_id = school_id;
		this.version = version;
	}




	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}




	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}




	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}




	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}




	/**
	 * @return the name2
	 */
	public String getName2() {
		return name2;
	}




	/**
	 * @param name2 the name2 to set
	 */
	public void setName2(String name2) {
		this.name2 = name2;
	}




	/**
	 * @return the image
	 */
	public String getImage() {
		return image;
	}




	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}




	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}




	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}




	/**
	 * @return the department1
	 */
	public String getDepartment1() {
		return department1;
	}




	/**
	 * @param department1 the department1 to set
	 */
	public void setDepartment1(String department1) {
		this.department1 = department1;
	}




	/**
	 * @return the department2
	 */
	public String getDepartment2() {
		return department2;
	}




	/**
	 * @param department2 the department2 to set
	 */
	public void setDepartment2(String department2) {
		this.department2 = department2;
	}




	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}




	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}




	/**
	 * @return the mobile_phone
	 */
	public String getMobile_phone() {
		return mobile_phone;
	}




	/**
	 * @param mobile_phone the mobile_phone to set
	 */
	public void setMobile_phone(String mobile_phone) {
		this.mobile_phone = mobile_phone;
	}




	/**
	 * @return the office_phone
	 */
	public String getOffice_phone() {
		return office_phone;
	}




	/**
	 * @param office_phone the office_phone to set
	 */
	public void setOffice_phone(String office_phone) {
		this.office_phone = office_phone;
	}




	/**
	 * @return the position
	 */
	public String getPosition() {
		return position;
	}




	/**
	 * @param position the position to set
	 */
	public void setPosition(String position) {
		this.position = position;
	}




	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}




	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}




	/**
	 * @return the school_id
	 */
	public int getSchool_id() {
		return school_id;
	}




	/**
	 * @param school_id the school_id to set
	 */
	public void setSchool_id(int school_id) {
		this.school_id = school_id;
	}




	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}




	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
}
