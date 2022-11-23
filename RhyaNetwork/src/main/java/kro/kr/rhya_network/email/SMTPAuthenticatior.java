package kro.kr.rhya_network.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
 
public class SMTPAuthenticatior extends Authenticator{
	// 계정 비밀번호 : ;jXFk#D4-M9qHQ$((g]q]CwFrmu/4s{C
	// 앱 비밀번호 : tmtskruzosiyoafu
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("rhya.no.reply.mail", "tmtskruzosiyoafu");
    }
}
