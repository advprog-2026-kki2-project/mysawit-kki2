package id.ac.ui.cs.advprog.mysawit.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private TransportService transportService;

    private Transport sampleTransport;

    @BeforeEach
    void setUp() {
        sampleTransport = Transport.builder()
                .id(1L)
                .driverId("driver-123")
                .status(TransportStatus.LOADING) // Default requirement
                .build();
    }

    @Test
    void testGetDriverDeliveries() {
        when(transportRepository.findByDriverId("driver-123"))
                .thenReturn(Arrays.asList(sampleTransport));

        List<Transport> results = transportService.getDriverDeliveries("driver-123");

        assertEquals(1, results.size());
        assertEquals("driver-123", results.get(0).getDriverId());
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

    @Test
    void testUpdateStatusThrowsExceptionForInvalidId() {
        when(transportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            transportService.updateStatus(99L, TransportStatus.ARRIVED);
        });
    }
}