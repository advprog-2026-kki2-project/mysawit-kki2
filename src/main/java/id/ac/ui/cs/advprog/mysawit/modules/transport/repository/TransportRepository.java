package id.ac.ui.cs.advprog.mysawit.modules.transport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.mysawit.modules.transport.model.Transport;

import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
    // Satisfies requirement: List of deliveries assigned to a specific driver
    List<Transport> findByDriverId(String driverId);
}