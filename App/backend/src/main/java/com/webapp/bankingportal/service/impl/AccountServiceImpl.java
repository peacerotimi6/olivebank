
package com.webapp.bankingportal.service.impl;

import com.webapp.bankingportal.exception.FundTransferException;
import com.webapp.bankingportal.dto.AccountResponse;
import com.webapp.bankingportal.entity.Account;
import com.webapp.bankingportal.entity.Transaction;
import com.webapp.bankingportal.entity.TransactionType;
import com.webapp.bankingportal.entity.User;
import com.webapp.bankingportal.exception.InsufficientBalanceException;
import com.webapp.bankingportal.exception.InvalidAmountException;
import com.webapp.bankingportal.exception.InvalidPinException;
import com.webapp.bankingportal.exception.NotFoundException;
import com.webapp.bankingportal.exception.UnauthorizedException;
import com.webapp.bankingportal.repository.AccountRepository;
import com.webapp.bankingportal.repository.TransactionRepository;
import com.webapp.bankingportal.repository.UserRepository;
import com.webapp.bankingportal.service.AccountService;
import com.webapp.bankingportal.service.EmailService;
import com.webapp.bankingportal.util.ApiMessages;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final Logger log =
            LoggerFactory.getLogger(AccountServiceImpl.class);

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("100000.00");

    // Create a new account with Olive Bank demo details
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        try {
            log.info("Creating new account for user: {}", user.getId());

            Account account = new Account();
            account.setUser(user);
            account.setBalance(BigDecimal.ZERO);
            account.setAccountNumber(generateAccountNumber());

            // Demo details for Olive Bank
            account.setSortCode("04-00-04");
            account.setBranch("Olive Bank Demo Branch");

            Account savedAccount = accountRepository.save(account);
            accountRepository.flush();

            log.info(
                    "Account saved with ID: {} and number: {}",
                    savedAccount.getId(),
                    savedAccount.getAccountNumber()
            );

            return savedAccount;

        } catch (Exception e) {
            log.error("Failed to create account", e);
            throw e;
        }
    }

    // Generate a six-digit demo account number
    private String generateAccountNumber() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordValid(String accountNumber, String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        User user = userRepository.findById(account.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return user.getPassword() != null
                && passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public void createPin(String accountNumber, String pin) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getPin() != null) {
            throw new UnauthorizedException("PIN already exists");
        }

        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new InvalidPinException(
                    "PIN must contain exactly four digits"
            );
        }

        account.setPin(passwordEncoder.encode(pin));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updatePin(
            String accountNumber,
            String oldPin,
            String newPin
    ) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (oldPin == null || oldPin.isEmpty()) {
            throw new UnauthorizedException(
                    ApiMessages.PIN_EMPTY_ERROR.getMessage()
            );
        }

        if (account.getPin() == null
                || !passwordEncoder.matches(oldPin, account.getPin())) {
            throw new UnauthorizedException(
                    ApiMessages.PIN_INVALID_ERROR.getMessage()
            );
        }

        if (newPin == null || newPin.isEmpty()) {
            throw new InvalidPinException(
                    ApiMessages.PIN_EMPTY_ERROR.getMessage()
            );
        }

        if (!newPin.matches("[0-9]{4}")) {
            throw new InvalidPinException(
                    ApiMessages.PIN_FORMAT_INVALID_ERROR.getMessage()
            );
        }

        account.setPin(passwordEncoder.encode(newPin));
        accountRepository.save(account);
    }

    @Override
    public boolean isPinCreated(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> account.getPin() != null)
                .orElse(false);
    }

    @Override
    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        accountRepository.delete(account);
    }

    private void validateAccountAndPin(
            String accountNumber,
            String pin
    ) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (pin == null || pin.isEmpty()) {
            throw new UnauthorizedException(
                    ApiMessages.PIN_EMPTY_ERROR.getMessage()
            );
        }

        if (account.getPin() == null
                || !passwordEncoder.matches(pin, account.getPin())) {
            throw new UnauthorizedException(
                    ApiMessages.PIN_INVALID_ERROR.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public AccountResponse cashDeposit(
            String accountNumber,
            String pin,
            BigDecimal amount
    ) {
        log.info(
                "Processing cash deposit for account: {} with amount: {}",
                accountNumber,
                amount
        );

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_NEGATIVE_ERROR.getMessage()
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_EXCEED_100_000_ERROR.getMessage()
            );
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        validateAccountAndPin(accountNumber, pin);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        account = accountRepository.save(account);

        if (account.getId() == null) {
            throw new RuntimeException(
                    "Failed to save account - no ID generated"
            );
        }

        log.info(
                "Account updated with ID: {} and new balance: {}",
                account.getId(),
                newBalance
        );

        Account verifiedAccount = accountRepository.findById(account.getId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found after save"));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_DEPOSIT);
        transaction.setTransactionDate(new Date());
        transaction.setSourceAccount(verifiedAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (savedTransaction.getId() == null) {
            throw new RuntimeException(
                    "Failed to save transaction - no ID generated"
            );
        }

        log.info(
                "Transaction saved with ID: {} for account: {}",
                savedTransaction.getId(),
                accountNumber
        );

        // Send deposit confirmation email
        User user = verifiedAccount.getUser();

        emailService.sendEmail(
                user.getEmail(),
                "Olive Bank - Deposit Confirmation",
                emailService.getDepositEmailTemplate(
                        user.getName(),
                        verifiedAccount.getAccountNumber(),
                        formatMoney(amount),
                        formatDate(new Date()),
                        "Cash deposit",
                        formatBalance(verifiedAccount.getBalance())
                )
        );

        return new AccountResponse(verifiedAccount);
    }

    @Override
    @Transactional
    public AccountResponse cashWithdrawal(
            String accountNumber,
            String pin,
            BigDecimal amount
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_NEGATIVE_ERROR.getMessage()
            );
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        validateAccountAndPin(accountNumber, pin);

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_EXCEED_100_000_ERROR.getMessage()
            );
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    ApiMessages.BALANCE_INSUFFICIENT_ERROR.getMessage()
            );
        }

        // Deduct the withdrawal amount
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Save the withdrawal transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_WITHDRAWAL);
        transaction.setTransactionDate(new Date());
        transaction.setSourceAccount(account);

        transactionRepository.save(transaction);

        // Send a debit alert to the account holder
        User user = account.getUser();

        emailService.sendEmail(
                user.getEmail(),
                "Olive Bank - Debit Alert",
                emailService.getTransferEmailTemplate(
                        user.getName(),
                        account.getAccountNumber(),
                        formatMoney(amount),
                        formatDate(new Date()),
                        "Cash withdrawal",
                        formatBalance(account.getBalance()),
                        false
                )
        );

        return new AccountResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse fundTransfer(
            String sourceAccountNumber,
            String targetAccountNumber,
            BigDecimal amount,
            String pin
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_NEGATIVE_ERROR.getMessage()
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                    ApiMessages.AMOUNT_EXCEED_100_000_ERROR.getMessage()
            );
        }

        if (sourceAccountNumber != null
                && sourceAccountNumber.equals(targetAccountNumber)) {
            throw new FundTransferException(
                    ApiMessages.CASH_TRANSFER_SAME_ACCOUNT_ERROR.getMessage()
            );
        }

        Account sourceAccount =
                accountRepository.findByAccountNumber(sourceAccountNumber)
                        .orElseThrow(() ->
                                new NotFoundException("Source account not found"));

        Account targetAccount =
                accountRepository.findByAccountNumber(targetAccountNumber)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        ApiMessages.ACCOUNT_NOT_FOUND.getMessage()
                                ));

        validateAccountAndPin(sourceAccountNumber, pin);

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    ApiMessages.BALANCE_INSUFFICIENT_ERROR.getMessage()
            );
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount)
        );

        targetAccount.setBalance(
                targetAccount.getBalance().add(amount)
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.CASH_TRANSFER);
        transaction.setTransactionDate(new Date());
        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);

        transactionRepository.save(transaction);

        // Notify the sender
        User sender = sourceAccount.getUser();

        emailService.sendEmail(
                sender.getEmail(),
                "Olive Bank - Transfer Confirmation",
                emailService.getTransferEmailTemplate(
                        sender.getName(),
                        sourceAccount.getAccountNumber(),
                        formatMoney(amount),
                        formatDate(new Date()),
                        "Transfer to account ending "
                                + lastThree(targetAccount.getAccountNumber()),
                        formatBalance(sourceAccount.getBalance()),
                        false
                )
        );

        // Notify the recipient
        User recipient = targetAccount.getUser();

        emailService.sendEmail(
                recipient.getEmail(),
                "Olive Bank - Money Received",
                emailService.getTransferEmailTemplate(
                        recipient.getName(),
                        targetAccount.getAccountNumber(),
                        formatMoney(amount),
                        formatDate(new Date()),
                        "Transfer received from account ending "
                                + lastThree(sourceAccount.getAccountNumber()),
                        formatBalance(targetAccount.getBalance()),
                        true
                )
        );

        return new AccountResponse(sourceAccount);
    }

    private static String formatMoney(BigDecimal amount) {
        return String.format(java.util.Locale.UK, "£%,.2f", amount);
    }

    private static String formatBalance(BigDecimal balance) {
        String direction = balance.signum() < 0 ? " DR" : " CR";
        return formatMoney(balance) + direction;
    }

    private static String formatDate(Date date) {
        return new java.text.SimpleDateFormat(
                "dd-MMM-yyyy hh:mm a", java.util.Locale.UK
        ).format(date);
    }

    private static String lastThree(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 3) {
            return "***";
        }

        return accountNumber.substring(accountNumber.length() - 3);
    }

    @Override
    public AccountResponse getAccountInfo(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        return new AccountResponse(account);
    }
}
