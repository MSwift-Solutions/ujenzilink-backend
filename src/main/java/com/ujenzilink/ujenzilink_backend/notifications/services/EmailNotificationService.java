package com.ujenzilink.ujenzilink_backend.notifications.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.notifications.dtos.EmailNotificationDTO;
import com.ujenzilink.ujenzilink_backend.notifications.enums.EmailTypes;
import com.ujenzilink.ujenzilink_backend.notifications.models.Email;
import com.ujenzilink.ujenzilink_backend.notifications.repositories.EmailRepository;
import com.ujenzilink.ujenzilink_backend.notifications.utils.EmailTemplates;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailNotificationService(JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    private void sendConfirmationEmail(EmailNotificationDTO emailDetails, User user) {
        try {
            String body = EmailTemplates.getVerificationCodeEmail(emailDetails.name(), emailDetails.token(), emailDetails.email(), frontendUrl);
            sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.VERIFICATION_CODE);
        } catch (Exception e) {
            System.out.println("Failed to send mail " + e.getMessage());
        }
    }

    private void sendSuccessfulCreationEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getWelcomeEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.WELCOME_EMAIL);
    }

    private void sendPassChangeEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordChangedEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.PASSWORD_CHANGED);
    }

    private void sendPassResetEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordResetEmail(emailDetails.name(), emailDetails.token(), emailDetails.email(), frontendUrl);
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.PASSWORD_RESET);
    }

    private void sendSignInNotificationEmail(String email, String name, String loginTime, User user) {
        String body = EmailTemplates.getSignInNotificationEmail(name, loginTime);
        sendEmail(email, "Sign-in Notification", body, user, EmailTypes.SIGNIN_NOTIFICATION);
    }

    private void sendSignUpNotificationEmail(String email, String name, User user) {
        String body = EmailTemplates.getSignUpNotificationEmail(name);
        sendEmail(email, "Welcome to UJENZI LINK", body, user, EmailTypes.SIGNUP_NOTIFICATION);
    }

    private void sendProjectInvitationEmail(String email, String name, String projectName, String inviterName,
            User user) {
        String body = EmailTemplates.getProjectInvitationEmail(name, projectName, inviterName);
        sendEmail(email, "Project Invitation: " + projectName, body, user, EmailTypes.PROJECT_INVITATION);
    }

    private void sendAccountLockedEmail(String email, String name, User user) {
        String body = EmailTemplates.getAccountLockedEmail(name);
        sendEmail(email, "Account Locked", body, user, EmailTypes.ACCOUNT_LOCKED);
    }

    // Generic email sending method
    private void sendEmail(String to, String subject, String body, User user, EmailTypes emailType) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("wd213230@gmail.com");
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);

            mailSender.send(mailMessage);

            // Log email to database
            if (user != null) {
                logEmail(to, emailType, body, user);
            }
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendSystemFailureReportEmail(String to, String hostname, String timestamp, String backupFile, String step,
            String errorMessage, String destHost, String destDir) {
        String body = EmailTemplates.getBackupFailureReportEmail(hostname, timestamp, backupFile, step, errorMessage,
                destHost, destDir);
        sendEmail(to, "URGENT: Backup Failure Notification", body, null, null);
    }

    // Helper method to log emails to database
    private void logEmail(String recipientEmail, EmailTypes emailType, String body, User user) {
        try {
            Email email = new Email(recipientEmail, emailType, body, user);
            emailRepository.save(email);
        } catch (Exception e) {
            System.err.println("Failed to log email: " + e.getMessage());
        }
    }
}
