package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationServiceImplTest {

    @Mock
    private PlantationRepository plantationRepository;

    @InjectMocks
    private PlantationServiceImpl plantationService;

    private Plantation plantation;

    @BeforeEach
    void setUp() {
        plantation = new Plantation();
        plantation.setPlantationName("Test Estate");
        plantation.setPlantationCode("PLT-20240101-0001");
    }

    @Test
    void testSearch() {
        Iterator<Plantation> mockIterator = List.of(plantation).iterator();
        when(plantationRepository.search("Test", "PLT-20240101-0001", "F001")).thenReturn(mockIterator);

        List<Plantation> result = plantationService.search("Test", "PLT-20240101-0001", "F001");

        assertEquals(1, result.size());
        assertEquals("Test Estate", result.get(0).getPlantationName());
        verify(plantationRepository, times(1)).search("Test", "PLT-20240101-0001", "F001");
    }
}
