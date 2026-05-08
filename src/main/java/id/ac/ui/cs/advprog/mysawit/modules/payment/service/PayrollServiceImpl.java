package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollSourceType;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.WageConfiguration;
import id.ac.ui.cs.advprog.mysawit.modules.payment.repository.PayrollRepository;
import id.ac.ui.cs.advprog.mysawit.modules.payment.repository.WageConfigurationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PayrollServiceImpl implements PayrollService {

    private static final BigDecimal PAYROLL_SHARE = new BigDecimal("0.90");
    private static final int MONEY_SCALE = 2;

    private final PayrollRepository payrollRepository;
    private final WageConfigurationRepository wageConfigurationRepository;
    private final PaymentGateway paymentGateway;

    public PayrollServiceImpl(PayrollRepository payrollRepository,
                              WageConfigurationRepository wageConfigurationRepository,
                              PaymentGateway paymentGateway) {
        this.payrollRepository = payrollRepository;
        this.wageConfigurationRepository = wageConfigurationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional
    public WageConfiguration saveWageConfiguration(BigDecimal laborerWagePerKg,
                                                   BigDecimal driverWagePerKg,
                                                   BigDecimal foremanWagePerKg) {
        WageConfiguration configuration = wageConfigurationRepository
                .findById(WageConfiguration.GLOBAL_CONFIGURATION_ID)
                .orElseGet(WageConfiguration::new);

        configuration.setLaborerWagePerKg(validateRate(laborerWagePerKg, "Laborer wage"));
        configuration.setDriverWagePerKg(validateRate(driverWagePerKg, "Driver wage"));
        configuration.setForemanWagePerKg(validateRate(foremanWagePerKg, "Foreman wage"));

        return wageConfigurationRepository.save(configuration);
    }

    @Override
    @Transactional(readOnly = true)
    public WageConfiguration getWageConfiguration() {
        return wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID)
                .orElseThrow(() -> new IllegalStateException("Wage configuration has not been set."));
    }

    @Override
    @Transactional
    public Payroll calculateLaborerPayroll(String laborerName, String harvestId, BigDecimal harvestedWeightKg) {
        return createPayroll(
                laborerName,
                Role.LABORER,
                PayrollSourceType.HARVEST_APPROVAL,
                harvestId,
                harvestedWeightKg,
                getWageConfiguration().getLaborerWagePerKg()
        );
    }

    @Override
    @Transactional
    public Payroll calculateDriverPayroll(String driverId, String deliveryId, BigDecimal deliveredWeightKg) {
        return createPayroll(
                driverId,
                Role.DRIVER,
                PayrollSourceType.DELIVERY_COMPLETION,
                deliveryId,
                deliveredWeightKg,
                getWageConfiguration().getDriverWagePerKg()
        );
    }

    @Override
    @Transactional
    public Payroll calculateForemanPayroll(String foremanName, String deliveryId, BigDecimal recognizedWeightKg) {
        return createPayroll(
                foremanName,
                Role.FOREMAN,
                PayrollSourceType.DELIVERY_APPROVAL,
                deliveryId,
                recognizedWeightKg,
                getWageConfiguration().getForemanWagePerKg()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollHistory(String beneficiaryReference) {
        return getPayrollHistory(beneficiaryReference, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollHistory(String beneficiaryReference,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           PayrollStatus status) {
        if (beneficiaryReference == null || beneficiaryReference.isBlank()) {
            throw new IllegalArgumentException("Beneficiary reference is required.");
        }

        validateDateRange(startDate, endDate);
        Specification<Payroll> specification = hasBeneficiaryReference(beneficiaryReference)
                .and(hasStatus(status))
                .and(createdOnOrAfter(startDate))
                .and(createdBeforeOrOn(endDate));

        return payrollRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payroll> getAdminPayrolls(String beneficiaryReference,
                                          Role recipientRole,
                                          PayrollStatus status,
                                          LocalDate startDate,
                                          LocalDate endDate) {
        validateDateRange(startDate, endDate);
        Specification<Payroll> specification = hasBeneficiaryReference(beneficiaryReference)
                .and(hasRecipientRole(recipientRole))
                .and(hasStatus(status))
                .and(createdOnOrAfter(startDate))
                .and(createdBeforeOrOn(endDate));

        return payrollRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public Payroll approvePayroll(String payrollId) {
        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll);

        PaymentGatewayReceipt receipt = paymentGateway.process(payroll);
        payroll.setStatus(PayrollStatus.ACCEPTED);
        payroll.setRejectionReason(null);
        payroll.setPaymentGateway(receipt.gatewayName());
        payroll.setPaymentReference(receipt.paymentReference());
        payroll.setProcessedAt(LocalDateTime.now());

        return payrollRepository.save(payroll);
    }

    @Override
    @Transactional
    public Payroll rejectPayroll(String payrollId, String rejectionReason) {
        Payroll payroll = findPayrollOrThrow(payrollId);
        ensurePending(payroll);

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectionReason(rejectionReason.trim());
        payroll.setPaymentGateway(null);
        payroll.setPaymentReference(null);
        payroll.setProcessedAt(LocalDateTime.now());

        return payrollRepository.save(payroll);
    }

    private Payroll createPayroll(String beneficiaryReference,
                                  Role role,
                                  PayrollSourceType sourceType,
                                  String sourceReferenceId,
                                  BigDecimal weightKg,
                                  BigDecimal wageRatePerKg) {
        if (beneficiaryReference == null || beneficiaryReference.isBlank()) {
            throw new IllegalArgumentException("Beneficiary reference is required.");
        }

        if (sourceReferenceId == null || sourceReferenceId.isBlank()) {
            throw new IllegalArgumentException("Source reference is required.");
        }

        BigDecimal validatedWeight = validateWeight(weightKg);
        BigDecimal normalizedRate = validateRate(wageRatePerKg, "Wage");
        BigDecimal amount = normalizedRate
                .multiply(validatedWeight)
                .multiply(PAYROLL_SHARE)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        Payroll payroll = new Payroll();
        payroll.setBeneficiaryReference(beneficiaryReference);
        payroll.setRecipientRole(role);
        payroll.setSourceType(sourceType);
        payroll.setSourceReferenceId(sourceReferenceId);
        payroll.setWeightKg(validatedWeight);
        payroll.setWageRatePerKg(normalizedRate);
        payroll.setAmount(amount);
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setDescription(buildDescription(role, normalizedRate, validatedWeight, amount));

        return payrollRepository.save(payroll);
    }

    private Payroll findPayrollOrThrow(String payrollId) {
        if (payrollId == null || payrollId.isBlank()) {
            throw new IllegalArgumentException("Payroll id is required.");
        }

        return payrollRepository.findById(payrollId)
                .orElseThrow(() -> new NoSuchElementException("Payroll not found."));
    }

    private void ensurePending(Payroll payroll) {
        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new IllegalStateException("Only pending payroll can be processed.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be on or before end date.");
        }
    }

    private Specification<Payroll> hasBeneficiaryReference(String beneficiaryReference) {
        return (root, query, criteriaBuilder) ->
                beneficiaryReference == null || beneficiaryReference.isBlank()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("beneficiaryReference"), beneficiaryReference.trim());
    }

    private Specification<Payroll> hasRecipientRole(Role recipientRole) {
        return (root, query, criteriaBuilder) ->
                recipientRole == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("recipientRole"), recipientRole);
    }

    private Specification<Payroll> hasStatus(PayrollStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<Payroll> createdOnOrAfter(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
                startDate == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
    }

    private Specification<Payroll> createdBeforeOrOn(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
                endDate == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.lessThan(root.get("createdAt"), endDate.plusDays(1).atStartOfDay());
    }

    private String buildDescription(Role role,
                                    BigDecimal wageRatePerKg,
                                    BigDecimal weightKg,
                                    BigDecimal amount) {
        return "%s payroll = %s/kg x %s kg x 90%% = %s".formatted(
                role.name(),
                formatDecimal(wageRatePerKg),
                formatDecimal(weightKg),
                formatDecimal(amount)
        );
    }

    private BigDecimal validateWeight(BigDecimal weightKg) {
        if (weightKg == null) {
            throw new IllegalArgumentException("Weight is required.");
        }

        if (weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }

        return weightKg.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal validateRate(BigDecimal rate, String label) {
        if (rate == null) {
            throw new IllegalArgumentException(label + " is required.");
        }

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0.");
        }

        return rate.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String formatDecimal(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
