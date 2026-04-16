package id.ac.ui.cs.advprog.mysawit.modules.payment.service;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollSourceType;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.WageConfiguration;
import id.ac.ui.cs.advprog.mysawit.modules.payment.repository.PayrollRepository;
import id.ac.ui.cs.advprog.mysawit.modules.payment.repository.WageConfigurationRepository;
import id.ac.ui.cs.advprog.mysawit.modules.payment.service.impl.PayrollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private WageConfigurationRepository wageConfigurationRepository;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollServiceImpl(payrollRepository, wageConfigurationRepository);
    }

    @Test
    void saveWageConfigurationShouldPersistGlobalConfiguration() {
        when(wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID))
                .thenReturn(Optional.empty());
        when(wageConfigurationRepository.save(any(WageConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WageConfiguration result = payrollService.saveWageConfiguration(
                new BigDecimal("1500"),
                new BigDecimal("1000"),
                new BigDecimal("2000")
        );

        assertEquals(WageConfiguration.GLOBAL_CONFIGURATION_ID, result.getId());
        assertEquals(new BigDecimal("1500.00"), result.getLaborerWagePerKg());
        assertEquals(new BigDecimal("1000.00"), result.getDriverWagePerKg());
        assertEquals(new BigDecimal("2000.00"), result.getForemanWagePerKg());
    }

    @Test
    void calculateLaborerPayrollShouldUseConfiguredFormulaAndPersistPendingEntry() {
        WageConfiguration configuration = configuredRates();
        when(wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID))
                .thenReturn(Optional.of(configuration));
        when(payrollRepository.save(any(Payroll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payroll result = payrollService.calculateLaborerPayroll(
                "laborer-a",
                "harvest-001",
                new BigDecimal("125")
        );

        ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(captor.capture());

        Payroll savedPayroll = captor.getValue();
        assertEquals(Role.LABORER, savedPayroll.getRecipientRole());
        assertEquals(PayrollSourceType.HARVEST_APPROVAL, savedPayroll.getSourceType());
        assertEquals(PayrollStatus.PENDING, savedPayroll.getStatus());
        assertEquals(new BigDecimal("168750.00"), savedPayroll.getAmount());
        assertEquals("LABORER payroll = 1500.00/kg x 125.00 kg x 90% = 168750.00", savedPayroll.getDescription());
        assertEquals(savedPayroll.getAmount(), result.getAmount());
    }

    @Test
    void calculateDriverPayrollShouldUseDriverRate() {
        when(wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID))
                .thenReturn(Optional.of(configuredRates()));
        when(payrollRepository.save(any(Payroll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payroll result = payrollService.calculateDriverPayroll(
                "driver-a",
                "delivery-001",
                new BigDecimal("80")
        );

        assertEquals(Role.DRIVER, result.getRecipientRole());
        assertEquals(PayrollSourceType.DELIVERY_COMPLETION, result.getSourceType());
        assertEquals(new BigDecimal("72000.00"), result.getAmount());
    }

    @Test
    void calculateForemanPayrollShouldUseRecognizedWeight() {
        when(wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID))
                .thenReturn(Optional.of(configuredRates()));
        when(payrollRepository.save(any(Payroll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payroll result = payrollService.calculateForemanPayroll(
                "foreman-a",
                "delivery-001",
                new BigDecimal("73.5")
        );

        assertEquals(Role.FOREMAN, result.getRecipientRole());
        assertEquals(PayrollSourceType.DELIVERY_APPROVAL, result.getSourceType());
        assertEquals(new BigDecimal("132300.00"), result.getAmount());
    }

    @Test
    void calculatePayrollShouldFailWhenConfigurationIsMissing() {
        when(wageConfigurationRepository.findById(WageConfiguration.GLOBAL_CONFIGURATION_ID))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> payrollService.calculateLaborerPayroll(
                        "laborer-a",
                        "harvest-001",
                        new BigDecimal("100")
                )
        );

        assertEquals("Wage configuration has not been set.", exception.getMessage());
    }

    @Test
    void getPayrollHistoryShouldReturnEntriesByBeneficiaryReference() {
        Payroll payroll = new Payroll();
        payroll.setBeneficiaryReference("laborer-a");
        when(payrollRepository.findByBeneficiaryReferenceOrderByCreatedAtDesc("laborer-a"))
                .thenReturn(List.of(payroll));

        List<Payroll> result = payrollService.getPayrollHistory("laborer-a");

        assertEquals(1, result.size());
        assertEquals("laborer-a", result.getFirst().getBeneficiaryReference());
    }

    private WageConfiguration configuredRates() {
        WageConfiguration configuration = new WageConfiguration();
        configuration.setLaborerWagePerKg(new BigDecimal("1500.00"));
        configuration.setDriverWagePerKg(new BigDecimal("1000.00"));
        configuration.setForemanWagePerKg(new BigDecimal("2000.00"));
        return configuration;
    }
}
