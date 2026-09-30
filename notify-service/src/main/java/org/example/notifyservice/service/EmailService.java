package org.example.notifyservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendBookingCreatedEmail(String recipient) {
        if (!StringUtils.hasText(recipient)) {
            throw new IllegalArgumentException("Recipient email must not be blank");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient.trim());
        message.setSubject("Đặt vé xem phim thành công");
        message.setText("Cảm ơn bạn đã đặt vé xem phim!");
        mailSender.send(message);
    }
}
