package com.revshop.service.impl;

import com.revshop.exception.InternalServerException;
import com.revshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailServiceImpl implements EmailService {

    private static final String DEFAULT_FROM_ADDRESS = "no-reply@revshop.local";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@revshop.local}")
    private String fromAddress = DEFAULT_FROM_ADDRESS;

    @Value("${spring.mail.host:}")
    private String mailHost = "";

    @Value("${spring.mail.username:}")
    private String mailUsername = "";

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String resetLink, int expiryMinutes) {
        String resolvedFromAddress = resolveFromAddress();
        validateMailConfiguration(resolvedFromAddress);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new InternalServerException("SMTP mail sender is unavailable. Check the spring.mail configuration.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(resolvedFromAddress);
        message.setTo(recipientEmail);
        message.setSubject("RevShop password reset");
        message.setText("""
                Hello,

                We received a request to reset the password for your RevShop account.

                Reset your password using this link:
                %s

                This link expires in %d minutes.

                If you did not request this change, you can ignore this email.

                RevShop Support
                """.formatted(resetLink, expiryMinutes));

        try {
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            log.error("SMTP authentication failed while sending password reset email to {}", recipientEmail, ex);
            throw new InternalServerException(
                    "SMTP authentication failed. Check MAIL_USERNAME, MAIL_PASSWORD, and your app password."
            );
        } catch (MailSendException ex) {
            log.error("SMTP rejected or could not send password reset email to {}", recipientEmail, ex);
            throw new InternalServerException(resolveMailSendFailureMessage(ex));
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", recipientEmail, ex);
            throw new InternalServerException(
                    "Unable to send the password reset email. Check SMTP host, sender email, and network access."
            );
        }
    }

    private void validateMailConfiguration(String resolvedFromAddress) {
        if (!StringUtils.hasText(mailHost)) {
            throw new InternalServerException("SMTP host is not configured. Set MAIL_HOST or spring.mail.host.");
        }
        if (!StringUtils.hasText(mailUsername)) {
            throw new InternalServerException("SMTP username is not configured. Set MAIL_USERNAME or spring.mail.username.");
        }
        if (!StringUtils.hasText(resolvedFromAddress)) {
            throw new InternalServerException("SMTP sender address is not configured. Set APP_MAIL_FROM or MAIL_USERNAME.");
        }
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(fromAddress) && !DEFAULT_FROM_ADDRESS.equalsIgnoreCase(fromAddress.trim())) {
            return fromAddress.trim();
        }
        return StringUtils.hasText(mailUsername) ? mailUsername.trim() : fromAddress;
    }

    private String resolveMailSendFailureMessage(MailSendException ex) {
        String rootMessage = ex.getMostSpecificCause() == null ? "" : ex.getMostSpecificCause().getMessage();
        String normalizedMessage = rootMessage == null ? "" : rootMessage.toLowerCase();

        if (normalizedMessage.contains("could not connect to smtp host")
                || normalizedMessage.contains("connection refused")
                || normalizedMessage.contains("timed out")
                || normalizedMessage.contains("unknownhost")) {
            return "Could not connect to the SMTP server. Check MAIL_HOST, MAIL_PORT, and network access.";
        }

        if (normalizedMessage.contains("sender address rejected")
                || normalizedMessage.contains("from address")
                || normalizedMessage.contains("invalid addresses")) {
            return "SMTP rejected the sender address. Set APP_MAIL_FROM to the same email as MAIL_USERNAME.";
        }

        return "Unable to send the password reset email. Check SMTP host, sender email, and network access.";
    }
}
