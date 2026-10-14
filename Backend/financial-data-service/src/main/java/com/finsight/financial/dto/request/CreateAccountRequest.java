package com.finsight.financial.dto.request;

import com.finsight.financial.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account number is required")
    @Size(min = 3, max = 50, message = "Account number must be between 3 and 50 characters")
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g. USD, EUR, INR)")
    private String currency;

    @PositiveOrZero(message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance;

    @Size(max = 100, message = "Institution name must not exceed 100 characters")
    private String institutionName;
}
