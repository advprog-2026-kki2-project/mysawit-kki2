package id.ac.ui.cs.advprog.mysawit.transport.service;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;
import id.ac.ui.cs.advprog.mysawit.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.transport.exception.CapacityExceededException;
import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.transport.repository.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportService {

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private DailyHarvestRepository dailyHarvestRepository; // Integrating with Rani's module

    /**
     * Requirement: Foreman assigns a Truck Driver to pick up approved crops.
     * Constraint: Total weight must not exceed 400kg.
     */
    public Transport assignPickup(PickupRequestDto dto) {
        double totalWeight = 0;

        for (String harvestId : dto.getHarvestIds()) {
            DailyHarvest harvest = dailyHarvestRepository.findById(harvestId)
                    .orElseThrow(() -> new IllegalArgumentException("Harvest not found: " + harvestId));

            // Verify the harvest is approved by a foreman
            if (!"APPROVED".equalsIgnoreCase(harvest.getStatus())) {
                throw new IllegalStateException("Harvest " + harvestId + " is not approved for pickup.");
            }

            totalWeight += harvest.getWeightKg();
        }

        // Enforce the 400kg capacity constraint
        if (totalWeight > 400.0) {
            throw new CapacityExceededException(totalWeight);
        }

        // Create new transport task with default status LOADING
        Transport transport = Transport.builder()
                .driverId(dto.getDriverId())
                .totalWeight(totalWeight)
                .status(TransportStatus.LOADING)
                .build();

        return transportRepository.save(transport);
    }

    public List<Transport> getDriverDeliveries(String driverId) {
        return transportRepository.findByDriverId(driverId);
    }

    public Transport updateStatus(Long transportId, TransportStatus newStatus) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport record not found."));
        transport.setStatus(newStatus);
        return transportRepository.save(transport);
    }

    /**
     * Foreman verifies a delivery that has ARRIVED status.
     * Stage 1: If approved, triggers Driver payroll asynchronously.
     * If rejected, sets rejection reason and marks as FOREMAN_REJECTED.
     */
    public Transport verifyByForeman(Long transportId, boolean isApproved, String rejectionReason) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport record not found."));

        if (transport.getStatus() != TransportStatus.ARRIVED) {
            throw new IllegalStateException(
                    "Only deliveries with ARRIVED status can be verified by Foreman. Current status: " + transport.getStatus());
        }

        if (isApproved) {
            transport.setForemanApproved(true);
            transport.setStatus(TransportStatus.FOREMAN_APPROVED);
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting a delivery.");
            }
            transport.setForemanApproved(false);
            transport.setForemanRejectionReason(rejectionReason);
            transport.setStatus(TransportStatus.FOREMAN_REJECTED);
        }

        return transportRepository.save(transport);
    }

    /**
     * Central Admin verifies a delivery that has been approved by Foreman.
     * Stage 2: If approved (fully or partially), triggers Foreman payroll asynchronously.
     * Partial rejection: Admin specifies recognized weight and reason.
     */
    public Transport verifyByAdmin(Long transportId, boolean isApproved,
                                   Double recognizedWeight, String rejectionReason) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("Transport record not found."));

        if (transport.getStatus() != TransportStatus.FOREMAN_APPROVED) {
            throw new IllegalStateException(
                    "Only Foreman-approved deliveries can be verified by Admin. Current status: " + transport.getStatus());
        }

        if (isApproved) {
            // Full approval: recognized weight equals total weight
            transport.setAdminApproved(true);
            transport.setRecognizedWeight(transport.getTotalWeight());
            transport.setStatus(TransportStatus.ADMIN_APPROVED);
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting a delivery.");
            }

            if (recognizedWeight != null && recognizedWeight > 0) {
                // Partial rejection: some weight is still recognized
                transport.setAdminApproved(true);
                transport.setRecognizedWeight(recognizedWeight);
                transport.setAdminRejectionReason(rejectionReason);
                transport.setStatus(TransportStatus.ADMIN_APPROVED);
            } else {
                // Full rejection: no weight recognized
                transport.setAdminApproved(false);
                transport.setAdminRejectionReason(rejectionReason);
                transport.setStatus(TransportStatus.ADMIN_REJECTED);
            }
        }

        return transportRepository.save(transport);
    }

    /**
     * Admin: Get list of foreman-approved deliveries ready for admin review.
     */
    public List<Transport> getForemanApprovedDeliveries() {
        return transportRepository.findByStatus(TransportStatus.FOREMAN_APPROVED);
    }

    /**
     * Foreman: Get list of ongoing deliveries.
     */
    public List<Transport> getOngoingDeliveries() {
        return transportRepository.findByStatusIn(
                List.of(TransportStatus.LOADING, TransportStatus.TRANSPORTING, TransportStatus.ARRIVED));
    }
}