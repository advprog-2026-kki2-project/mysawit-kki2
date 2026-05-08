package id.ac.ui.cs.advprog.mysawit.modules.payment.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.PayrollDashboardResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.PayrollDecisionRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.PayrollResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.WageConfigurationRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.dto.WageConfigurationResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.PayrollStatus;
import id.ac.ui.cs.advprog.mysawit.modules.payment.model.WageConfiguration;
import id.ac.ui.cs.advprog.mysawit.modules.payment.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/payrolls")
public class PaymentApiController {

    private final PayrollService payrollService;
    private final AuthService authService;

    public PaymentApiController(PayrollService payrollService, AuthService authService) {
        this.payrollService = payrollService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<PayrollDashboardResponseDto> getDashboard(
            @RequestParam(required = false) String beneficiaryReference,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        List<Payroll> payrolls = payrollService.getAdminPayrolls(
                beneficiaryReference,
                role,
                status,
                startDate,
                endDate
        );

        return ResponseEntity.ok(toDashboardResponse(payrolls));
    }

    @GetMapping("/beneficiaries/{beneficiaryReference}")
    public ResponseEntity<List<PayrollResponseDto>> getPayrollByBeneficiary(
            @PathVariable String beneficiaryReference,
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        List<PayrollResponseDto> payrolls = payrollService.getPayrollHistory(
                        beneficiaryReference,
                        startDate,
                        endDate,
                        status
                ).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PayrollResponseDto>> getMyPayrolls(
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        AuthResponse session = requireAuthenticated(authentication);

        List<PayrollResponseDto> payrolls = payrollService.getPayrollHistory(
                        session.getUsername(),
                        startDate,
                        endDate,
                        status
                ).stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/configuration")
    public ResponseEntity<WageConfigurationResponseDto> getWageConfiguration(Authentication authentication) {
        requireRole(authentication, Role.ADMIN);
        return ResponseEntity.ok(toResponse(payrollService.getWageConfiguration()));
    }

    @PutMapping("/configuration")
    public ResponseEntity<WageConfigurationResponseDto> updateWageConfiguration(
            @Valid @RequestBody WageConfigurationRequestDto request,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        WageConfiguration updated = payrollService.saveWageConfiguration(
                request.getLaborerWagePerKg(),
                request.getDriverWagePerKg(),
                request.getForemanWagePerKg()
        );

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{payrollId}/approve")
    public ResponseEntity<PayrollResponseDto> approvePayroll(
            @PathVariable String payrollId,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);
        return ResponseEntity.ok(toResponse(payrollService.approvePayroll(payrollId)));
    }

    @PostMapping("/{payrollId}/reject")
    public ResponseEntity<PayrollResponseDto> rejectPayroll(
            @PathVariable String payrollId,
            @Valid @RequestBody PayrollDecisionRequestDto request,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);
        return ResponseEntity.ok(toResponse(payrollService.rejectPayroll(payrollId, request.getReason())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    private AuthResponse requireAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return authService.currentSession(authentication.getName());
    }

    private void requireRole(Authentication authentication, Role requiredRole) {
        AuthResponse session = requireAuthenticated(authentication);
        if (session.getRole() != requiredRole) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }
    }

    private PayrollDashboardResponseDto toDashboardResponse(List<Payroll> payrolls) {
        PayrollDashboardResponseDto response = new PayrollDashboardResponseDto();
        response.setTotalEntries(payrolls.size());
        response.setPendingCount(payrolls.stream().filter(payroll -> payroll.getStatus() == PayrollStatus.PENDING).count());
        response.setAcceptedCount(payrolls.stream().filter(payroll -> payroll.getStatus() == PayrollStatus.ACCEPTED).count());
        response.setRejectedCount(payrolls.stream().filter(payroll -> payroll.getStatus() == PayrollStatus.REJECTED).count());
        response.setTotalAmount(sumAmount(payrolls, null));
        response.setTotalAcceptedAmount(sumAmount(payrolls, PayrollStatus.ACCEPTED));
        response.setTotalPendingAmount(sumAmount(payrolls, PayrollStatus.PENDING));
        response.setPayrolls(payrolls.stream().map(this::toResponse).toList());
        return response;
    }

    private BigDecimal sumAmount(List<Payroll> payrolls, PayrollStatus status) {
        return payrolls.stream()
                .filter(payroll -> status == null || payroll.getStatus() == status)
                .map(Payroll::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PayrollResponseDto toResponse(Payroll payroll) {
        PayrollResponseDto response = new PayrollResponseDto();
        response.setId(payroll.getId());
        response.setBeneficiaryReference(payroll.getBeneficiaryReference());
        response.setRecipientRole(payroll.getRecipientRole());
        response.setSourceType(payroll.getSourceType());
        response.setSourceReferenceId(payroll.getSourceReferenceId());
        response.setWeightKg(payroll.getWeightKg());
        response.setWageRatePerKg(payroll.getWageRatePerKg());
        response.setAmount(payroll.getAmount());
        response.setStatus(payroll.getStatus());
        response.setDescription(payroll.getDescription());
        response.setRejectionReason(payroll.getRejectionReason());
        response.setPaymentGateway(payroll.getPaymentGateway());
        response.setPaymentReference(payroll.getPaymentReference());
        response.setCreatedAt(payroll.getCreatedAt());
        response.setProcessedAt(payroll.getProcessedAt());
        return response;
    }

    private WageConfigurationResponseDto toResponse(WageConfiguration configuration) {
        WageConfigurationResponseDto response = new WageConfigurationResponseDto();
        response.setLaborerWagePerKg(configuration.getLaborerWagePerKg());
        response.setDriverWagePerKg(configuration.getDriverWagePerKg());
        response.setForemanWagePerKg(configuration.getForemanWagePerKg());
        response.setUpdatedAt(configuration.getUpdatedAt());
        return response;
    }
}
