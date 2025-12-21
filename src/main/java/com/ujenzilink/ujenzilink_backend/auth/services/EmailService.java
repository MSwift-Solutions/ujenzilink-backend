package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.dtos.EmailDetails;
import com.ujenzilink.ujenzilink_backend.auth.enums.EmailTypes;
import com.ujenzilink.ujenzilink_backend.auth.models.Email;
import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.EmailRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    public EmailService(JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    public void sendConfirmationEmail(EmailDetails emailDetails) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("wd213230@gmail.com");
            mailMessage.setTo(emailDetails.email());
            mailMessage.setSubject("Customer");

            String stringBuilder = "Dear " + emailDetails.name().toUpperCase() +
                    ",\n\n" +
                    "Thanks for registering. Please confirm your account using the code or link below:\n" +
                    "Code: " + emailDetails.token() + "\n" +
                    "Link: http://localhost:8080/auth/confirm?token=" + emailDetails.token() +
                    "\n\nNote: The confirmation token and link expires in 15 minutes.\n\n" +
                    "Best,\nMusa";

            mailMessage.setText(stringBuilder);

            mailSender.send(mailMessage);

            // Log email to database
            logEmail(emailDetails.email(), EmailTypes.VERIFICATION_CODE, stringBuilder, null);

        } catch (Exception e) {
            System.out.println("Failed to send mail " + e.getMessage());
        }
    }

    public void sendSuccessfulCreationEmail(EmailDetails emailDetails, User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("wd213230@gmail.com");
        mailMessage.setTo(emailDetails.email());
        mailMessage.setSubject("Customer");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dear ").append(emailDetails.name().toUpperCase())
                .append(", \n\n");
        stringBuilder.append("Your sign up request was successfully confirmed.\n");
        stringBuilder.append("Proceed to login page by clicking link below, enjoy our services. \n\n");
        stringBuilder.append("~Musa");
        mailMessage.setText(stringBuilder.toString());

        mailSender.send(mailMessage);

        // Log email to database
        logEmail(emailDetails.email(), EmailTypes.WELCOME_EMAIL, stringBuilder.toString(), user);
    }

    public void sendPassChangeEmail(EmailDetails emailDetails, User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("wd213230@gmail.com");
        mailMessage.setTo(emailDetails.email());
        mailMessage.setSubject("Customer");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dear ").append(emailDetails.name().toUpperCase()).append(",\n\n");
        stringBuilder.append("Your password has been successfully changed.\n\n");
        stringBuilder
                .append("If you didn't request a password reset, please contact our support team immediately.\n\n");
        stringBuilder.append(
                "Thank you for using [UJENZI LINK]. If you have any questions or need further assistance, feel free to contact our support team.\n\n");
        stringBuilder.append("Best regards,\n\n");
        stringBuilder.append("~Musa");
        mailMessage.setText(stringBuilder.toString());

        mailSender.send(mailMessage);

        // Log email to database
        logEmail(emailDetails.email(), EmailTypes.PASSWORD_CHANGED, stringBuilder.toString(), user);
    }

    public void sendPassResetEmail(EmailDetails emailDetails, User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("wd213230@gmail.com");
        mailMessage.setTo(emailDetails.email());
        mailMessage.setSubject("Customer");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dear ").append(emailDetails.name().toUpperCase()).append(",\n\n");
        stringBuilder.append("We hope this email finds you well! 😊\n\n");
        stringBuilder.append(
                "You recently requested to reset your password. To complete the password reset process, please use the following verification code:\n\n");
        stringBuilder.append("Verification Code: ").append(emailDetails.token()).append("\n\n");
        stringBuilder.append("If you didn't request a password reset, you can safely ignore this email.\n\n");
        stringBuilder.append(
                "Thank you for using [UJENZI LINK]. If you have any questions or need further assistance, feel free to contact our support team.\n\n");
        stringBuilder.append("Best regards,\n\n");
        stringBuilder.append("~Musa");
        mailMessage.setText(stringBuilder.toString());

        mailSender.send(mailMessage);

        // Log email to database
        logEmail(emailDetails.email(), EmailTypes.PASSWORD_RESET, stringBuilder.toString(), user);
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
