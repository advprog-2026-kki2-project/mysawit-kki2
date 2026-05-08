package id.ac.ui.cs.advprog.mysawit.modules.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WageConfigurationRequestDto {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal laborerWagePerKg;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal driverWagePerKg;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal foremanWagePerKg;
}
