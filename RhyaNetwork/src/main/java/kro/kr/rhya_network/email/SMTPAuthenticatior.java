package kro.kr.rhya_network.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
 
public class SMTPAuthenticatior extends Authenticator{
	// ���� ��й�ȣ : ;jXFk#D4-M9qHQ$((g]q]CwFrmu/4s{C
	// �� ��й�ȣ : tmtskruzosiyoafu
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("rhya.no.reply.mail", "tmtskruzosiyoafu");
    }
}
