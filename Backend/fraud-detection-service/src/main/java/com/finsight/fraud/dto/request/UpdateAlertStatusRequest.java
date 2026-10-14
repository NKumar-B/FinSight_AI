package com.finsight.fraud.dto.request;

import com.finsight.fraud.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlertStatusRequest {

    @NotNull(message = "Alert status is required")
    private AlertStatus status;

    private String assignedTo;

    private String resolutionNotes;
}
