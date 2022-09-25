package kro.kr.rhya_network.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kro.kr.rhya_network.util.PathManager;


public class RhyaLogger {
	// �α� ���� ���� ���
	private final String PATH = PathManager.RHYA_LOGGER_SAVE_PATH;
	// ���� ���� ���� ����
	private final SimpleDateFormat FILEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
	// Ŭ���� �̸�
	public String ClassName;
	// �α� ��� ����
	public Boolean LogConsole;
	public Boolean LogFile;
	// �α� ��� JSP ������ �̸�
	public String JspName;
	// ���� ��� ����
	public SimpleDateFormat Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	// �α� ����
	public enum Type { Info, Warning, Error, Fatal, Debug }
	
	
	// �α� ���ڿ� ����
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
	
	
	// �α� ��� �Լ� - Console
	private void WriteConsoleLogger(Type type, String message) {
		System.out.println(LogStrMaker(type, message));
	}
	
	
	// �α� ��� �Լ� - File
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
	
	
	// �α� ���
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
	
	
	// �α� ���� ���� v1
	public String CreateLogTextv1(String ip, String[] parm, String[] parm_v) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("Ŭ���̾�Ʈ ���� [ OK! ]  �Ķ����: ");
		
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
	
	// �α� ���� ���� v2
	public String CreateLogTextv2(String ip, String url) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("����Ű�� ��ġ���� ���� '");
		log_sb.append(url);
		log_sb.append("'���� �̵� / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v3
	public String CreateLogTextv3(String ip) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("����Ű�� ��ġ���� ����");
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v4
	public String CreateLogTextv4(String ip, String url, String message) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(message);
		log_sb.append(" '");
		log_sb.append(url);
		log_sb.append("'���� �̵� / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v5
	public String CreateLogTextv5(String ip, String message) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(message);
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v6
	public String CreateLogTextv6(String ip, String url) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("�߸��� ���� �� : Null '");
		log_sb.append(url);
		log_sb.append("'���� �̵� / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v7
	public String CreateLogTextv7(String ip) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append("�߸��� ���� �� : Null");
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v8
	public String CreateLogTextv8(String ip, String msg, String msg2) {
		StringBuilder log_sb = new StringBuilder();
		log_sb.append(msg);
		log_sb.append(" ");
		log_sb.append(msg2);
		log_sb.append(" / IP -> ");
		log_sb.append(ip);
		
		return log_sb.toString();
	}
	
	// �α� ���� ���� v9
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
	
	// �α� ���� ���� v10
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
	
	// �α� ���� ���� v11
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
	
	// �α� ���� ���� v10
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
	
	// �α� ���� ���� v13
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
