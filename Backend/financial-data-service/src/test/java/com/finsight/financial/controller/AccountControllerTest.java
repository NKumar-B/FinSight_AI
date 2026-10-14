package com.finsight.financial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.financial.dto.request.CreateAccountRequest;
import com.finsight.financial.dto.response.AccountResponse;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.AccountType;
import com.finsight.financial.security.JwtAuthenticationFilter;
import com.finsight.financial.service.AccountService;
import com.finsight.common.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void testCreateAccountValidPayloadReturnsCreated() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("ACC-7788")
                .accountName("Treasury Reserve")
                .accountType(AccountType.INVESTMENT)
                .currency("USD")
                .initialBalance(new BigDecimal("100000.00"))
                .institutionName("JPMorgan")
                .build();

        AccountResponse response = AccountResponse.builder()
                .id(1L)
                .accountNumber("ACC-7788")
                .accountName("Treasury Reserve")
                .accountType(AccountType.INVESTMENT)
                .currency("USD")
                .balance(new BigDecimal("100000.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC-7788"))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }

    @Test
    void testCreateAccountInvalidPayloadReturnsBadRequest() throws Exception {
        CreateAccountRequest invalidRequest = CreateAccountRequest.builder()
                .accountNumber("")
                .currency("INVALID")
                .build();

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testGetAccountByIdReturnsOk() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .id(5L)
                .accountNumber("ACC-5555")
                .accountName("Checking Main")
                .currency("USD")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountService.getAccountById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC-5555"));
    }
}
