package com.craftora.mail;

import com.craftora.common.AppProperties;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional email. {@code app.mail.mode=log} (development/sandbox) logs messages and keeps the last
 * 50 in memory instead of delivering them; {@code smtp} delivers via the configured SMTP provider.
 * Delivery failures are logged and never break the calling business flow.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int OUTBOX_SIZE = 50;

    private final AppProperties props;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final Deque<EmailMessage> outbox = new ArrayDeque<>();

    public EmailService(AppProperties props, ObjectProvider<JavaMailSender> mailSender) {
        this.props = props;
        this.mailSender = mailSender;
    }

    public void send(EmailMessage message) {
        if ("smtp".equals(props.mail().mode())) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(props.mail().from());
                mail.setTo(message.to());
                mail.setSubject(message.subject());
                mail.setText(message.body());
                mailSender.getObject().send(mail);
            } catch (RuntimeException e) {
                log.error("Failed to send email '{}': {}", message.subject(), e.getMessage());
            }
            return;
        }
        log.info("[mail:log] subject='{}' (delivery disabled, APP_MAIL_MODE=log)", message.subject());
        synchronized (outbox) {
            outbox.addFirst(message);
            while (outbox.size() > OUTBOX_SIZE) {
                outbox.removeLast();
            }
        }
    }

    /** Sandbox outbox (log mode only), newest first. */
    public List<EmailMessage> outbox() {
        synchronized (outbox) {
            return List.copyOf(outbox);
        }
    }
}
