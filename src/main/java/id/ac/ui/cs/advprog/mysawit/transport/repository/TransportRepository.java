package id.ac.ui.cs.advprog.mysawit.transport.repository;

import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
    // Satisfies requirement: List of deliveries assigned to a specific driver
    List<Transport> findByDriverId(String driverId);
}