package id.ac.ui.cs.advprog.mysawit.transport.service;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;
import id.ac.ui.cs.advprog.mysawit.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.transport.exception.CapacityExceededException;
import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.transport.repository.TransportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private DailyHarvestRepository dailyHarvestRepository;

    @InjectMocks
    private TransportService transportService;

    private Transport sampleTransport;
    private DailyHarvest approvedHarvest;

    @BeforeEach
    void setUp() {
        sampleTransport = Transport.builder()
                .id(1L)
                .driverId("driver-123")
                .totalWeight(200.0)
                .status(TransportStatus.LOADING) // Default requirement
                .build();

        approvedHarvest = new DailyHarvest();
        approvedHarvest.setWeightKg(200.0);
        approvedHarvest.setStatus("APPROVED");
    }

    // --- Milestone 1 Tests (Status & Driver View) ---

    @Test
    void testGetDriverDeliveries() {
        when(transportRepository.findByDriverId("driver-123"))
                .thenReturn(List.of(sampleTransport));

        List<Transport> results = transportService.getDriverDeliveries("driver-123");

        assertEquals(1, results.size());
        assertEquals("driver-123", results.getFirst().getDriverId());
        verify(transportRepository, times(1)).findByDriverId("driver-123");
    }

    @Test
    void testUpdateStatusSuccessfully() {
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenReturn(sampleTransport);

        Transport updated = transportService.updateStatus(1L, TransportStatus.TRANSPORTING);

        assertEquals(TransportStatus.TRANSPORTING, updated.getStatus());
        verify(transportRepository, times(1)).save(sampleTransport);
    }

    // --- Milestone 2 Tests (400kg & Integration) ---

    @Test
    void testAssignPickupSuccess() {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setDriverId("driver-123");
        dto.setHarvestIds(List.of("harvest-1"));

        when(dailyHarvestRepository.findById("harvest-1")).thenReturn(Optional.of(approvedHarvest));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.assignPickup(dto);

        assertNotNull(result);
        assertEquals(200.0, result.getTotalWeight());
        verify(transportRepository).save(any(Transport.class));
    }

    @Test
    void testAssignPickupExceeds400kgLimit() {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setDriverId("driver-1");
        dto.setHarvestIds(List.of("h1", "h2", "h3"));

        DailyHarvest heavyHarvest = new DailyHarvest();
        heavyHarvest.setWeightKg(150.0);
        heavyHarvest.setStatus("APPROVED");

        when(dailyHarvestRepository.findById(anyString())).thenReturn(Optional.of(heavyHarvest));

        // 150kg * 3 = 450kg (Exceeds 400kg constraint)
        assertThrows(CapacityExceededException.class, () -> transportService.assignPickup(dto));
    }

    @Test
    void testAssignPickupFailsForUnapprovedHarvest() {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setHarvestIds(List.of("unapproved-1"));

        DailyHarvest pendingHarvest = new DailyHarvest();
        pendingHarvest.setStatus("PENDING");

        when(dailyHarvestRepository.findById("unapproved-1")).thenReturn(Optional.of(pendingHarvest));

        // Attempting to pickup unapproved crops should fail
        assertThrows(IllegalStateException.class, () -> transportService.assignPickup(dto));
    }

    // --- Milestone 3 Tests (Foreman Verification) ---

    @Test
    void testForemanApproveDelivery() {
        sampleTransport.setStatus(TransportStatus.ARRIVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.verifyByForeman(1L, true, null);

        assertEquals(TransportStatus.FOREMAN_APPROVED, result.getStatus());
        assertTrue(result.getForemanApproved());
    }

    @Test
    void testForemanRejectDeliveryWithReason() {
        sampleTransport.setStatus(TransportStatus.ARRIVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.verifyByForeman(1L, false, "Damaged goods");

        assertEquals(TransportStatus.FOREMAN_REJECTED, result.getStatus());
        assertFalse(result.getForemanApproved());
        assertEquals("Damaged goods", result.getForemanRejectionReason());
    }

    @Test
    void testForemanRejectWithoutReasonFails() {
        sampleTransport.setStatus(TransportStatus.ARRIVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));

        assertThrows(IllegalArgumentException.class,
                () -> transportService.verifyByForeman(1L, false, null));
    }

    @Test
    void testForemanVerifyNonArrivedDeliveryFails() {
        // Status is LOADING (not ARRIVED)
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));

        assertThrows(IllegalStateException.class,
                () -> transportService.verifyByForeman(1L, true, null));
    }

    // --- Milestone 3 Tests (Admin Verification) ---

    @Test
    void testAdminFullApproval() {
        sampleTransport.setStatus(TransportStatus.FOREMAN_APPROVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.verifyByAdmin(1L, true, null, null);

        assertEquals(TransportStatus.ADMIN_APPROVED, result.getStatus());
        assertTrue(result.getAdminApproved());
        assertEquals(200.0, result.getRecognizedWeight());
    }

    @Test
    void testAdminPartialRejection() {
        sampleTransport.setStatus(TransportStatus.FOREMAN_APPROVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.verifyByAdmin(1L, false, 150.0, "50kg spoiled");

        assertEquals(TransportStatus.ADMIN_APPROVED, result.getStatus());
        assertTrue(result.getAdminApproved());
        assertEquals(150.0, result.getRecognizedWeight());
        assertEquals("50kg spoiled", result.getAdminRejectionReason());
    }

    @Test
    void testAdminFullRejection() {
        sampleTransport.setStatus(TransportStatus.FOREMAN_APPROVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));
        when(transportRepository.save(any(Transport.class))).thenAnswer(i -> i.getArguments()[0]);

        Transport result = transportService.verifyByAdmin(1L, false, null, "Completely damaged");

        assertEquals(TransportStatus.ADMIN_REJECTED, result.getStatus());
        assertFalse(result.getAdminApproved());
        assertEquals("Completely damaged", result.getAdminRejectionReason());
    }

    @Test
    void testAdminVerifyNonForemanApprovedFails() {
        sampleTransport.setStatus(TransportStatus.ARRIVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));

        assertThrows(IllegalStateException.class,
                () -> transportService.verifyByAdmin(1L, true, null, null));
    }

    @Test
    void testAdminRejectWithoutReasonFails() {
        sampleTransport.setStatus(TransportStatus.FOREMAN_APPROVED);
        when(transportRepository.findById(1L)).thenReturn(Optional.of(sampleTransport));

        assertThrows(IllegalArgumentException.class,
                () -> transportService.verifyByAdmin(1L, false, null, null));
    }
}