package com.bank.PAYMENT_SYSTEM.account.service;



import com.bank.PAYMENT_SYSTEM.account.dto.AccountResponse;
import com.bank.PAYMENT_SYSTEM.account.dto.CreateAccountRequest;
import com.bank.PAYMENT_SYSTEM.account.entity.Account;
import com.bank.PAYMENT_SYSTEM.account.mapper.AccountMapper;
import com.bank.PAYMENT_SYSTEM.account.repository.AccountRepository;
import com.bank.PAYMENT_SYSTEM.common.enums.AccountStatus;
import com.bank.PAYMENT_SYSTEM.common.util.AccountNumberGenerator;
import com.bank.PAYMENT_SYSTEM.exception.ResourceNotFoundException;
import com.bank.PAYMENT_SYSTEM.user.entity.User;
import com.bank.PAYMENT_SYSTEM.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // CREATE ACCOUNT

    public AccountResponse createAccount(
            CreateAccountRequest request) {

        User user = userRepository.findById(
                        request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Account account = new Account();

        account.setUser(user);

        account.setAccountType(
                request.getAccountType());

        account.setAccountNumber(
                AccountNumberGenerator
                        .generateAccountNumber());

        Account savedAccount =
                accountRepository.save(account);

        return AccountMapper.toResponse(
                savedAccount);
    }

    // GET ACCOUNT BY ID

    public AccountResponse getAccountById(
            UUID accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found"));

        return AccountMapper.toResponse(account);
    }

    // FREEZE ACCOUNT

    public String freezeAccount(UUID accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found"));

        account.setAccountStatus(
                AccountStatus.FROZEN);

        accountRepository.save(account);

        return "Account frozen successfully";
    }

    // UNFREEZE ACCOUNT

    public String unfreezeAccount(
            UUID accountId) {

        Account account =
                accountRepository.findById(accountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found"));

        account.setAccountStatus(
                AccountStatus.ACTIVE);

        accountRepository.save(account);

        return "Account unfrozen successfully";
    }
}
