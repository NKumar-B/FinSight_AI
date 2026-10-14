package com.finsight.financial.dto.request;

import com.finsight.financial.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionStatusRequest {

    @NotNull(message = "New transaction status is required")
    private TransactionStatus status;

    private String reason;
}
