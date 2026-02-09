package com.ujenzilink.ujenzilink_backend.notifications.utils;

public class EmailTemplates {

    public static String getVerificationCodeEmail(String name, String token) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Thanks for registering. Please confirm your account using the code or link below:\n" +
                "Code: " + token + "\n" +
                "Link: http://localhost:8080/auth/confirm?token=" + token +
                "\n\nNote: The confirmation token and link expires in 15 minutes.\n\n" +
                "Best,\nMusa";
    }

    public static String getWelcomeEmail(String name) {
        return "Dear " + name.toUpperCase() + ", \n\n" +
                "Your sign up request was successfully confirmed.\n" +
                "Proceed to login page by clicking link below, enjoy our services. \n\n" +
                "~Musa";
    }

    public static String getPasswordChangedEmail(String name) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Your password has been successfully changed.\n\n" +
                "If you didn't request a password reset, please contact our support team immediately.\n\n" +
                "Thank you for using [UJENZI LINK]. If you have any questions or need further assistance, feel free to contact our support team.\n\n"
                +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getPasswordResetEmail(String name, String token) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "We hope this email finds you well! 😊\n\n" +
                "You recently requested to reset your password. To complete the password reset process, please use the following verification code:\n\n"
                +
                "Verification Code: " + token + "\n\n" +
                "If you didn't request a password reset, you can safely ignore this email.\n\n" +
                "Thank you for using [UJENZI LINK]. If you have any questions or need further assistance, feel free to contact our support team.\n\n"
                +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getSignInNotificationEmail(String name, String loginTime) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "We detected a new sign-in to your account at " + loginTime + ".\n\n" +
                "If this was you, you can safely ignore this email.\n\n" +
                "If you didn't sign in, please secure your account immediately by changing your password.\n\n" +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getSignUpNotificationEmail(String name) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Welcome to UJENZI LINK! We're excited to have you on board.\n\n" +
                "Get started by exploring our features and connecting with the community.\n\n" +
                "If you have any questions, feel free to reach out to our support team.\n\n" +
                "Best regards,\n\n" +
                "~Musa";
    }
}
