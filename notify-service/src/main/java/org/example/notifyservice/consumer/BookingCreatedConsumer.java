package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BookingCreatedConsumer {

    private final EmailService emailService;

    public BookingCreatedConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${notification.kafka.booking-created-topic:booking-created}", groupId = "${spring.kafka.consumer.group-id:notify-service}")
    public void consume(String email) {
        emailService.sendBookingCreatedEmail(email);
    }
}
