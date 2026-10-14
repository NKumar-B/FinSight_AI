package com.finsight.financial.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.request.CreateAccountRequest;
import com.finsight.financial.dto.response.AccountResponse;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    PagedResponse<AccountResponse> listAccounts(String search, Pageable pageable);
    AccountResponse getAccountById(Long id);
    AccountResponse getAccountByNumber(String accountNumber);
}
