package id.ac.ui.cs.advprog.mysawit.modules.plantation.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.dto.PlantationRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.dto.PlantationResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Coordinate;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.PlantationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantationApiControllerTest {

    @Mock
    private PlantationService plantationService;

    @Mock
    private AuthService authService;

    private PlantationApiController controller;

    @BeforeEach
    void setUp() {
        controller = new PlantationApiController(plantationService, authService);
    }

    @Test
    void listShouldReturnPlantationsForAdmin() {
        Authentication authentication = authenticatedUser("admin@example.com");
        when(authService.currentSession("admin@example.com"))
                .thenReturn(new AuthResponse("Session active", "admin", Role.ADMIN));

        Plantation plantation = new Plantation();
        plantation.setPlantationId("plt-1");
        plantation.setPlantationCode("PLT-20240416-0001");
        plantation.setPlantationName("Kebun Barat");
        plantation.setAreaHectares(25);
        Coordinate coordinate = new Coordinate();
        coordinate.setX(0);
        coordinate.setY(0);
        plantation.setCorners(List.of(coordinate));

        when(plantationService.findAll()).thenReturn(List.of(plantation));

        ResponseEntity<List<PlantationResponseDto>> response = controller.list(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Kebun Barat", response.getBody().getFirst().getPlantationName());
    }

    @Test
    void createShouldRejectNonAdmin() {
        Authentication authentication = authenticatedUser("laborer@example.com");
        when(authService.currentSession("laborer@example.com"))
                .thenReturn(new AuthResponse("Session active", "laborer", Role.LABORER));

        PlantationRequestDto request = new PlantationRequestDto();
        request.setPlantationCode("PLT-20240416-0002");
        request.setPlantationName("Kebun Timur");
        request.setAreaHectares(10);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.create(request, authentication)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    private Authentication authenticatedUser(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }
}
