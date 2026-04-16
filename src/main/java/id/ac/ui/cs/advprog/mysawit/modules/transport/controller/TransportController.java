package id.ac.ui.cs.advprog.mysawit.modules.transport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import id.ac.ui.cs.advprog.mysawit.modules.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.modules.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.modules.transport.service.TransportService;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
public class TransportController {

    @Autowired
    private TransportService transportService;

    /**
     * Milestone 2 Requirement: Foreman assigns a Truck Driver to pick up approved crops.
     * The 400kg capacity limit is enforced inside the service.
     */
    @PostMapping("/assign-pickup")
    public ResponseEntity<?> assignPickup(@RequestBody PickupRequestDto dto) {
        try {
            Transport transport = transportService.assignPickup(dto);
            return new ResponseEntity<>(transport, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Returns 400 Bad Request if weights are too high or crops aren't approved
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Milestone 1 Requirement: Truck Drivers can see the list of deliveries assigned to them.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Transport>> getMyDeliveries(@PathVariable String driverId) {
        List<Transport> deliveries = transportService.getDriverDeliveries(driverId);
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Milestone 1 Requirement: Truck Drivers can change delivery status.
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