package kro.kr.rhya_network.email;

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmail {
	// SMTP ���� �� �̸��� �߼� ����
	public final String SEND_EMAIL_ADDRESS = "rhya.no.reply.mail@gmail.com";
	public final String SMTP_SERVER_INFO_HOST = "smtp.gmail.com";
	public final String SMTP_SERVER_INFO_PORT = "465";
	public final String SMTP_SERVER_INFO_STARTTLS = "true";
	public final String SMTP_SERVER_INFO_AUTH = "true";
	public final String SMTP_SERVER_INFO_DEBUG = "true";
	public final String SMTP_SERVER_INFO_SSL_TRUST = "smtp.gmail.com";
	public final String SMTP_SERVER_INFO_SOCKETF_PORT = "465";
	public final String SMTP_SERVER_INFO_SOCKETF_CLASS = "javax.net.ssl.SSLSocketFactory";
	public final String SMTP_SERVER_INFO_SOCKETF_FALLBACK = "false";
	
	
	
	// ���� ���� ����
	public Properties GetProperties() {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		System.setProperty("jsse.enableSNIExtension", "false");
		
		Properties properties = new Properties();
		properties.put("mail.smtp.host", SMTP_SERVER_INFO_HOST);
		properties.put("mail.smtp.port", SMTP_SERVER_INFO_PORT);
		properties.put("mail.smtp.starttls.enable", SMTP_SERVER_INFO_STARTTLS);
		properties.put("mail.smtp.auth", SMTP_SERVER_INFO_AUTH);
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.ssl.trust", SMTP_SERVER_INFO_SSL_TRUST);
		properties.put("mail.smtp.debug", SMTP_SERVER_INFO_DEBUG);
		properties.put("mail.smtp.socketFactory.port", SMTP_SERVER_INFO_SOCKETF_PORT);
		properties.put("mail.smtp.socketFactory.class", SMTP_SERVER_INFO_SOCKETF_CLASS);
		properties.put("mail.smtp.socketFactory.fallback", SMTP_SERVER_INFO_SOCKETF_FALLBACK);
		return properties;
	}
	
	
	// ���� ����
	public void Send(Properties Iproperties, String html, String title, String from_addr) throws MessagingException {
		Properties properties = Iproperties;
		Authenticator auth = new SMTPAuthenticatior();
	    Session ses = Session.getInstance(properties, auth);
	    ses.setDebug(true);
	    MimeMessage msg = new MimeMessage(ses); // ������ ������ ���� ��ü
	    msg.setSubject(title); // ����
	    Address fromAddr = new InternetAddress(SEND_EMAIL_ADDRESS);
	    msg.setFrom(fromAddr); // ������ ���
	    Address toAddr = new InternetAddress(from_addr);
	    msg.addRecipient(Message.RecipientType.TO, toAddr); // �޴� ���
	    msg.setContent(html, "text/html;charset=UTF-8"); // ����� ���ڵ�
	    Transport.send(msg); // ����
	}
}
