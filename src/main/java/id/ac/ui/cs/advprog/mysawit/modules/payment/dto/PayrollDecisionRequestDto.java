package id.ac.ui.cs.advprog.mysawit.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollDecisionRequestDto {

    @NotBlank
    private String reason;
}
