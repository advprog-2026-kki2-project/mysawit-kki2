package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SandboxPaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "SANDBOX_GATEWAY";

    @Override
    public PaymentGatewayReceipt process(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll is required.");
        }

        if (payroll.getAmount() == null || payroll.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Payroll amount must be greater than 0.");
        }

        String reference = "SBX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return new PaymentGatewayReceipt(GATEWAY_NAME, reference);
    }
}
