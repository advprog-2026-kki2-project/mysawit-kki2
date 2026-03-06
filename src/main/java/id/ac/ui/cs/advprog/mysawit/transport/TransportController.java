package id.ac.ui.cs.advprog.mysawit.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
public class TransportController {

    @Autowired
    private TransportService transportService;

    /**
     * Requirement: Truck Drivers can see the list of deliveries assigned to them.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Transport>> getMyDeliveries(@PathVariable String driverId) {
        List<Transport> deliveries = transportService.getDriverDeliveries(driverId);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Requirement: Truck Drivers can change delivery status to Loading, Transporting, and Arrived.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Transport> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam TransportStatus status) {
        try {
            Transport updated = transportService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}