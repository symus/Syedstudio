package com.craftora.mail;

import com.craftora.common.AppProperties;
import org.springframework.stereotype.Component;

/** Plain-text transactional templates in Polish and English. */
@Component
public class MailTemplates {

    private final AppProperties props;

    public MailTemplates(AppProperties props) {
        this.props = props;
    }

    public EmailMessage verifyEmail(String to, String locale, String name, String token) {
        String link = props.publicUrl() + "/verify-email?token=" + token;
        return pl(locale)
                ? msg(to, "Potwierdź swój adres e-mail – " + brand(),
                "Cześć %s,\n\nkliknij link, aby potwierdzić adres e-mail:\n%s\n\nLink jest ważny 48 godzin.".formatted(name, link))
                : msg(to, "Confirm your email – " + brand(),
                "Hi %s,\n\nplease confirm your email address:\n%s\n\nThe link is valid for 48 hours.".formatted(name, link));
    }

    public EmailMessage passwordReset(String to, String locale, String token) {
        String link = props.publicUrl() + "/reset-password?token=" + token;
        return pl(locale)
                ? msg(to, "Reset hasła – " + brand(),
                "Otrzymaliśmy prośbę o reset hasła. Ustaw nowe hasło tutaj:\n%s\n\nLink jest ważny 1 godzinę. Jeśli to nie Ty, zignoruj tę wiadomość.".formatted(link))
                : msg(to, "Reset your password – " + brand(),
                "We received a request to reset your password. Set a new one here:\n%s\n\nThe link is valid for 1 hour. If this wasn't you, ignore this email.".formatted(link));
    }

    public EmailMessage inquiryReceivedByMaker(String to, String locale, String customerName, String inquiryId) {
        String link = props.publicUrl() + "/dashboard/inquiries/" + inquiryId;
        return pl(locale)
                ? msg(to, "Nowe zapytanie od " + customerName,
                "Masz nowe zapytanie o zamówienie na zamówienie.\nZobacz szczegóły: %s".formatted(link))
                : msg(to, "New inquiry from " + customerName,
                "You have a new custom-order inquiry.\nView details: %s".formatted(link));
    }

    public EmailMessage inquiryConfirmation(String to, String locale, String customerName, String makerName) {
        return pl(locale)
                ? msg(to, "Twoje zapytanie zostało wysłane do " + makerName,
                "Cześć %s,\n\n%s otrzymał(a) Twoje zapytanie i odpowie bezpośrednio na ten adres e-mail.\n\n– %s"
                        .formatted(customerName, makerName, brand()))
                : msg(to, "Your inquiry was sent to " + makerName,
                "Hi %s,\n\n%s has received your inquiry and will reply to this email address.\n\n– %s"
                        .formatted(customerName, makerName, brand()));
    }

    public EmailMessage inquiryReply(String to, String locale, String makerName, String status, String message) {
        return pl(locale)
                ? msg(to, "Odpowiedź od " + makerName,
                "%s odpowiedział(a) na Twoje zapytanie (status: %s):\n\n%s".formatted(makerName, status, message))
                : msg(to, "Reply from " + makerName,
                "%s replied to your inquiry (status: %s):\n\n%s".formatted(makerName, status, message));
    }

    private String brand() {
        return props.brandName();
    }

    private static boolean pl(String locale) {
        return !"en".equals(locale);
    }

    private static EmailMessage msg(String to, String subject, String body) {
        return new EmailMessage(to, subject, body);
    }
}
