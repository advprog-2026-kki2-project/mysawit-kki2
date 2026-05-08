package id.ac.ui.cs.advprog.mysawit.modules.payment.dto;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollSourceType;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PayrollResponseDto {

    private String id;
    private String beneficiaryReference;
    private Role recipientRole;
    private PayrollSourceType sourceType;
    private String sourceReferenceId;
    private BigDecimal weightKg;
    private BigDecimal wageRatePerKg;
    private BigDecimal amount;
    private PayrollStatus status;
    private String description;
    private String rejectionReason;
    private String paymentGateway;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
