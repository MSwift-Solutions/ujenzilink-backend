package com.ujenzilink.ujenzilink_backend.notifications.utils;

public class EmailTemplates {

    private static String getRegards(String frontendUrl) {
        return "<br><br>" +
                "<p style='margin: 0;'>Best regards,</p>" +
                "<p style='margin: 0; font-weight: 600; color: #f28c18;'>Ujenzilink Team</p>" +
                "<p style='margin: 0;'><a href='mailto:ujenzilink@gmail.com' style='color: #1f2f57; text-decoration: none;'>ujenzilink@gmail.com</a> | 0727587966</p>"
                +
                "<p style='margin: 0;'><a href='" + frontendUrl + "' style='color: #f28c18; text-decoration: none;'>"
                + frontendUrl + "</a></p>";
    }

    private static String wrap(String content, String frontendUrl) {
        return "<div style='font-family: Arial, sans-serif; font-size: 15px; color: #2e2e2e; line-height: 1.6; max-width: 600px; padding: 16px;'>"
                +
                content +
                getRegards(frontendUrl) +
                "</div>";
    }

    public static String getVerificationCodeEmail(String name, String token, String email, String frontendUrl) {
        String confirmationLink = frontendUrl + "/auth/confirm?token=" + token + "&email=" + email;
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Thanks for registering. Please confirm your account using the code or link below:</p>" +
                "<p><strong>Code:</strong> " + token + "<br>" +
                "<strong>Link:</strong> <a href='" + confirmationLink + "'>" + confirmationLink + "</a></p>" +
                "<p><em>Note: The confirmation token and link expires in 15 minutes.</em></p>";
        return wrap(content, frontendUrl);
    }

    public static String getWelcomeEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Your sign up request was successfully confirmed.</p>" +
                "<p>Proceed to the login page to enjoy our services.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getWelcomeEmail(String name) {
        return getWelcomeEmail(name, "https://ujenzilink.com");
    }

    public static String getPasswordChangedEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Your password has been successfully changed.</p>" +
                "<p>If you didn't request a password reset, please contact our support team immediately.</p>" +
                "<p>Thank you for using Ujenzilink. If you have any questions or need further assistance, feel free to contact our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getPasswordChangedEmail(String name) {
        return getPasswordChangedEmail(name, "https://ujenzilink.com");
    }

    public static String getPasswordResetEmail(String name, String token, String email, String frontendUrl) {
        String resetLink = frontendUrl + "/auth/forgot-password?email=" + email + "&token=" + token;
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>We hope this email finds you well! </p>" +
                "<p>You recently requested to reset your password. To complete the password reset process, please use the following verification code or link below:</p>"
                +
                "<p><strong>Verification Code:</strong> " + token + "<br>" +
                "<strong>Link:</strong> <a href='" + resetLink + "'>" + resetLink + "</a></p>" +
                "<p>If you didn't request a password reset, you can safely ignore this email.</p>" +
                "<p>Thank you for using Ujenzilink. If you have any questions or need further assistance, feel free to contact our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getPasswordResetEmail(String name, String token, String email) {
        return getPasswordResetEmail(name, token, email, "https://ujenzilink.com");
    }

    public static String getSignInNotificationEmail(String name, String loginTime, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>We detected a new sign-in to your account at " + loginTime + ".</p>" +
                "<p>If this was you, you can safely ignore this email.</p>" +
                "<p>If you didn't sign in, please secure your account immediately by changing your password.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getSignInNotificationEmail(String name, String loginTime) {
        return getSignInNotificationEmail(name, loginTime, "https://ujenzilink.com");
    }

    public static String getSignUpNotificationEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Welcome to Ujenzilink! We're excited to have you on board.</p>" +
                "<p>Get started by exploring our features and connecting with the community.</p>" +
                "<p>If you have any questions, feel free to reach out to our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getSignUpNotificationEmail(String name) {
        return getSignUpNotificationEmail(name, "https://ujenzilink.com");
    }

    public static String getProjectInvitationEmail(String name, String projectName, String inviterName,
            String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>You have been added to the project '<strong>" + projectName + "</strong>' by " + inviterName
                + ".</p>" +
                "<p>You can now access the project details and collaborate with the team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getProjectInvitationEmail(String name, String projectName, String inviterName) {
        return getProjectInvitationEmail(name, projectName, inviterName, "https://ujenzilink.com");
    }

    public static String getAccountLockedEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Your account has been temporarily locked due to multiple failed login attempts.</p>" +
                "<p>To unlock your account, please reset your password or contact our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountLockedEmail(String name) {
        return getAccountLockedEmail(name, "https://ujenzilink.com");
    }

    public static String getAccountDeletionEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>This action will initiate the permanent deletion of your account. All your data will be erased after 14 days.</p>"
                +
                "<p>Your account will be deleted permanently after <strong>14 days</strong>. " +
                "Until then, you can reclaim your account by logging back in.</p>" +
                "<p>If you did not initiate this request, please secure your account immediately.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountDeletionEmail(String name) {
        return getAccountDeletionEmail(name, "https://ujenzilink.com");
    }

    public static String getAccountDeletionRevertEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>We are pleased to inform you that your account deletion request has been successfully reverted by an administrator.</p>"
                +
                "<p>Your account is now fully restored, and you can continue to use our services as usual.</p>" +
                "<p>If you have any questions, please feel free to reach out to our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountDeletionRevertEmail(String name) {
        return getAccountDeletionRevertEmail(name, "https://ujenzilink.com");
    }

    public static String getAdminVerifiedEmail(String name, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Great news! Your account on Ujenzilink has been manually verified by one of our administrators.</p>"
                +
                "<p>You can now log in and start using all features of the platform.</p>" +
                "<p>If you have any questions or need assistance getting started, feel free to reach out to our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAdminVerifiedEmail(String name) {
        return getAdminVerifiedEmail(name, "https://ujenzilink.com");
    }

    public static String getBackupFailureReportEmail(String hostname, String timestamp, String backupFile, String step,
            String errorMessage, String destHost, String destDir) {
        String frontendUrl = "https://ujenzilink.com";
        String content = "<p><strong>URGENT: Backup Failure Detected</strong></p>" +
                "<ul>" +
                "<li><strong>Hostname:</strong> " + hostname + "</li>" +
                "<li><strong>Timestamp:</strong> " + timestamp + "</li>" +
                "<li><strong>Backup File:</strong> " + backupFile + "</li>" +
                "<li><strong>Step:</strong> " + step + "</li>" +
                "<li><strong>Error Message:</strong> <span style='color: red;'>" + errorMessage + "</span></li>" +
                "<li><strong>Destination Host:</strong> " + destHost + "</li>" +
                "<li><strong>Destination Directory:</strong> " + destDir + "</li>" +
                "</ul>" +
                "<p>Please investigate this issue immediately.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountSuspendedEmail(String name, String reason, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Your account has been suspended by an administrator.</p>" +
                "<p><strong>Reason:</strong> " + reason + "</p>" +
                "<p>If you believe this is a mistake, please contact our support team.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountSuspendedEmail(String name, String reason) {
        return getAccountSuspendedEmail(name, reason, "https://ujenzilink.com");
    }

    public static String getAccountUnsuspendedEmail(String name, String reason, String frontendUrl) {
        String content = "<p>Dear <strong>" + name.toUpperCase() + "</strong>,</p>" +
                "<p>Your account suspension has been lifted by an administrator.</p>" +
                "<p><strong>Reason:</strong> " + reason + "</p>" +
                "<p>You can now log in and use our services as usual.</p>";
        return wrap(content, frontendUrl);
    }

    public static String getAccountUnsuspendedEmail(String name, String reason) {
        return getAccountUnsuspendedEmail(name, reason, "https://ujenzilink.com");
    }
}
