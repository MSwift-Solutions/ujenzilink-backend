package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.configs.ResendConfig;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.EmailNotificationDTO;
import com.ujenzilink.ujenzilink_backend.notifications.enums.EmailTypes;
import com.ujenzilink.ujenzilink_backend.notifications.models.Email;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.EmailRepository;
import com.ujenzilink.ujenzilink_backend.notifications.utils.EmailTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ResendNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ResendNotificationService.class);

    private final WebClient webClient;
    private final String fromEmail;
    private final EmailRepository emailRepository;

    public ResendNotificationService(
            WebClient resendWebClient,
            ResendConfig resendConfig,
            EmailRepository emailRepository
    ) {
        this.webClient = resendWebClient;
        this.fromEmail = resendConfig.getFromEmail();
        this.emailRepository = emailRepository;
    }

    public void sendConfirmationEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getVerificationCodeEmail(emailDetails.name(), emailDetails.token());
        sendEmail(emailDetails.email(), "Verification Code", body, user, EmailTypes.VERIFICATION_CODE);
    }

    public void sendSuccessfulCreationEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getWelcomeEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Welcome to UJENZI LINK", body, user, EmailTypes.WELCOME_EMAIL);
    }

    public void sendPassChangeEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordChangedEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Password Changed", body, user, EmailTypes.PASSWORD_CHANGED);
    }

    public void sendPassResetEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordResetEmail(emailDetails.name(), emailDetails.token());
        sendEmail(emailDetails.email(), "Password Reset", body, user, EmailTypes.PASSWORD_RESET);
    }

    public void sendSignInNotificationEmail(String email, String name, String loginTime, User user) {
        String body = EmailTemplates.getSignInNotificationEmail(name, loginTime);
        sendEmail(email, "Sign-in Notification", body, user, EmailTypes.SIGNIN_NOTIFICATION);
    }

    public void sendSignUpNotificationEmail(String email, String name, User user) {
        String body = EmailTemplates.getSignUpNotificationEmail(name);
        sendEmail(email, "Welcome to UJENZI LINK", body, user, EmailTypes.SIGNUP_NOTIFICATION);
    }

    public void sendProjectInvitationEmail(String email, String name, String projectName, String inviterName, User user) {
        String body = EmailTemplates.getProjectInvitationEmail(name, projectName, inviterName);
        sendEmail(email, "Project Invitation: " + projectName, body, user, EmailTypes.PROJECT_INVITATION);
    }

    public void sendAccountLockedEmail(String email, String name, User user) {
        String body = EmailTemplates.getAccountLockedEmail(name);
        sendEmail(email, "Account Locked", body, user, EmailTypes.ACCOUNT_LOCKED);
    }

    public void sendAccountDeletionEmail(String email, String name, User user) {
        String body = EmailTemplates.getAccountDeletionEmail(name);
        sendEmail(email, "Account Deletion Initiated", body, user, EmailTypes.ACCOUNT_DELETION);
    }

    private void sendEmail(String to, String subject, String htmlContent, User user, EmailTypes emailType) {

        //TODO fix to email to the one on resend till we buy domain name
        to = "mswiftsolutions01@gmail.com";
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("from", fromEmail);
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("html", htmlContent);

            ResponseEntity<Void> response = webClient.post()
                    .uri("/emails")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            boolean sentSuccessfully = response != null && response.getStatusCode().is2xxSuccessful();

            if (sentSuccessfully) {
                //log.info("Email sent successfully to {} with type {}", to, emailType);
                logEmail(to, emailType, htmlContent, user);
            } else {
                log.warn("Email was not sent successfully to {} with type {}", to, emailType);
            }

        } catch (Exception e) {
            log.error("Failed to send email to {} with type {}: {}", to, emailType, e.getMessage(), e);
        }

    }

    private void logEmail(String recipientEmail, EmailTypes emailType, String body, User user) {
        try {
            Email email = new Email(recipientEmail, emailType, body, user);
           // add the is successfull and the service used.
            emailRepository.save(email);
        } catch (Exception e) {
            log.error("Failed to log email for {} with type {}: {}", recipientEmail, emailType, e.getMessage(), e);
        }
    }
}