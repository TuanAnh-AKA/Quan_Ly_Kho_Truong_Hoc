package com.example.quan_ly_kho.service;



import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void sendOtpEmail(String to, String otp) throws IOException {
        String subject = "Mã OTP đặt lại mật khẩu của bạn";
        String contentText = "Xin chào!\n\n"
                + "Mã OTP để đặt lại mật khẩu của bạn là: " + otp + "\n"
                + "Mã này có hiệu lực trong 5 phút.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\nĐội ngũ Quản lý kho";

        Email from = new Email("anh.tuan.08355@gmail.com"); // địa chỉ gửi (bạn có thể đổi)
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", contentText);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("✅ Gửi mail thành công. Status: " + response.getStatusCode());
        } catch (IOException ex) {
            System.err.println("❌ Lỗi khi gửi mail: " + ex.getMessage());
            throw ex;
        }
    }
}
