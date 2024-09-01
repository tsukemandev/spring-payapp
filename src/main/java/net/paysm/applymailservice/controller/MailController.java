package net.paysm.applymailservice.controller;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class MailController {

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/send-mail/apply")
    public ResponseEntity<Object> sendApplyEmail(@RequestBody Map<String, String> request) throws MessagingException {
        
        String accountHolder = request.get("account-holder");

        String businessAccountNumber = request.get("business-account-number");
        String businessAccountNumberType = request.get("business-account-number-type");
        String businessAddress = request.get("business-address");
        String businessAddressDetail = request.get("business-address-detail");
        String businessEmail = request.get("business-email");
        String businessNumber = request.get("business-number");
        String businessTel1 = request.get("business-tel-1");
        String businessTel2 = request.get("business-tel-2");
        String businessTel3 = request.get("business-tel-3");
        String businessType = request.get("business-type");

        String corpHomepage = request.get("corp-homepage");
        String corpNmae = request.get("corp-name");

        String ownerBitrh = request.get("owner-birth");
        String ownerName = request.get("owner-name");

        String requestOther = request.get("request"); //별도 요청사항
        String zoneCode = request.get("zone-code");


        businessType = "1".equals(businessType) ? "개인 사업자" : "2".equals(businessType) ? "법인 사업자" : "비영리법인";
        businessAccountNumberType = "1".equals(businessAccountNumberType) ? "개인 사업자" : "2".equals(businessAccountNumberType) ? "법인 사업자" : "비영리법인";



        String subject = corpNmae + " 신청서류";
        String message = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + 
        "<head>" + 
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" + 
            "<title>HTML Email Template</title>" + 
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
        "</head>" 

            + "====================================="
            + "<br>"
            + " 회사명 : " + corpNmae + "<br>"
            + " 사업자등록번호 : " + businessNumber + "<br>"
            + " 사업자 유형 : " + businessType + "<br>"
            + " 대표자명 : " + ownerName + "<br>"
            + " 대표자 생년월일 : " + ownerBitrh + "<br>"
            + "<br>"
            + "====================================="
            + "<br>"
            + " 사업자 계좌번호 : " + businessAccountNumber + "<br>"
            + " 사업자 계좌유형 : " + businessAccountNumberType + "<br>"
            + " 예금주명 : " + accountHolder + "<br>"

            + "<br>"
            + "====================================="
            + "<br>"

            + " 사업장 우편번호 : " + zoneCode + "<br>"
            + " 사업장 주소 : " + businessAddress + "<br>"
            + " 사업장 상세주소 : " + businessAddressDetail + "<br>"
            + " 사업장 대표 전화번호 : " + businessTel1 + " - "  + businessTel2 + " - " + businessTel3 + "<br>"
            + " 사업장 대표 이메일 : " + businessEmail + "<br>"
            + " 회사 홈페이지 : " + corpHomepage + "<br>"
            + " 요청사항 : " + requestOther + "<br>"

            + "</html>"
            ;
        
            sendHtmlMessage("barunidea0880@naver.com", subject, message);
        return ResponseEntity.ok("메일전송이 완료되었습니다.");
    }


    @PostMapping("/send-mail/alliance")
    public ResponseEntity<Object> sendAllianceEmail(@RequestBody Map<String, String> request) throws MessagingException {
        
        String businessName = request.get("business-name");  //상호명
        String businessNumber = request.get("business-number");  //사업자 등록번호

        String businessTel1 = request.get("business-tel-1");  //연락처
        String businessTel2 = request.get("business-tel-2");
        String businessTel3 = request.get("business-tel-3");
        String businessEmail = request.get("business-email");  // 이메일

        String title = request.get("title"); //제목
        String req = request.get("request"); //문의사항

        String subject = businessName + " 제휴문의";
        String message = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + 
        "<head>" + 
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" + 
            "<title>HTML Email Template</title>" + 
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
        "</head>" 

            + "====================================="
            + "<br>"
            + " 회사명 : " + businessName + "<br>"
            + " 사업자등록번호 : " + businessNumber + "<br>"
            + " 연락처 : " + businessTel1 + " - "  + businessTel2 + " - " + businessTel3 + "<br>"
            + " 이메일 : " + businessEmail + "<br>"
            + " 제목 : " + title + "<br>"
            + " 요청사항 : " + req + "<br>"

            + "</html>"
            ;
        
            sendHtmlMessage("barunidea0880@naver.com", subject, message);
        return ResponseEntity.ok("메일전송이 완료되었습니다.");
    }


    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        // true 매개변수는 multipart 메시지를 나타냅니다.
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true는 HTML 메일을 보낼 수 있도록 합니다.

        emailSender.send(message);
    }
}