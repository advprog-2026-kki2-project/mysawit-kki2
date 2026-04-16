package id.ac.ui.cs.advprog.mysawit.modules.payment.repository;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, String> {
    List<Payroll> findByBeneficiaryReferenceOrderByCreatedAtDesc(String beneficiaryReference);
}
