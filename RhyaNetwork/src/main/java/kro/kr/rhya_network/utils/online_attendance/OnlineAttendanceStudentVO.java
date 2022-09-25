package kro.kr.rhya_network.utils.online_attendance;

public class OnlineAttendanceStudentVO {
	// �л� ���� UUID
	private String uuid;
	// �л� �� ���� UUID
	private String class_uuid;
	// �л� ��ȣ
	private int number;
	// �л� �̸�
	private String name;
	// �л� �̹��� URL [ Auth Token �ʿ� ]
	private String image;
	// �л� ����
	private int gender;
	// �л� ���� ����
	private int move_out;
	// �⵵
	private int year;
	// �л� ��Ÿ ����
	private String note;
	// �л� ������ ���� ����
	private int version;
	
	
	
	
	/**
	 * ������
	 * 
	 * @param uuid �л� UUID
	 * @param class_uuid �л� �� UUID
	 * @param number �л� ��ȣ
	 * @param name �л� �̸�
	 * @param image �л� �̹���
	 * @param gender �л� ����
	 * @param move_out ���� ����
	 * @param year �⵵
	 * @param note ��Ÿ ����
	 * @param version ������ ���� ���� ����
	 */
	public OnlineAttendanceStudentVO(String uuid, String class_uuid, int number, String name, String image, int gender,
									 int move_out, int year, String note, int version) {
		this.uuid = uuid;
		this.class_uuid = class_uuid;
		this.number = number;
		this.name = name;
		this.image = image;
		this.gender = gender;
		this.move_out = move_out;
		this.year = year;
		this.note = note;
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
	 * @return the class_uuid
	 */
	public String getClass_uuid() {
		return class_uuid;
	}



	/**
	 * @param class_uuid the class_uuid to set
	 */
	public void setClass_uuid(String class_uuid) {
		this.class_uuid = class_uuid;
	}



	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}



	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
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
	 * @return the gender
	 */
	public int getGender() {
		return gender;
	}



	/**
	 * @param gender the gender to set
	 */
	public void setGender(int gender) {
		this.gender = gender;
	}



	/**
	 * @return the move_out
	 */
	public int getMove_out() {
		return move_out;
	}



	/**
	 * @param move_out the move_out to set
	 */
	public void setMove_out(int move_out) {
		this.move_out = move_out;
	}



	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}



	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}



	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}



	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
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
