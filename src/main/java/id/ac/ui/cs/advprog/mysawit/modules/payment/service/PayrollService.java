package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.WageConfiguration;

import java.math.BigDecimal;
import java.util.List;

public interface PayrollService {
    WageConfiguration saveWageConfiguration(
            BigDecimal laborerWagePerKg,
            BigDecimal driverWagePerKg,
            BigDecimal foremanWagePerKg
    );

    WageConfiguration getWageConfiguration();

    Payroll calculateLaborerPayroll(String laborerName, String harvestId, BigDecimal harvestedWeightKg);

    Payroll calculateDriverPayroll(String driverId, String deliveryId, BigDecimal deliveredWeightKg);

    Payroll calculateForemanPayroll(String foremanName, String deliveryId, BigDecimal recognizedWeightKg);

    List<Payroll> getPayrollHistory(String beneficiaryReference);
}
