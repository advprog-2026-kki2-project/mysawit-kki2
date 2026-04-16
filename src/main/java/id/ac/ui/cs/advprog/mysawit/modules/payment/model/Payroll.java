package id.ac.ui.cs.advprog.mysawit.modules.payment.model;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String beneficiaryReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role recipientRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollSourceType sourceType;

    @Column(nullable = false)
    private String sourceReferenceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal wageRatePerKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
