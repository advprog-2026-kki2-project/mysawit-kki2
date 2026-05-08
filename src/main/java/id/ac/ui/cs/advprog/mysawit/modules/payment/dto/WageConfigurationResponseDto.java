package id.ac.ui.cs.advprog.mysawit.modules.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WageConfigurationResponseDto {

    private BigDecimal laborerWagePerKg;
    private BigDecimal driverWagePerKg;
    private BigDecimal foremanWagePerKg;
    private LocalDateTime updatedAt;
}
