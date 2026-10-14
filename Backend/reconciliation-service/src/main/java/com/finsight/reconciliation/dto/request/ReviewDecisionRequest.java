package com.finsight.reconciliation.dto.request;

import com.finsight.reconciliation.enums.DecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDecisionRequest {

    @NotNull(message = "Decision type is required")
    private DecisionType decision;

    @NotBlank(message = "Review reason or justification is required")
    private String reason;
}
