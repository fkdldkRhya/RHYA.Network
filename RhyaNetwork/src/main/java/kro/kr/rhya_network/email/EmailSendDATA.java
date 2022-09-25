package kro.kr.rhya_network.email;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import kro.kr.rhya_network.page.JspPageInfo;
import kro.kr.rhya_network.page.PageParameter;
import kro.kr.rhya_network.security.RhyaAES;
import kro.kr.rhya_network.security.URLFilter;

public class EmailSendDATA {
	public static final String ADMIN_EMAIL = "sihun.choi@email.rhya-network.kro.kr";
	
	
	
	// 계정 활성화
	public static class ActivateAccount {
		// 이메일 HTML
		public final String html_text = "<!DOCTYPE html>\r\n"
				+ "\r\n"
				+ "<html lang=\"en\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\r\n"
				+ "<head>\r\n"
				+ "<title></title>\r\n"
				+ "<meta charset=\"utf-8\"/>\r\n"
				+ "<meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\"/>\r\n"
				+ "<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml><![endif]-->\r\n"
				+ "<style>\r\n"
				+ "		* {\r\n"
				+ "			box-sizing: border-box;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		body {\r\n"
				+ "			margin: 0;\r\n"
				+ "			padding: 0;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		a[x-apple-data-detectors] {\r\n"
				+ "			color: inherit !important;\r\n"
				+ "			text-decoration: inherit !important;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		#MessageViewBody a {\r\n"
				+ "			color: inherit;\r\n"
				+ "			text-decoration: none;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		p {\r\n"
				+ "			line-height: inherit\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		@media (max-width:700px) {\r\n"
				+ "			.icons-inner {\r\n"
				+ "				text-align: center;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.icons-inner td {\r\n"
				+ "				margin: 0 auto;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.fullMobileWidth,\r\n"
				+ "			.row-content {\r\n"
				+ "				width: 100% !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.image_block img.big {\r\n"
				+ "				width: auto !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.stack .column {\r\n"
				+ "				width: 100%;\r\n"
				+ "				display: block;\r\n"
				+ "			}\r\n"
				+ "		}\r\n"
				+ "	</style>\r\n"
				+ "</head>\r\n"
				+ "<body style=\"background-color: #202020; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"nl-container\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #202020;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-1\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"image_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;padding-right:20px;padding-left:20px;padding-top:70px;\">\r\n"
				+ "<div style=\"line-height:10px\"><img class=\"fullMobileWidth big\" src=\"https://rhya-network.kro.kr/RhyaNetwork/webpage/resources/icon/RNLogoForWhite_x400.png\" style=\"display: block; height: auto; border: 0; width: 238px; max-width: 100%;\" width=\"238\"/></div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 27px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#TITLE#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-2\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-bottom:35px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\"><span style=\"font-size:16px;\"><strong>이 메일은 발신 전용 메일 입니다. 발신 주소로 회신하지 마십시오. 또한 해당 메일은 개인의 민감한 정보가 포함되어있을 수 있습니다. 타인에게 노출되지 않도록 주의해주십시오.</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-3\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #141414; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:55px;padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #29a2ff; font-size: 28px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#ID#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\">#MESSAGE#</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"button_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;padding-top:10px;padding-right:10px;padding-bottom:50px;padding-left:10px;\">\r\n"
				+ "<div align=\"center\">\r\n"
				+ "<!--[if mso]><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"#URL#\" style=\"height:60px;width:205px;v-text-anchor:middle;\" arcsize=\"7%\" stroke=\"false\" fillcolor=\"#29a2ff\"><w:anchorlock/><v:textbox inset=\"0px,0px,0px,0px\"><center style=\"color:#ffffff; font-family:Arial, sans-serif; font-size:20px\"><![endif]--><a href=\"#URL#\" style=\"text-decoration:none;display:inline-block;color:#ffffff;background-color:#29a2ff;border-radius:4px;width:auto;border-top:1px solid #29a2ff;border-right:1px solid #29a2ff;border-bottom:1px solid #29a2ff;border-left:1px solid #29a2ff;padding-top:10px;padding-bottom:10px;font-family:Arial, Helvetica Neue, Helvetica, sans-serif;text-align:center;mso-border-alt:none;word-break:keep-all;\" target=\"_blank\"><span style=\"padding-left:50px;padding-right:50px;font-size:20px;display:inline-block;letter-spacing:normal;\"><span style=\"font-size: 12px; line-height: 2; word-break: break-word; mso-line-height-alt: 24px;\"><span data-mce-style=\"font-size: 20px; line-height: 40px;\" style=\"font-size: 20px; line-height: 40px;\"><strong>#BUTTON#</strong></span></span></span></a>\r\n"
				+ "<!--[if mso]></center></v:textbox></v:roundrect><![endif]-->\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-4\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<div class=\"spacer_block\" style=\"height:40px;line-height:40px;font-size:1px;\"> </div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-5\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>If this wasn't you</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-left:20px;padding-top:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0;\"><span style=\"font-size:16px;\"><strong>#NO_USER_REQUEST#</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-6\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;padding-top:40px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>Get in touch</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; letter-spacing: -1px;\"><span style=\"font-size:16px;\"><strong>RHYA.Network Dev Team, sihun.choi@email.rhya-network.kro.kr</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-7\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:90px;padding-right:20px;padding-bottom:10px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\">Copyright 2022 RHYA.Network. All rights reserved.</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-8\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"icons_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"color:#9d9d9d;font-family:inherit;font-size:15px;padding-bottom:5px;padding-top:5px;text-align:center;\">\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;\">\r\n"
				+ "<!--[if vml]><table align=\"left\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;\"><![endif]-->\r\n"
				+ "<!--[if !vml]><!-->\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" class=\"icons-inner\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; margin-right: -4px; padding-left: 0px; padding-right: 0px;\">\r\n"
				+ "<!--<![endif]-->\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-9\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table><!-- End -->\r\n"
				+ "</body>\r\n"
				+ "</html>";

		// 이메일 HTML custom tag
		private final String html_tag_title = "#TITLE#";
		private final String html_tag_id = "#ID#";
		private final String html_tag_message = "#MESSAGE#";
		private final String html_tag_url = "#URL#";
		private final String html_tag_button = "#BUTTON#";
		private final String html_tag_nurm = "#NO_USER_REQUEST#";
		
		// 문장 생성 StringBuilder
		private StringBuilder sb;
		
		// 이메일 Title 생성
		public String Title (String id) {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			sb.delete(0,sb.length());
			// 문장 생성
			sb.append("'");
			sb.append(id);
			sb.append("' 님! RHYA.Network 계정을 활성화해주세요.");
			
			return sb.toString();
		}
		
		// 이메일 HTML 생성
		public String Html (String id, String url) throws FileNotFoundException {
			String htmlc = html_text;
			// HTML 커스텀 태그 변환
			htmlc = htmlc.replaceAll(html_tag_id, id);
			htmlc = htmlc.replaceAll(html_tag_url, url);
			htmlc = htmlc.replaceAll(html_tag_title, "RHYA.Network에 가입을 환영합니다.");
			htmlc = htmlc.replaceAll(html_tag_button, "계정 활성화");
			htmlc = htmlc.replaceAll(html_tag_message, "<span style=\"font-size:16px;\"><strong>해당 메일은 RHYA,Network 통합 계정 활성화를 위해 회원가입 후 즉시 발송 되는 메일 입니다. </strong></span><span style=\"font-size:16px;\"><strong>아레 있는 계정 활성화 버튼의 만료 기간은 24시간 입니다. 이후에는 해당 버튼을 통해 계정을 활성화할 수 없습니다. 만약 24시간 안에 계정을 활성화하지 못했다면 아래에 있는 메일주소로 문의하여주시길 바랍니다.</strong></span>");
			htmlc = htmlc.replaceAll(html_tag_nurm, "해당 메일을 본인이 요청한 것이 아니라면 즉시 삭제하거나 아레 있는 관리자 메일 주소로 이 이메일의 사진 사본과 함께 연락 주십시오.");

			return htmlc;
		}
		
		// URL 생성
		public String Url (HttpServletRequest req, String u_uuid, String e_uuid, int red_page, int create_token) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			// URL 파라미터 조합
			URLFilter urlFilter = new URLFilter();
			sb.append(JspPageInfo.ServerURL);
			sb.append(JspPageInfo.GetJspPageURL(req, 9));
			sb.append("?u_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(u_uuid)));
			sb.append("&e_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(e_uuid)));
			sb.append("&");
			sb.append(PageParameter.REDIRECT_PAGE_ID_PARM);
			sb.append("=");
			sb.append(red_page);
			sb.append("&");
			sb.append(PageParameter.IS_CREATE_TOKEN_PARM);
			sb.append("=");
			sb.append(create_token);
			urlFilter = null;
			
			return sb.toString();
		}
	}
	
	
	// 비밀번호 초기화
	public static class ForgotPassword {
		// 이메일 HTML
		public final String html_text = "<!DOCTYPE html>\r\n"
				+ "\r\n"
				+ "<html lang=\"en\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\r\n"
				+ "<head>\r\n"
				+ "<title></title>\r\n"
				+ "<meta charset=\"utf-8\"/>\r\n"
				+ "<meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\"/>\r\n"
				+ "<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml><![endif]-->\r\n"
				+ "<style>\r\n"
				+ "		* {\r\n"
				+ "			box-sizing: border-box;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		body {\r\n"
				+ "			margin: 0;\r\n"
				+ "			padding: 0;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		a[x-apple-data-detectors] {\r\n"
				+ "			color: inherit !important;\r\n"
				+ "			text-decoration: inherit !important;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		#MessageViewBody a {\r\n"
				+ "			color: inherit;\r\n"
				+ "			text-decoration: none;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		p {\r\n"
				+ "			line-height: inherit\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		@media (max-width:700px) {\r\n"
				+ "			.icons-inner {\r\n"
				+ "				text-align: center;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.icons-inner td {\r\n"
				+ "				margin: 0 auto;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.fullMobileWidth,\r\n"
				+ "			.row-content {\r\n"
				+ "				width: 100% !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.image_block img.big {\r\n"
				+ "				width: auto !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.stack .column {\r\n"
				+ "				width: 100%;\r\n"
				+ "				display: block;\r\n"
				+ "			}\r\n"
				+ "		}\r\n"
				+ "	</style>\r\n"
				+ "</head>\r\n"
				+ "<body style=\"background-color: #202020; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"nl-container\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #202020;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-1\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"image_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;padding-right:20px;padding-left:20px;padding-top:70px;\">\r\n"
				+ "<div style=\"line-height:10px\"><img class=\"fullMobileWidth big\" src=\"https://rhya-network.kro.kr/RhyaNetwork/webpage/resources/icon/RNLogoForWhite_x400.png\" style=\"display: block; height: auto; border: 0; width: 238px; max-width: 100%;\" width=\"238\"/></div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 27px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#TITLE#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-2\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-bottom:35px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\"><span style=\"font-size:16px;\"><strong>이 메일은 발신 전용 메일 입니다. 발신 주소로 회신하지 마십시오. 또한 해당 메일은 개인의 민감한 정보가 포함되어있을 수 있습니다. 타인에게 노출되지 않도록 주의해주십시오.</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-3\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #141414; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:55px;padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #29a2ff; font-size: 28px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#ID#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\">#MESSAGE#</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"button_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;padding-top:10px;padding-right:10px;padding-bottom:50px;padding-left:10px;\">\r\n"
				+ "<div align=\"center\">\r\n"
				+ "<!--[if mso]><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"#URL#\" style=\"height:60px;width:205px;v-text-anchor:middle;\" arcsize=\"7%\" stroke=\"false\" fillcolor=\"#29a2ff\"><w:anchorlock/><v:textbox inset=\"0px,0px,0px,0px\"><center style=\"color:#ffffff; font-family:Arial, sans-serif; font-size:20px\"><![endif]--><a href=\"#URL#\" style=\"text-decoration:none;display:inline-block;color:#ffffff;background-color:#29a2ff;border-radius:4px;width:auto;border-top:1px solid #29a2ff;border-right:1px solid #29a2ff;border-bottom:1px solid #29a2ff;border-left:1px solid #29a2ff;padding-top:10px;padding-bottom:10px;font-family:Arial, Helvetica Neue, Helvetica, sans-serif;text-align:center;mso-border-alt:none;word-break:keep-all;\" target=\"_blank\"><span style=\"padding-left:50px;padding-right:50px;font-size:20px;display:inline-block;letter-spacing:normal;\"><span style=\"font-size: 12px; line-height: 2; word-break: break-word; mso-line-height-alt: 24px;\"><span data-mce-style=\"font-size: 20px; line-height: 40px;\" style=\"font-size: 20px; line-height: 40px;\"><strong>#BUTTON#</strong></span></span></span></a>\r\n"
				+ "<!--[if mso]></center></v:textbox></v:roundrect><![endif]-->\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-4\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<div class=\"spacer_block\" style=\"height:40px;line-height:40px;font-size:1px;\"> </div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-5\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>If this wasn't you</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-left:20px;padding-top:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0;\"><span style=\"font-size:16px;\"><strong>#NO_USER_REQUEST#</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-6\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;padding-top:40px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>Get in touch</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; letter-spacing: -1px;\"><span style=\"font-size:16px;\"><strong>RHYA.Network Dev Team, sihun.choi@email.rhya-network.kro.kr</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-7\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:90px;padding-right:20px;padding-bottom:10px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\">Copyright 2022 RHYA.Network. All rights reserved.</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-8\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"icons_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"color:#9d9d9d;font-family:inherit;font-size:15px;padding-bottom:5px;padding-top:5px;text-align:center;\">\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;\">\r\n"
				+ "<!--[if vml]><table align=\"left\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;\"><![endif]-->\r\n"
				+ "<!--[if !vml]><!-->\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" class=\"icons-inner\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; margin-right: -4px; padding-left: 0px; padding-right: 0px;\">\r\n"
				+ "<!--<![endif]-->\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-9\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table><!-- End -->\r\n"
				+ "</body>\r\n"
				+ "</html>";

		// 이메일 HTML custom tag
		private final String html_tag_title = "#TITLE#";
		private final String html_tag_id = "#ID#";
		private final String html_tag_message = "#MESSAGE#";
		private final String html_tag_url = "#URL#";
		private final String html_tag_button = "#BUTTON#";
		private final String html_tag_nurm = "#NO_USER_REQUEST#";
		
		// 문장 생성 StringBuilder
		private StringBuilder sb;

		// 이메일 Title 생성
		public String Title (String id) {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			sb.delete(0,sb.length());
			// 문장 생성
			sb.append("'");
			sb.append(id);
			sb.append("' 님! RHYA.Network 계정 비밀번호 초기화 매일 입니다");
			
			return sb.toString();
		}
		
		// 이메일 HTML 생성
		public String Html (String id, String url) throws FileNotFoundException {
			String htmlc = html_text;
			// HTML 커스텀 태그 변환
			htmlc = htmlc.replaceAll(html_tag_id, id);
			htmlc = htmlc.replaceAll(html_tag_url, url);
			htmlc = htmlc.replaceAll(html_tag_button, "비밀번호 초기화");
			htmlc = htmlc.replaceAll(html_tag_title, "RHYA.Network 계정 비밀번호 변경");
			htmlc = htmlc.replaceAll(html_tag_message, "<span style=\"font-size:16px;\"><strong>회원님이 요청한 비밀번호 재설정 메일입니다. 아래 버튼을 통해 비밀번호를 재설정 할 수 있습니다. 이 메일은 발송되고 약 5시간 동안만 유지됩니다.</strong></span>");
			htmlc = htmlc.replaceAll(html_tag_nurm, "이 메일은 회원님의 이메일 주소, 이름, 아이디가 회원님의 계정과 일치했기 때문에 발송된 메일입니다. 메일을 본인이 요청한 것이 아니라면 즉시 삭제하거나 아래 있는 관리자 메일 주소로 이 이메일의 사진 사본과 함께 연락해 주십시오. 또한 회원님의 계정 정보가 유출된 것일 수도 있습니다.");

			return htmlc;
		}
		
		// URL 생성
		public String Url (HttpServletRequest req, String u_uuid, String e_uuid, int red_page, int create_token) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			// URL 파라미터 조합
			URLFilter urlFilter = new URLFilter();
			sb.append(JspPageInfo.ServerURL);
			sb.append(JspPageInfo.GetJspPageURL(req, 6));
			sb.append("?u_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(u_uuid)));
			sb.append("&e_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(e_uuid)));
			sb.append("&");
			sb.append(PageParameter.REDIRECT_PAGE_ID_PARM);
			sb.append("=");
			sb.append(red_page);
			sb.append("&");
			sb.append(PageParameter.IS_CREATE_TOKEN_PARM);
			sb.append("=");
			sb.append(create_token);
			urlFilter = null;
			
			return sb.toString();
		}
	}


	// 온라인 출석부 계정 동기화
	public static class AccountSyncRequestForAuthCode {
		// 이메일 HTML
		public final String html_text = "<!DOCTYPE html>\r\n"
				+ "\r\n"
				+ "<html lang=\"en\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\r\n"
				+ "<head>\r\n"
				+ "<title></title>\r\n"
				+ "<meta charset=\"utf-8\"/>\r\n"
				+ "<meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\"/>\r\n"
				+ "<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml><![endif]-->\r\n"
				+ "<style>\r\n"
				+ "		* {\r\n"
				+ "			box-sizing: border-box;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		body {\r\n"
				+ "			margin: 0;\r\n"
				+ "			padding: 0;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		a[x-apple-data-detectors] {\r\n"
				+ "			color: inherit !important;\r\n"
				+ "			text-decoration: inherit !important;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		#MessageViewBody a {\r\n"
				+ "			color: inherit;\r\n"
				+ "			text-decoration: none;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		p {\r\n"
				+ "			line-height: inherit\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		@media (max-width:700px) {\r\n"
				+ "			.icons-inner {\r\n"
				+ "				text-align: center;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.icons-inner td {\r\n"
				+ "				margin: 0 auto;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.fullMobileWidth,\r\n"
				+ "			.row-content {\r\n"
				+ "				width: 100% !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.image_block img.big {\r\n"
				+ "				width: auto !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.stack .column {\r\n"
				+ "				width: 100%;\r\n"
				+ "				display: block;\r\n"
				+ "			}\r\n"
				+ "		}\r\n"
				+ "	</style>\r\n"
				+ "</head>\r\n"
				+ "<body style=\"background-color: #202020; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"nl-container\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #202020;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-1\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"image_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;padding-right:20px;padding-left:20px;padding-top:70px;\">\r\n"
				+ "<div style=\"line-height:10px\"><img class=\"fullMobileWidth big\" src=\"https://rhya-network.kro.kr/RhyaNetwork/webpage/resources/icon/RNLogoForWhite_x400.png\" style=\"display: block; height: auto; border: 0; width: 238px; max-width: 100%;\" width=\"238\"/></div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 27px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#TITLE#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-2\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-bottom:35px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\"><span style=\"font-size:16px;\"><strong>이 메일은 발신 전용 메일 입니다. 발신 주소로 회신하지 마십시오. 또한 해당 메일은 개인의 민감한 정보가 포함되어있을 수 있습니다. 타인에게 노출되지 않도록 주의해주십시오.</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-3\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #141414; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:55px;padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #29a2ff; font-size: 28px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#ID#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\">#MESSAGE#</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"button_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;padding-top:10px;padding-right:10px;padding-bottom:50px;padding-left:10px;\">\r\n"
				+ "<div align=\"center\">\r\n"
				+ "<!--[if mso]><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"#URL#\" style=\"height:60px;width:205px;v-text-anchor:middle;\" arcsize=\"7%\" stroke=\"false\" fillcolor=\"#29a2ff\"><w:anchorlock/><v:textbox inset=\"0px,0px,0px,0px\"><center style=\"color:#ffffff; font-family:Arial, sans-serif; font-size:20px\"><![endif]--><a href=\"#URL#\" style=\"text-decoration:none;display:inline-block;color:#ffffff;background-color:#29a2ff;border-radius:4px;width:auto;border-top:1px solid #29a2ff;border-right:1px solid #29a2ff;border-bottom:1px solid #29a2ff;border-left:1px solid #29a2ff;padding-top:10px;padding-bottom:10px;font-family:Arial, Helvetica Neue, Helvetica, sans-serif;text-align:center;mso-border-alt:none;word-break:keep-all;\" target=\"_blank\"><span style=\"padding-left:50px;padding-right:50px;font-size:20px;display:inline-block;letter-spacing:normal;\"><span style=\"font-size: 12px; line-height: 2; word-break: break-word; mso-line-height-alt: 24px;\"><span data-mce-style=\"font-size: 20px; line-height: 40px;\" style=\"font-size: 20px; line-height: 40px;\"><strong>#BUTTON#</strong></span></span></span></a>\r\n"
				+ "<!--[if mso]></center></v:textbox></v:roundrect><![endif]-->\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-4\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<div class=\"spacer_block\" style=\"height:40px;line-height:40px;font-size:1px;\"> </div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-5\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>If this wasn't you</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-left:20px;padding-top:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0;\"><span style=\"font-size:16px;\"><strong>#NO_USER_REQUEST#</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-6\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;padding-top:40px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>Get in touch</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; letter-spacing: -1px;\"><span style=\"font-size:16px;\"><strong>RHYA.Network Dev Team, sihun.choi@email.rhya-network.kro.kr</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-7\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:90px;padding-right:20px;padding-bottom:10px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\">Copyright 2022 RHYA.Network. All rights reserved.</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-8\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"icons_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"color:#9d9d9d;font-family:inherit;font-size:15px;padding-bottom:5px;padding-top:5px;text-align:center;\">\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;\">\r\n"
				+ "<!--[if vml]><table align=\"left\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;\"><![endif]-->\r\n"
				+ "<!--[if !vml]><!-->\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" class=\"icons-inner\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; margin-right: -4px; padding-left: 0px; padding-right: 0px;\">\r\n"
				+ "<!--<![endif]-->\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-9\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table><!-- End -->\r\n"
				+ "</body>\r\n"
				+ "</html>";

		// 이메일 HTML custom tag
		private final String html_tag_title = "#TITLE#";
		private final String html_tag_id = "#ID#";
		private final String html_tag_message = "#MESSAGE#";
		private final String html_tag_url = "#URL#";
		private final String html_tag_button = "#BUTTON#";
		private final String html_tag_nurm = "#NO_USER_REQUEST#";
		
		// 문장 생성 StringBuilder
		private StringBuilder sb;
		
		// 이메일 Title 생성
		public String Title (String id) {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			sb.delete(0,sb.length());
			// 문장 생성
			sb.append("'");
			sb.append(id);
			sb.append("' 님의 온라인 출석부 계정 연동 요청");
			
			return sb.toString();
		}
		
		// 이메일 HTML 생성
		public String Html (String id, String url, String requestAccount, String targetAccount) throws FileNotFoundException {
			String htmlc = html_text;
			// HTML 커스텀 태그 변환
			htmlc = htmlc.replaceAll(html_tag_id, id);
			htmlc = htmlc.replaceAll(html_tag_url, url);
			htmlc = htmlc.replaceAll(html_tag_button, "계정 연동 승인");
			htmlc = htmlc.replaceAll(html_tag_title, "RHYA.Network 온라인 출석부 계정 연동 승인");
			htmlc = htmlc.replaceAll(html_tag_message, "<span style=\"font-size:16px;\"><strong>이 메일은 RHYA.Network 관리자 에게만 발송되는 메일 입니다. 온라인 출석부에서 <span style=\"font-size:16px;color: #29a2ff;\">#request#</span>계정이 <span style=\"font-size:16px;color: #29a2ff;\">#target#</span>계정으로 계정 정보 동기화를 진행하려고 합니다. 해당 작업의 승인을 진행하기 위해서는 아래 버튼을 눌러주십시오.</strong></span>".replace("#request#", requestAccount).replace("#target#", targetAccount));
			htmlc = htmlc.replaceAll(html_tag_nurm, "해당 메일을 본인이 요청한 것이 아니라면 즉시 삭제하거나 아레 있는 관리자 메일 주소로 이 이메일의 사진 사본과 함께 연락 주십시오.");


			return htmlc;
		}
		
		// URL 생성
		public String Url (HttpServletRequest req, String u_uuid, String a_uuid, String t_uuid) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			// URL 파라미터 조합
			URLFilter urlFilter = new URLFilter();
			sb.append(JspPageInfo.ServerURL);
			sb.append(JspPageInfo.GetJspPageURL(req, 27));
			sb.append("?u_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(u_uuid)));
			sb.append("&a_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(a_uuid)));
			sb.append("&t_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(t_uuid)));
			urlFilter = null;
			
			return sb.toString();
		}
	}

	
	// IP 하용
	public static class IPAccessAllow {
		private final String html_tag_id = "#ID#";
		private final String html_tag_url = "#URL#";
		
		// 문장 생성 StringBuilder
		private StringBuilder sb;
		
		// 이메일 HTML
		public final String login_email_html = "<!DOCTYPE html>\r\n"
				+ "\r\n"
				+ "<html lang=\"en\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\r\n"
				+ "<head>\r\n"
				+ "<title></title>\r\n"
				+ "<meta charset=\"utf-8\"/>\r\n"
				+ "<meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\"/>\r\n"
				+ "<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch><o:AllowPNG/></o:OfficeDocumentSettings></xml><![endif]-->\r\n"
				+ "<style>\r\n"
				+ "		* {\r\n"
				+ "			box-sizing: border-box;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		body {\r\n"
				+ "			margin: 0;\r\n"
				+ "			padding: 0;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		a[x-apple-data-detectors] {\r\n"
				+ "			color: inherit !important;\r\n"
				+ "			text-decoration: inherit !important;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		#MessageViewBody a {\r\n"
				+ "			color: inherit;\r\n"
				+ "			text-decoration: none;\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		p {\r\n"
				+ "			line-height: inherit\r\n"
				+ "		}\r\n"
				+ "\r\n"
				+ "		@media (max-width:700px) {\r\n"
				+ "			.icons-inner {\r\n"
				+ "				text-align: center;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.icons-inner td {\r\n"
				+ "				margin: 0 auto;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.fullMobileWidth,\r\n"
				+ "			.row-content {\r\n"
				+ "				width: 100% !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.image_block img.big {\r\n"
				+ "				width: auto !important;\r\n"
				+ "			}\r\n"
				+ "\r\n"
				+ "			.stack .column {\r\n"
				+ "				width: 100%;\r\n"
				+ "				display: block;\r\n"
				+ "			}\r\n"
				+ "		}\r\n"
				+ "	</style>\r\n"
				+ "</head>\r\n"
				+ "<body style=\"background-color: #202020; margin: 0; padding: 0; -webkit-text-size-adjust: none; text-size-adjust: none;\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"nl-container\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #202020;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-1\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"image_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;padding-right:20px;padding-left:20px;padding-top:70px;\">\r\n"
				+ "<div style=\"line-height:10px\"><img class=\"fullMobileWidth big\" src=\"https://rhya-network.kro.kr/RhyaNetwork/webpage/resources/icon/RNLogoForWhite_x400.png\" style=\"display: block; height: auto; border: 0; width: 238px; max-width: 100%;\" width=\"238\"/></div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 27px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>RHYA.Network 새로운 기기 로그인 허용</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-2\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-bottom:35px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\"><span style=\"font-size:16px;\"><strong>이 메일은 발신 전용 메일 입니다. 발신 주소로 회신하지 마십시오. 또한 해당 메일은 개인의 민감한 정보가 포함되어있을 수 있습니다. 타인에게 노출되지 않도록 주의해주십시오.</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-3\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #141414; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-top:55px;padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #29a2ff; font-size: 28px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>#ID#</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:25px;padding-bottom:25px;padding-left:25px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\"><span style=\"font-size:16px;\"><strong>아래의 정보로 새로운 기기가 접근하였습니다. 본인이 로그인 한 것이 아니라면 해당 메일을 제거하고 비밀번호를 변경해 주십시오.</strong></span></p>\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\"><span style=\"font-size:16px;\"></br><strong>TIME&#58; <span style=\"font-size:16px;color: #29a2ff;\">#TIME#</span></strong></span></p>\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\"><span style=\"font-size:16px;\"><strong>IP&#58; <span style=\"font-size:16px;color: #29a2ff;\">#IP#</span></strong></span></p>\r\n"
				+ "<p style=\"margin: 0; font-size: 16px;\"><span style=\"font-size:16px;\"><strong>LOCATION&#58; <span style=\"font-size:16px;color: #29a2ff;\">#LOCATION#</span></strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"button_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;padding-top:10px;padding-right:10px;padding-bottom:50px;padding-left:10px;\">\r\n"
				+ "<div align=\"center\">\r\n"
				+ "<!--[if mso]><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"#URL#\" style=\"height:60px;width:205px;v-text-anchor:middle;\" arcsize=\"7%\" stroke=\"false\" fillcolor=\"#29a2ff\"><w:anchorlock/><v:textbox inset=\"0px,0px,0px,0px\"><center style=\"color:#ffffff; font-family:Arial, sans-serif; font-size:20px\"><![endif]--><a href=\"#URL#\" style=\"text-decoration:none;display:inline-block;color:#ffffff;background-color:#29a2ff;border-radius:4px;width:auto;border-top:1px solid #29a2ff;border-right:1px solid #29a2ff;border-bottom:1px solid #29a2ff;border-left:1px solid #29a2ff;padding-top:10px;padding-bottom:10px;font-family:Arial, Helvetica Neue, Helvetica, sans-serif;text-align:center;mso-border-alt:none;word-break:keep-all;\" target=\"_blank\"><span style=\"padding-left:50px;padding-right:50px;font-size:20px;display:inline-block;letter-spacing:normal;\"><span style=\"font-size: 12px; line-height: 2; word-break: break-word; mso-line-height-alt: 24px;\"><span data-mce-style=\"font-size: 20px; line-height: 40px;\" style=\"font-size: 20px; line-height: 40px;\"><strong>로그인 허용</strong></span></span></span></a>\r\n"
				+ "<!--[if mso]></center></v:textbox></v:roundrect><![endif]-->\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-4\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<div class=\"spacer_block\" style=\"height:40px;line-height:40px;font-size:1px;\"> </div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-5\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>If this wasn't you</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-right:20px;padding-left:20px;padding-top:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0;\"><span style=\"font-size:16px;\"><strong>해당 메일을 본인이 요청한 것이 아니라면 즉시 삭제하거나 아레 있는 관리자 메일 주소로 이 이메일의 사진 사본과 함께 연락 주십시오.</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-6\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"heading_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"width:100%;text-align:center;padding-right:20px;padding-left:20px;padding-top:40px;\">\r\n"
				+ "<h1 style=\"margin: 0; color: #ffffff; font-size: 23px; font-family: Arial, Helvetica Neue, Helvetica, sans-serif; line-height: 120%; text-align: left; direction: ltr; font-weight: normal; letter-spacing: normal; margin-top: 0; margin-bottom: 0;\"><strong>Get in touch</strong></h1>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:10px;padding-right:20px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 12px; mso-line-height-alt: 14.399999999999999px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; letter-spacing: -1px;\"><span style=\"font-size:16px;\"><strong>RHYA.Network Dev Team, sihun.choi@email.rhya-network.kro.kr</strong></span></p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-7\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"text_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; word-break: break-word;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"padding-top:90px;padding-right:20px;padding-bottom:10px;padding-left:20px;\">\r\n"
				+ "<div style=\"font-family: sans-serif\">\r\n"
				+ "<div style=\"font-size: 14px; mso-line-height-alt: 16.8px; color: #ffffff; line-height: 1.2; font-family: Arial, Helvetica Neue, Helvetica, sans-serif;\">\r\n"
				+ "<p style=\"margin: 0; font-size: 14px;\">Copyright 2022 RHYA.Network. All rights reserved.</p>\r\n"
				+ "</div>\r\n"
				+ "</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-8\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"icons_block\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"color:#9d9d9d;font-family:inherit;font-size:15px;padding-bottom:5px;padding-top:5px;text-align:center;\">\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tr>\r\n"
				+ "<td style=\"text-align:center;\">\r\n"
				+ "<!--[if vml]><table align=\"left\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"display:inline-block;padding-left:0px;padding-right:0px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;\"><![endif]-->\r\n"
				+ "<!--[if !vml]><!-->\r\n"
				+ "<table cellpadding=\"0\" cellspacing=\"0\" class=\"icons-inner\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; display: inline-block; margin-right: -4px; padding-left: 0px; padding-right: 0px;\">\r\n"
				+ "<!--<![endif]-->\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row row-9\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt;\" width=\"100%\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td>\r\n"
				+ "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"row-content stack\" role=\"presentation\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; color: #000000; width: 680px;\" width=\"680\">\r\n"
				+ "<tbody>\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"column\" style=\"mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-weight: 400; text-align: left; vertical-align: top; padding-top: 5px; padding-bottom: 5px; border-top: 0px; border-right: 0px; border-bottom: 0px; border-left: 0px;\" width=\"100%\">\r\n"
				+ "<div class=\"spacer_block\" style=\"height:70px;line-height:70px;font-size:1px;\"> </div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</tbody>\r\n"
				+ "</table><!-- End -->\r\n"
				+ "</body>\r\n"
				+ "</html>";
		
		// 이메일 Title 생성
		public String Title (String id) {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			sb.delete(0,sb.length());
			// 문장 생성
			sb.append("새로운 기기에서 '");
			sb.append(id);
			sb.append("' 아이디로 로그인하였습니다.");
			
			return sb.toString();
		}
		
		// 이메일 HTML 생성
		public String Html (String id, String url, String time, String ip, String location) {
			String htmlc = login_email_html;
			// HTML 커스텀 태그 변환
			htmlc = htmlc.replaceAll(html_tag_id, id);
			htmlc = htmlc.replaceAll(html_tag_url, url);
			htmlc = htmlc.replaceAll("#TIME#", time);
			htmlc = htmlc.replaceAll("#IP#", ip);
			htmlc = htmlc.replaceAll("#LOCATION#", location);

			return htmlc;
		}
		
		// URL 생성
		public String Url (HttpServletRequest req, String uuid, String key) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			// URL 파라미터 조합
			URLFilter urlFilter = new URLFilter();
			sb.append(JspPageInfo.ServerURL);
			sb.append(JspPageInfo.GetJspPageURL(req, JspPageInfo.PageID_Rhya_IP_Access_Allow));
			sb.append("?uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(uuid)));
			sb.append("&key=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(key)));
			urlFilter = null;
			
			return sb.toString();
		}
	}

	
	// 우타이테 플레이어 이용권 이메일
	public static class UtaitePlayerTicketApplication {
		public final String token = "5D5E8865642F26C032C63861F293D7C84BBDB80A6CF863CD43790711FFA5CA03536DC5F6381C89B45A4D1DEA9F1F29F9C592503FEEAF57E602B19E16F59E8E22";
		// 이메일 HTML custom tag
		private final String html_tag_title = "#TITLE#";
		private final String html_tag_id = "#ID#";
		private final String html_tag_message = "#MESSAGE#";
		private final String html_tag_url = "#URL#";
		private final String html_tag_button = "#BUTTON#";
		private final String html_tag_nurm = "#NO_USER_REQUEST#";
		
		// 문장 생성 StringBuilder
		private StringBuilder sb;

		// 이메일 Title 생성
		public String Title (String id) {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			sb.delete(0,sb.length());
			// 문장 생성
			sb.append("'");
			sb.append(id);
			sb.append("' 님이 우타이테 플레이어 이용 승인을 요청하였습니다!");
			
			return sb.toString();
		}
		
		// 이메일 HTML 생성
		public String Html (String html, String id, String uuid, String url) throws FileNotFoundException {
			String htmlc = html;
			// HTML 커스텀 태그 변환
			htmlc = htmlc.replaceAll(html_tag_id, id);
			htmlc = htmlc.replaceAll(html_tag_url, url);
			htmlc = htmlc.replaceAll(html_tag_button, "이용권 허용");
			htmlc = htmlc.replaceAll(html_tag_title, "우타이테 플레이어 이용 승인 요청");
			htmlc = htmlc.replaceAll(html_tag_message, "<span style=\"font-size:16px;\"><strong>사용자가 우타이테 플레이어 이용 승인을 요청하였습니다. [ 사용자 UUID : %UUID% ] 아래 버튼을 클릭하면 승인 절차가 진행됩니다.</strong></span>".replace("%UUID%", uuid));
			htmlc = htmlc.replaceAll(html_tag_nurm, "해당 메일을 본인이 요청한 것이 아니라면 즉시 삭제하거나 아레 있는 관리자 메일 주소로 이 이메일의 사진 사본과 함께 연락 주십시오.");

			return htmlc;
		}
		
		// URL 생성
		public String Url (HttpServletRequest req, String u_uuid) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			// StringBuilder 초기화
			if (sb == null) { sb = new StringBuilder(); }
			// URL 파라미터 조합
			URLFilter urlFilter = new URLFilter();
			sb.append(JspPageInfo.ServerURL);
			sb.append(JspPageInfo.GetJspPageURL(req, 38));
			sb.append("?u_uuid=");
			sb.append(urlFilter.SetFilter(RhyaAES.AES_Encode(u_uuid)));
			sb.append("&token=");
			sb.append(token);
			urlFilter = null;
			
			return sb.toString();
		}
	}
}
