package com.webapp.bankingportal.service;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public CompletableFuture<Void> sendEmail(
            String to, String subject, String text) {

        val future = new CompletableFuture<Void>();

        try {
            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);

            log.info("Sent email to {}", to);
            future.complete(null);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}", to, e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public String getLoginEmailTemplate(
            String name, String loginTime, String loginLocation) {

        return "<div style=\"font-family:Arial,sans-serif;max-width:640px;margin:auto;background:#f5f7f2;padding:24px;\">"
                + "<div style=\"background:#ffffff;padding:28px;border-radius:12px;\">"
                + "<div style=\"text-align:center;border-bottom:1px solid #e5e7eb;padding-bottom:18px;\">"
                + "<h1 style=\"color:#526b2d;margin:0;\">Olive Bank</h1>"
                + "<p style=\"color:#667085;margin:6px 0 0;\">Your banking security matters</p>"
                + "</div>"
                + "<div style=\"padding:20px 0;color:#333;line-height:1.7;\">"
                + "<p>Hi, " + name + ",</p>"
                + "<p>A login attempt was made on your Olive Bank account.</p>"
                + "<p>Time: <strong>" + loginTime + "</strong></p>"
                + "<p>Location: <strong>" + loginLocation + "</strong></p>"
                + "<p>If this was you, no further action is required. "
                + "If you suspect unauthorized access, change your password "
                + "and contact Olive Bank support.</p>"
                + "<p>Regards,<br><strong>The Olive Bank Team</strong></p>"
                + "</div>"
                + "<hr style=\"border:0;border-top:1px solid #e5e7eb;\">"
                + "<p style=\"text-align:center;color:#667085;font-size:12px;\">"
                + "This is an automated security notification from Olive Bank.</p>"
                + "</div></div>";
    }

    @Override
    public String getOtpLoginEmailTemplate(
            String name, String accountNumber, String otp) {

        return "<div style=\"font-family:Arial,sans-serif;max-width:640px;margin:auto;background:#f5f7f2;padding:24px;\">"
                + "<div style=\"background:#ffffff;padding:28px;border-radius:12px;\">"
                + "<div style=\"text-align:center;border-bottom:1px solid #e5e7eb;padding-bottom:18px;\">"
                + "<h1 style=\"color:#526b2d;margin:0;\">Olive Bank</h1>"
                + "<p style=\"color:#667085;margin:6px 0 0;\">Secure online banking</p>"
                + "</div>"
                + "<div style=\"padding:20px 0;color:#333;line-height:1.7;\">"
                + "<p>Hi, " + name + ",</p>"
                + "<p>Account Number: <strong>" + accountNumber + "</strong></p>"
                + "<p>Use the following one-time password (OTP) to complete your login. "
                + "It expires in " + OtpServiceImpl.OTP_EXPIRY_MINUTES
                + " minutes.</p>"
                + "<h2 style=\"background:#526b2d;display:inline-block;padding:10px 20px;color:#fff;border-radius:6px;letter-spacing:3px;\">"
                + otp + "</h2>"
                + "<p>If you did not request this code, do not share it with anyone "
                + "and contact Olive Bank support.</p>"
                + "<p>Regards,<br><strong>The Olive Bank Team</strong></p>"
                + "</div>"
                + "<hr style=\"border:0;border-top:1px solid #e5e7eb;\">"
                + "<p style=\"text-align:center;color:#667085;font-size:12px;\">"
                + "This is an automated security notification from Olive Bank.</p>"
                + "</div></div>";
    }

    @Override
    public String getWelcomeEmailTemplate(String name, String accountNumber) {

        return "<div style=\"font-family:Arial,sans-serif;max-width:640px;margin:auto;background:#f4f7f2;padding:24px;\">"
                + "<div style=\"background:#ffffff;padding:28px;border-radius:12px;\">"
                + "<h1 style=\"color:#526b2d;text-align:center;\">Olive Bank</h1>"
                + "<h2>Welcome to Olive Bank!</h2>"
                + "<p>Hello " + name + ",</p>"
                + "<p>Your account has been created successfully. "
                + "We are delighted to welcome you.</p>"
                + "<p><strong>Account number:</strong> " + accountNumber + "</p>"
                + "<p>Keep your password, PIN and one-time passcodes private. "
                + "Never share them by email.</p>"
                + "<p>Kind regards,<br><strong>The Olive Bank Team</strong></p>"
                + "<hr><small>This is an automated email. Please do not reply.</small>"
                + "</div></div>";
    }

    @Override
    public String getDepositEmailTemplate(
            String name, String accountNumber, String amount,
            String dateTime, String narration, String balance) {

        return transactionEmail(name, accountNumber, amount, dateTime,
                narration, balance, true);
    }

    @Override
    public String getTransferEmailTemplate(
            String name, String accountNumber, String amount,
            String dateTime, String narration, String balance,
            boolean credit) {

        return transactionEmail(name, accountNumber, amount, dateTime,
                narration, balance, credit);
    }

    private String transactionEmail(
            String name, String accountNumber, String amount,
            String dateTime, String narration, String balance,
            boolean credit) {

        String direction = credit ? "CR" : "DR";
        String title = credit
                ? "Transaction Credit Alert"
                : "Transaction Debit Alert";

        return "<div style=\"font-family:Arial,sans-serif;max-width:640px;margin:auto;background:#f4f7f2;padding:24px;\">"
                + "<div style=\"background:#075b42;color:white;padding:22px;text-align:center;\">"
                + "<h1>Olive Bank</h1><h2>" + title + "</h2></div>"
                + "<div style=\"background:white;padding:26px;line-height:1.7;\">"
                + "<p>Hello " + name + ",</p>"
                + "<p>Your transaction details are below.</p>"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                + emailRow("Date/Time", dateTime)
                + emailRow("Account Number", maskAccount(accountNumber))
                + emailRow("Amount", amount + " " + direction)
                + emailRow("Narration", narration)
                + emailRow("Cleared Balance", balance)
                + emailRow("Uncleared Balance", "£0.00 CR")
                + "</table>"
                + "<p>Kind regards,<br><strong>The Olive Bank Team</strong></p>"
                + "<hr><small>This is an automated email. Please do not reply.</small>"
                + "</div></div>";
    }

    private String emailRow(String label, String value) {

        return "<tr><td style=\"padding:10px;border-bottom:1px solid #ddd;color:#667085;\">"
                + label
                + "</td><td style=\"padding:10px;border-bottom:1px solid #ddd;text-align:right;\"><strong>"
                + value
                + "</strong></td></tr>";
    }

    private String maskAccount(String accountNumber) {

        if (accountNumber == null || accountNumber.length() <= 3) {
            return "****";
        }

        return "******" + accountNumber.substring(accountNumber.length() - 3);
    }

    public void sendEmailWithAttachment(
            String to, String subject, String text,
            String attachmentFilePath) {

        try {
            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            val attachmentFile = new File(attachmentFilePath);
            helper.addAttachment(attachmentFile.getName(), attachmentFile);

            mailSender.send(message);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
