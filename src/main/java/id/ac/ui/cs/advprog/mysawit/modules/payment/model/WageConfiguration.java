package id.ac.ui.cs.advprog.mysawit.modules.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wage_configuration")
public class WageConfiguration {

    public static final Long GLOBAL_CONFIGURATION_ID = 1L;

    @Id
    private Long id = GLOBAL_CONFIGURATION_ID;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal laborerWagePerKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal driverWagePerKg;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal foremanWagePerKg;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}
