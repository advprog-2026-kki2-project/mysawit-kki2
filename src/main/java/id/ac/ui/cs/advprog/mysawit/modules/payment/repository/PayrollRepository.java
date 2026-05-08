package id.ac.ui.cs.advprog.mysawit.modules.payment.repository;

import id.ac.ui.cs.advprog.mysawit.modules.payment.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, String>, JpaSpecificationExecutor<Payroll> {
    List<Payroll> findByBeneficiaryReferenceOrderByCreatedAtDesc(String beneficiaryReference);
}
