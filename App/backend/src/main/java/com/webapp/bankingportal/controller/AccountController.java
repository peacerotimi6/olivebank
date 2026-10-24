package com.webapp.bankingportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webapp.bankingportal.dto.AmountRequest;
import com.webapp.bankingportal.dto.FundTransferRequest;
import com.webapp.bankingportal.dto.PinRequest;
import com.webapp.bankingportal.dto.PinUpdateRequest;
import com.webapp.bankingportal.service.AccountService;
import com.webapp.bankingportal.service.TransactionService;
import com.webapp.bankingportal.util.ApiMessages;
import com.webapp.bankingportal.util.JsonUtil;
import com.webapp.bankingportal.util.LoggedinUser;
import com.webapp.bankingportal.dto.ApiResponse;
import com.webapp.bankingportal.dto.AccountResponse;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/account", "/api/accounts"})
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @GetMapping("/pin/check")
    public ResponseEntity<Map<String, Boolean>> checkAccountPIN() {
        val isPINValid = accountService.isPinCreated(LoggedinUser.getAccountNumber());
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPIN", isPINValid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pin/create")
    public ResponseEntity<String> createPIN(@RequestBody PinRequest pinRequest) {
        String accountNumber = LoggedinUser.getAccountNumber();

        if (pinRequest.password() == null || pinRequest.password().isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiMessages.PASSWORD_EMPTY_ERROR.getMessage());
        }

        if (!accountService.isPasswordValid(accountNumber, pinRequest.password())) {
            return ResponseEntity.status(401)
                    .body(ApiMessages.PASSWORD_INVALID_ERROR.getMessage());
        }

        if (pinRequest.pin() == null || pinRequest.pin().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiMessages.PIN_EMPTY_ERROR.getMessage());
        }

        if (!pinRequest.pin().matches("[0-9]{4}")) {
            return ResponseEntity.badRequest()
                    .body(ApiMessages.PIN_FORMAT_INVALID_ERROR.getMessage());
        }

        accountService.createPin(accountNumber, pinRequest.pin());
        return ResponseEntity.ok(ApiMessages.PIN_CREATION_SUCCESS.getMessage());
    }

    @PostMapping("/pin/update")
    public ResponseEntity<String> updatePIN(@RequestBody PinUpdateRequest pinUpdateRequest) {
        // Validate password
        if (pinUpdateRequest.password() == null || pinUpdateRequest.password().isEmpty()) {
            return ResponseEntity.status(401).body(ApiMessages.PASSWORD_EMPTY_ERROR.getMessage());
        }
        if (!accountService.isPasswordValid(
                LoggedinUser.getAccountNumber(), pinUpdateRequest.password())) {
            return ResponseEntity.status(401).body(ApiMessages.PASSWORD_INVALID_ERROR.getMessage());
        }

        if (pinUpdateRequest.oldPin() == null || pinUpdateRequest.oldPin().isEmpty()) {
            return ResponseEntity.status(401).body(ApiMessages.PIN_EMPTY_ERROR.getMessage());
        }

        if (pinUpdateRequest.newPin() == null || pinUpdateRequest.newPin().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiMessages.PIN_EMPTY_ERROR.getMessage());
        }

        // Validate PIN format
        if (!pinUpdateRequest.newPin().matches("[0-9]{4}")) {
            return ResponseEntity.badRequest().body(ApiMessages.PIN_FORMAT_INVALID_ERROR.getMessage());
        }

        accountService.updatePin(
                LoggedinUser.getAccountNumber(),
                pinUpdateRequest.oldPin(),
                pinUpdateRequest.newPin());

        return ResponseEntity.ok(ApiMessages.PIN_UPDATE_SUCCESS.getMessage());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> cashDeposit(@RequestBody AmountRequest request) {
        log.info("Processing cash deposit request");
        AccountResponse response = accountService.cashDeposit(LoggedinUser.getAccountNumber(), request.pin(), request.amount());
        return ResponseEntity.ok(ApiMessages.CASH_DEPOSIT_SUCCESS.getMessage());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> cashWithdrawal(@RequestBody AmountRequest request) {
        log.info("Processing cash withdrawal request");
        accountService.cashWithdrawal(LoggedinUser.getAccountNumber(), request.pin(), request.amount());
        return ResponseEntity.ok(ApiMessages.CASH_WITHDRAWAL_SUCCESS.getMessage());
    }

    @PostMapping({"/transfer", "/fund-transfer"})
    public ResponseEntity<String> fundTransfer(@RequestBody FundTransferRequest request) {
        log.info("Processing fund transfer request to account: {}", request.targetAccountNumber());
        accountService.fundTransfer(LoggedinUser.getAccountNumber(), request.targetAccountNumber(), request.amount(), request.pin());
        return ResponseEntity.ok(ApiMessages.CASH_TRANSFER_SUCCESS.getMessage());
    }

    @GetMapping("/transactions")
    public ResponseEntity<String> getAllTransactionsByAccountNumber() {
        val transactions = transactionService
                .getAllTransactionsByAccountNumber(LoggedinUser.getAccountNumber());
        return ResponseEntity.ok(JsonUtil.toJson(transactions));
    }

}
