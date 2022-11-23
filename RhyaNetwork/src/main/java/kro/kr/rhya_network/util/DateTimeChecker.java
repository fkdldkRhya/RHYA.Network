package kro.kr.rhya_network.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeChecker {
	// 시간 확인 함수
	public static boolean isTime_H(String input_date, int hours) throws ParseException {
		// 현재 시간
		Date get_now_date = new Date();
		// 입력된 시간 더하기
		Calendar calendar = Calendar.getInstance();
		// String to Date 변환
		Date input_date_time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(input_date);
		calendar.setTime(input_date_time);
		// 시간 더하기
		calendar.add(Calendar.HOUR, hours);
		input_date_time = calendar.getTime();
		// 시간 비교
		if (get_now_date.compareTo(input_date_time) >= 0) {
			// 시간 지남
			return false;
		}else {
			// 시간 지나지 않음
			return true;
		}
	}
}
