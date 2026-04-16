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
}