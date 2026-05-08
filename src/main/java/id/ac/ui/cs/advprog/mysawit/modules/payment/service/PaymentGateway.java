package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;

public interface PaymentGateway {

    PaymentGatewayReceipt process(Payroll payroll);
}
