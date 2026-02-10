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

    public EmailNotificationService(JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    public void sendConfirmationEmail(EmailNotificationDTO emailDetails, User user) {
        try {
            String body = EmailTemplates.getVerificationCodeEmail(emailDetails.name(), emailDetails.token());
            sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.VERIFICATION_CODE);
        } catch (Exception e) {
            System.out.println("Failed to send mail " + e.getMessage());
        }
    }

    public void sendSuccessfulCreationEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getWelcomeEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.WELCOME_EMAIL);
    }

    public void sendPassChangeEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordChangedEmail(emailDetails.name());
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.PASSWORD_CHANGED);
    }

    public void sendPassResetEmail(EmailNotificationDTO emailDetails, User user) {
        String body = EmailTemplates.getPasswordResetEmail(emailDetails.name(), emailDetails.token());
        sendEmail(emailDetails.email(), "Customer", body, user, EmailTypes.PASSWORD_RESET);
    }

    public void sendSignInNotificationEmail(String email, String name, String loginTime, User user) {
        String body = EmailTemplates.getSignInNotificationEmail(name, loginTime);
        sendEmail(email, "Sign-in Notification", body, user, EmailTypes.SIGNIN_NOTIFICATION);
    }

    public void sendSignUpNotificationEmail(String email, String name, User user) {
        String body = EmailTemplates.getSignUpNotificationEmail(name);
        sendEmail(email, "Welcome to UJENZI LINK", body, user, EmailTypes.SIGNUP_NOTIFICATION);
    }

    public void sendProjectInvitationEmail(String email, String name, String projectName, String inviterName,
            User user) {
        String body = EmailTemplates.getProjectInvitationEmail(name, projectName, inviterName);
        sendEmail(email, "Project Invitation: " + projectName, body, user, EmailTypes.PROJECT_INVITATION);
    }

    public void sendAccountLockedEmail(String email, String name, User user) {
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
            logEmail(to, emailType, body, user);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
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
