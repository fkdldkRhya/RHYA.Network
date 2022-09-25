package kro.kr.rhya_network.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kro.kr.rhya_network.util.PathManager;


public class RhyaLogger {
	// 로그 파일 생성 경로
	private final String PATH = PathManager.RHYA_LOGGER_SAVE_PATH;
	// 파일 생성 날자 형식
	private final SimpleDateFormat FILEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
	// 클레스 이름
	public String ClassName;
	// 로그 출력 형식
	public Boolean LogConsole;
	public Boolean LogFile;
	// 로그 출력 JSP 페이지 이름
	public String JspName;
	// 날자 출력 형식
	public SimpleDateFormat Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	// 로그 종류
	public enum Type { Info, Warning, Error, Fatal, Debug }
	
	
	// 로그 문자열 생성
	private String LogStrMaker(Type type, String message) {
		String className = this.getClass().getName();
		if (ClassName != null) {
			className = ClassName;
		}
		String result = String.format("%-20s %-10s [%s(%s)] ~$  %s", Format.format(new Date()).toString(),
															   		  type.toString().toUpperCase(),
															          JspName,
															          className,
															          message);
		return result;
	}
	
	
	// 로그 출력 함수 - Console
	private void WriteConsoleLogger(Type type, String message) {
		System.out.println(LogStrMaker(type, message));
	}
	
	
	// 로그 출력 함수 - File
	private void WriteFileLogger(Type type, String message) throws IOException {
		final String fileName = "jsp-log-";
		String nowDate = FILEFORMAT.format(new Date());
		StringBuilder sb = new StringBuilder();
		sb.append(PATH);
		sb.append(File.separator);
		sb.append(fileName);
		sb.append(nowDate);
		sb.append(".log");
		
		String fileFullName = sb.toString();
		sb.delete(0,sb.length());
		sb.append(LogStrMaker(type, message));
		sb.append(System.lineSeparator());
		FileWriter fw = new FileWriter(fileFullName, true);
		fw.write(sb.toString());
		fw.close();
		sb = null;
	}
	
	
	// 로그 출력
	public void Log(Type type, String message) {
		if (LogConsole) {
			WriteConsoleLogger(type, message);
		}
		
		if (LogFile) {
			try {
				WriteFileLogger(type, message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	// 로그 내용 생성 v1
	public String CreateLogTextv1(String ip, String[] parm, String[] parm_v) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("클라이언트 접속 [ OK! ]  파라미터: ");
		
		for (int i = 0; i < parm.length; i ++) {
			log_sb.append(parm[i]);
			log_sb.append("(");
			log_sb.append(parm_v[i]);
			log_sb.append("),");
		}
		
		log_sb.append(" IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v2
	public String CreateLogTextv2(String ip, String url) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("인증키가 일치하지 않음 '");
		log_sb.append(url);
		log_sb.append("'으로 이동 / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v3
	public String CreateLogTextv3(String ip) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("인증키가 일치하지 않음");
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v4
	public String CreateLogTextv4(String ip, String url, String message) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(message);
		log_sb.append(" '");
		log_sb.append(url);
		log_sb.append("'으로 이동 / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v5
	public String CreateLogTextv5(String ip, String message) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(message);
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v6
	public String CreateLogTextv6(String ip, String url) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("잘못된 인자 값 : Null '");
		log_sb.append(url);
		log_sb.append("'으로 이동 / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v7
	public String CreateLogTextv7(String ip) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("잘못된 인자 값 : Null");
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v8
	public String CreateLogTextv8(String ip, String msg, String msg2) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v9
	public String CreateLogTextv9(String ip, String msg, String msg2, String msg3, String msg4) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" ");
		log_sb.append(msg3);
		log_sb.append(" ");
		log_sb.append(msg4);
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v10
	public String CreateLogTextv10(String ip, String msg, String msg2, String msg3, String msg4, String msg5) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" ");
		log_sb.append(msg3);
		log_sb.append(" ");
		log_sb.append(msg4);
		log_sb.append(" [ ");
		log_sb.append(msg5);
		log_sb.append(" ] / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v11
	public String CreateLogTextv11(String ip, String msg, String msg2, String msg3, String msg4, String msg5, String msg6, String msg7, String msg8, String msg9, String msg10) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" ");
		log_sb.append(msg3);
		log_sb.append(" ");
		log_sb.append(msg4);
		log_sb.append(" ");
		log_sb.append(msg5);
		log_sb.append(" ");
		log_sb.append(msg6);
		log_sb.append(" ");
		log_sb.append(msg7);;
		log_sb.append(" ");
		log_sb.append(msg8);
		log_sb.append(" ");
		log_sb.append(msg9);
		log_sb.append(" [ ");
		log_sb.append(msg10);
		log_sb.append(" ] / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v10
	public String CreateLogTextv12(String ip, String msg, String msg2, String msg3, String msg4, String msg5, String msg6) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" ");
		log_sb.append(msg3);
		log_sb.append(" ");
		log_sb.append(msg4);
		log_sb.append(" ");
		log_sb.append(msg5);
		log_sb.append(" [ ");
		log_sb.append(msg6);
		log_sb.append(" ] / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// 로그 내용 생성 v13
	public String CreateLogTextv13(String ip, String msg, String msg2, String msg3, String msg4, String msg5, String msg6, String msg7, String msg8, String msg9, String msg10, String msg11) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" ");
		log_sb.append(msg3);
		log_sb.append(" ");
		log_sb.append(msg4);
		log_sb.append(" ");
		log_sb.append(msg5);
		log_sb.append(" ");
		log_sb.append(msg6);
		log_sb.append(" ");
		log_sb.append(msg7);;
		log_sb.append(" ");
		log_sb.append(msg8);
		log_sb.append(" ");
		log_sb.append(msg9);
		log_sb.append(" ");
		log_sb.append(msg10);
		log_sb.append(" [ ");
		log_sb.append(msg11);
		log_sb.append(" ] / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
}
