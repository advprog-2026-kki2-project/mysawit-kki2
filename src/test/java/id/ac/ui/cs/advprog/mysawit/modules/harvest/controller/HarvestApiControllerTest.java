package id.ac.ui.cs.advprog.mysawit.modules.harvest.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.HarvestSubmissionRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.HarvestSubmissionResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.service.DailyHarvestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HarvestApiControllerTest {

    @Mock
    private DailyHarvestService dailyHarvestService;

    @Mock
    private AuthService authService;

    private HarvestApiController controller;

    @BeforeEach
    void setUp() {
        controller = new HarvestApiController(dailyHarvestService, authService);
    }

    @Test
    void submitShouldUseLoggedInLaborerIdentity() {
        Authentication authentication = authenticatedUser("laborer@example.com");
        when(authService.currentSession("laborer@example.com"))
                .thenReturn(new AuthResponse("Session active", "budi.sawit", Role.LABORER));

        DailyHarvest harvest = new DailyHarvest();
        harvest.setId("harvest-1");
        harvest.setLaborerName("budi.sawit");
        harvest.setHarvestDate(LocalDate.of(2026, 4, 16));
        harvest.setWeightKg(120.5);
        harvest.setNotes("Panen blok A");
        harvest.setStatus("PENDING");

        when(dailyHarvestService.recordHarvest(any(DailyHarvestRequestDto.class), any()))
                .thenReturn(harvest);

        HarvestSubmissionRequestDto request = new HarvestSubmissionRequestDto();
        request.setHarvestDate(LocalDate.of(2026, 4, 16));
        request.setWeightKg(120.5);
        request.setNotes("Panen blok A");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "panen.jpg",
                "image/jpeg",
                "image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        ResponseEntity<HarvestSubmissionResponseDto> response =
                controller.submitHarvest(request, photo, authentication);

        ArgumentCaptor<DailyHarvestRequestDto> captor =
                ArgumentCaptor.forClass(DailyHarvestRequestDto.class);
        verify(dailyHarvestService).recordHarvest(captor.capture(), any());

        assertEquals("budi.sawit", captor.getValue().getLaborerName());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("harvest-1", response.getBody().getId());
    }

    private Authentication authenticatedUser(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }
}
