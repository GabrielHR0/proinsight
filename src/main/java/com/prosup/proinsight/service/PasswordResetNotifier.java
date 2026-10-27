package com.prosup.proinsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Delivers password-reset tokens out-of-band.
 *
 * <p>The raw token must never be returned by the HTTP API. In production the
 * token is sent by e-mail; in local/test environments it is written to the log
 * so the flow can be exercised without an SMTP server. When no SMTP host is
 * configured in production, the token is intentionally discarded and an error
 * is logged instead of leaking it to the caller.</p>
 */
@Component
public class PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetNotifier.class);

    private final Environment environment;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final boolean smtpStartTls;
    private final String from;
    private final String resetUrl;

    public PasswordResetNotifier(Environment environment,
                                 @Value("${app.password-reset.smtp-host:}") String smtpHost,
                                 @Value("${app.password-reset.smtp-port:587}") int smtpPort,
                                 @Value("${app.password-reset.smtp-username:}") String smtpUsername,
                                 @Value("${app.password-reset.smtp-password:}") String smtpPassword,
                                 @Value("${app.password-reset.smtp-starttls:true}") boolean smtpStartTls,
                                 @Value("${app.password-reset.from:no-reply@proinsight.com}") String from,
                                 @Value("${app.password-reset.reset-url:http://localhost:5173/reset-password}") String resetUrl) {
        this.environment = environment;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpStartTls = smtpStartTls;
        this.from = from;
        this.resetUrl = resetUrl;
    }

    public void send(String email, String rawToken) {
        String resetLink = buildResetLink(rawToken);

        if (smtpHost == null || smtpHost.isBlank()) {
            if (isLocalOrTest()) {
                log.info("Password reset link for {}: {}", email, resetLink);
            } else {
                log.error("Password reset e-mail not sent: app.password-reset.smtp-host is not configured");
            }
            return;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtpHost);
        sender.setPort(smtpPort);
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        boolean hasCredentials = smtpUsername != null && !smtpUsername.isBlank();
        if (hasCredentials) {
            sender.setUsername(smtpUsername);
            sender.setPassword(smtpPassword);
        }

        Properties mailProperties = sender.getJavaMailProperties();
        mailProperties.put("mail.smtp.auth", String.valueOf(hasCredentials));
        mailProperties.put("mail.smtp.starttls.enable", String.valueOf(smtpStartTls));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("ProInsight - Redefinição de senha");
        message.setText("""
                Olá,

                Recebemos uma solicitação para redefinir a senha da sua conta ProInsight.

                Acesse o link abaixo para criar uma nova senha. O link expira em 15 minutos e só pode ser usado uma vez:

                %s

                Se você não fez essa solicitação, ignore este e-mail.
                """.formatted(resetLink));

        sender.send(message);
        log.info("Password reset e-mail sent to {}", email);
    }

    private String buildResetLink(String rawToken) {
        String separator = resetUrl.contains("?") ? "&" : "?";
        return resetUrl + separator + "token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private boolean isLocalOrTest() {
        return environment.acceptsProfiles(Profiles.of("local", "test"));
    }
}
