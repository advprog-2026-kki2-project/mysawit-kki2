package id.ac.ui.cs.advprog.mysawit.transport.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Central Admin to approve, reject, or partially reject a delivery.
 * For partial rejection, recognizedWeight specifies how much weight the factory accepts.
 */
@Getter
@Setter
public class AdminVerificationDto {
    private boolean approved;
    private Double recognizedWeight;
    private String rejectionReason;
}
