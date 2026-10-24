package com.webapp.bankingportal.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    CompletableFuture<Void> sendEmail(String to, String subject, String text);

    String getLoginEmailTemplate(
            String name, String loginTime, String loginLocation);

    String getOtpLoginEmailTemplate(
            String name, String accountNumber, String otp);

    String getWelcomeEmailTemplate(
            String name, String accountNumber);

    String getDepositEmailTemplate(
            String name, String accountNumber, String amount,
            String dateTime, String narration, String balance);

    String getTransferEmailTemplate(
            String name, String accountNumber, String amount,
            String dateTime, String narration, String balance,
            boolean credit);
}