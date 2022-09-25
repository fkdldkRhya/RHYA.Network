package kro.kr.rhya_network.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeChecker {
	// �ð� Ȯ�� �Լ�
	public static boolean isTime_H(String input_date, int hours) throws ParseException {
		// ���� �ð�
		Date get_now_date = new Date();
		// �Էµ� �ð� ���ϱ�
		Calendar calendar = Calendar.getInstance();
		// String to Date ��ȯ
		Date input_date_time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(input_date);
		calendar.setTime(input_date_time);
		// �ð� ���ϱ�
		calendar.add(Calendar.HOUR, hours);
		input_date_time = calendar.getTime();
		// �ð� ��
		if (get_now_date.compareTo(input_date_time) >= 0) {
			// �ð� ����
			return false;
		}else {
			// �ð� ������ ����
			return true;
		}
	}
}
