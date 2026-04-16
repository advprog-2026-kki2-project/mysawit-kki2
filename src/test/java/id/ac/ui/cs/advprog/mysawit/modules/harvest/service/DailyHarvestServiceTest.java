package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyHarvestServiceTest {

    @Mock
    private DailyHarvestRepository repository;

    @Mock
    private HarvestImageStorageService imageStorageService;

    private DailyHarvestService service;

    @BeforeEach
    void setUp() {
        service = new DailyHarvestService(repository, imageStorageService);
    }

    @Test
    void recordHarvestShouldPersistPendingHarvest() {
        DailyHarvestRequestDto request = new DailyHarvestRequestDto();
        request.setLaborerName("budi.sawit");
        request.setHarvestDate(LocalDate.of(2026, 4, 16));
        request.setWeightKg(88.0);
        request.setNotes("Panen blok B");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "photo.jpg",
                "image/jpeg",
                "image".getBytes(StandardCharsets.UTF_8)
        );

        when(repository.existsByLaborerNameIgnoreCaseAndHarvestDate("budi.sawit", LocalDate.of(2026, 4, 16)))
                .thenReturn(false);
        when(imageStorageService.store(photo)).thenReturn("/tmp/panen.jpg");
        when(repository.save(any(DailyHarvest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyHarvest result = service.recordHarvest(request, photo);

        ArgumentCaptor<DailyHarvest> captor = ArgumentCaptor.forClass(DailyHarvest.class);
        verify(repository).save(captor.capture());

        assertEquals("PENDING", captor.getValue().getStatus());
        assertEquals("/tmp/panen.jpg", result.getPhotoPath());
    }

    @Test
    void recordHarvestShouldRejectDuplicateSubmission() {
        DailyHarvestRequestDto request = new DailyHarvestRequestDto();
        request.setLaborerName("budi.sawit");
        request.setHarvestDate(LocalDate.of(2026, 4, 16));
        request.setWeightKg(88.0);
        request.setNotes("Panen blok B");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "photo.jpg",
                "image/jpeg",
                "image".getBytes(StandardCharsets.UTF_8)
        );

        when(repository.existsByLaborerNameIgnoreCaseAndHarvestDate("budi.sawit", LocalDate.of(2026, 4, 16)))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.recordHarvest(request, photo)
        );

        assertEquals("Harvest already submitted for this date.", exception.getMessage());
    }
}
