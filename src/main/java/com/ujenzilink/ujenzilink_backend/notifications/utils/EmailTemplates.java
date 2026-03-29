package com.ujenzilink.ujenzilink_backend.notifications.utils;

public class EmailTemplates {

    public static String getVerificationCodeEmail(String name, String token, String email, String frontendUrl) {
        String confirmationLink = frontendUrl + "/auth/confirm?token=" + token + "&email=" + email;
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Thanks for registering. Please confirm your account using the code or link below:\n" +
                "Code: " + token + "\n" +
                "Link: " + confirmationLink +
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

    public static String getProjectInvitationEmail(String name, String projectName, String inviterName) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "You have been added to the project '" + projectName + "' by " + inviterName + ".\n\n" +
                "You can now access the project details and collaborate with the team.\n\n" +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getAccountLockedEmail(String name) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Your account has been temporarily locked due to multiple failed login attempts.\n\n" +
                "To unlock your account, please reset your password or contact our support team.\n\n" +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getAccountDeletionEmail(String name) {
        return "Dear " + name.toUpperCase() + ",<br><br>" +
                "This action will initiate the permanent deletion of your account. All your data will be erased after 14 days.<br><br>" +
                "Your account will be deleted permanently after <span class=\"text-slate-900 font-bold\">14 days</span>. " +
                "Until then, you can reclaim your account by logging back in.<br><br>" +
                "If you did not initiate this request, please secure your account immediately.<br><br>" +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getAccountDeletionRevertEmail(String name) {
        return "Dear " + name.toUpperCase() + ",<br><br>" +
                "We are pleased to inform you that your account deletion request has been successfully reverted by an administrator.<br><br>" +
                "Your account is now fully restored, and you can continue to use our services as usual.<br><br>" +
                "If you have any questions, please feel free to reach out to our support team.<br><br>" +
                "Best regards,<br><br>" +
                "~Musa";
    }

    public static String getAdminVerifiedEmail(String name) {
        return "Dear " + name.toUpperCase() + ",\n\n" +
                "Great news! Your account on UJENZI LINK has been manually verified by one of our administrators.\n\n" +
                "You can now log in and start using all features of the platform.\n\n" +
                "If you have any questions or need assistance getting started, feel free to reach out to our support team.\n\n" +
                "Best regards,\n\n" +
                "~Musa";
    }

    public static String getBackupFailureReportEmail(String hostname, String timestamp, String backupFile, String step,
            String errorMessage, String destHost, String destDir) {
        return "URGENT: Backup Failure Detected\n\n" +
                "Hostname: " + hostname + "\n" +
                "Timestamp: " + timestamp + "\n" +
                "Backup File: " + backupFile + "\n" +
                "Step: " + step + "\n" +
                "Error Message: " + errorMessage + "\n" +
                "Destination Host: " + destHost + "\n" +
                "Destination Directory: " + destDir + "\n\n" +
                "Please investigate this issue immediately.\n\n" +
                "~Musa System Monitor";
    }
}

