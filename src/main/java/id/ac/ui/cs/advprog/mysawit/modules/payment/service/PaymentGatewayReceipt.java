package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

public record PaymentGatewayReceipt(
        String gatewayName,
        String paymentReference
) {
}
