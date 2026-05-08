package id.ac.ui.cs.advprog.mysawit.modules.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PayrollDashboardResponseDto {

    private long totalEntries;
    private long pendingCount;
    private long acceptedCount;
    private long rejectedCount;
    private BigDecimal totalAmount;
    private BigDecimal totalAcceptedAmount;
    private BigDecimal totalPendingAmount;
    private List<PayrollResponseDto> payrolls;
}
