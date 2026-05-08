package id.ac.ui.cs.advprog.mysawit.transport.controller;

import id.ac.ui.cs.advprog.mysawit.transport.dto.AdminVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.ForemanVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.transport.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        } catch (IllegalArgumentException | IllegalStateException
                 | id.ac.ui.cs.advprog.mysawit.transport.exception.CapacityExceededException e) {
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

    /**
     * Milestone 3 Requirement: Foreman verifies an arrived delivery (approve/reject).
     * Stage 1 of two-stage verification. Triggers Driver payroll on approval.
     */
    @PostMapping("/{id}/foreman-verify")
    public ResponseEntity<?> foremanVerify(
            @PathVariable Long id,
            @RequestBody ForemanVerificationDto dto) {
        try {
            Transport result = transportService.verifyByForeman(
                    id, dto.isApproved(), dto.getRejectionReason());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Milestone 3 Requirement: Central Admin verifies a foreman-approved delivery.
     * Stage 2 of two-stage verification. Supports full approval, partial rejection, and full rejection.
     * Triggers Foreman payroll on approval (based on recognized weight).
     */
    @PostMapping("/{id}/admin-verify")
    public ResponseEntity<?> adminVerify(
            @PathVariable Long id,
            @RequestBody AdminVerificationDto dto) {
        try {
            Transport result = transportService.verifyByAdmin(
                    id, dto.isApproved(), dto.getRecognizedWeight(), dto.getRejectionReason());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Foreman: View ongoing deliveries in their plantation.
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<Transport>> getOngoingDeliveries() {
        return ResponseEntity.ok(transportService.getOngoingDeliveries());
    }

    /**
     * Admin: View foreman-approved deliveries ready for admin review.
     */
    @GetMapping("/foreman-approved")
    public ResponseEntity<List<Transport>> getForemanApprovedDeliveries() {
        return ResponseEntity.ok(transportService.getForemanApprovedDeliveries());
    }
}