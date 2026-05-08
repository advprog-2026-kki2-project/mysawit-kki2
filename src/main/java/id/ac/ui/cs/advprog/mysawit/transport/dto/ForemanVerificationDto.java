package id.ac.ui.cs.advprog.mysawit.transport.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Foreman to approve or reject an arrived delivery.
 */
@Getter
@Setter
public class ForemanVerificationDto {
    private boolean approved;
    private String rejectionReason;
}
