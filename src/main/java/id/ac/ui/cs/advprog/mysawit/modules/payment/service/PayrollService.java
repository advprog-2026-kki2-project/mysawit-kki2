package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.WageConfiguration;
import id.ac.ui.cs.advprog.mysawit.core.model.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    List<Payroll> getPayrollHistory(
            String beneficiaryReference,
            LocalDate startDate,
            LocalDate endDate,
            PayrollStatus status
    );

    List<Payroll> getAdminPayrolls(
            String beneficiaryReference,
            Role recipientRole,
            PayrollStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    Payroll approvePayroll(String payrollId);

    Payroll rejectPayroll(String payrollId, String rejectionReason);
}
