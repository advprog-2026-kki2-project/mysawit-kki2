package id.ac.ui.cs.advprog.mysawit.modules.payment.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.PayrollDashboardResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollSourceType;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import id.ac.ui.cs.advprog.mysawit.modules.payment.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentApiControllerTest {

    @Mock
    private PayrollService payrollService;

    @Mock
    private AuthService authService;

    private PaymentApiController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentApiController(payrollService, authService);
    }

    @Test
    void getDashboardShouldReturnSummaryForAdmin() {
        Authentication authentication = authenticatedUser("admin@example.com");
        when(authService.currentSession("admin@example.com"))
                .thenReturn(new AuthResponse("Session active", "admin", Role.ADMIN));
        when(payrollService.getAdminPayrolls(null, null, null, null, null))
                .thenReturn(List.of(samplePayroll(PayrollStatus.PENDING), samplePayroll(PayrollStatus.ACCEPTED)));

        ResponseEntity<PayrollDashboardResponseDto> response = controller.getDashboard(
                null,
                null,
                null,
                null,
                null,
                authentication
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().getTotalEntries());
    }

    @Test
    void approvePayrollShouldRejectNonAdmin() {
        Authentication authentication = authenticatedUser("driver@example.com");
        when(authService.currentSession("driver@example.com"))
                .thenReturn(new AuthResponse("Session active", "driver-1", Role.DRIVER));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.approvePayroll("payroll-1", authentication)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getMyPayrollsShouldUseCurrentUsername() {
        Authentication authentication = authenticatedUser("laborer@example.com");
        when(authService.currentSession("laborer@example.com"))
                .thenReturn(new AuthResponse("Session active", "laborer-1", Role.LABORER));
        when(payrollService.getPayrollHistory("laborer-1", null, null, PayrollStatus.PENDING))
                .thenReturn(List.of(samplePayroll(PayrollStatus.PENDING)));

        ResponseEntity<?> response = controller.getMyPayrolls(PayrollStatus.PENDING, null, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    private Authentication authenticatedUser(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }

    private Payroll samplePayroll(PayrollStatus status) {
        Payroll payroll = new Payroll();
        payroll.setId("payroll-1");
        payroll.setBeneficiaryReference("laborer-1");
        payroll.setRecipientRole(Role.LABORER);
        payroll.setSourceType(PayrollSourceType.HARVEST_APPROVAL);
        payroll.setSourceReferenceId("harvest-1");
        payroll.setWeightKg(new BigDecimal("100.00"));
        payroll.setWageRatePerKg(new BigDecimal("1500.00"));
        payroll.setAmount(new BigDecimal("135000.00"));
        payroll.setDescription("desc");
        payroll.setStatus(status);
        payroll.setCreatedAt(LocalDateTime.now());
        return payroll;
    }
}
