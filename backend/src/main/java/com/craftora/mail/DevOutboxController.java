package com.craftora.mail;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development/E2E sandbox only: exposes the in-memory outbox so verification and reset links can be followed
 * without a real mailbox. Enabled solely when {@code APP_DEV_OUTBOX=true}; never enable in production.
 */
@Hidden
@RestController
@RequestMapping("/api/dev/outbox")
@ConditionalOnProperty(name = "app.dev.outbox-enabled", havingValue = "true")
public class DevOutboxController {

    private final EmailService emailService;

    public DevOutboxController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public List<EmailMessage> outbox() {
        return emailService.outbox();
    }
}
