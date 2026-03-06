package id.ac.ui.cs.advprog.mysawit.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransportService {

    @Autowired
    private TransportRepository transportRepository;

    // Logic for Driver to see their list
    public List<Transport> getDriverDeliveries(String driverId) {
        return transportRepository.findByDriverId(driverId);
    }

    // Logic to change status (Loading -> Transporting -> Arrived)
    public Transport updateStatus(Long transportId, TransportStatus newStatus) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport record not found."));

        transport.setStatus(newStatus);
        return transportRepository.save(transport);
    }
}