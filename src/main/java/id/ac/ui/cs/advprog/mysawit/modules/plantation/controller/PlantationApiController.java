package id.ac.ui.cs.advprog.mysawit.modules.plantation.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.dto.CoordinateDto;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.dto.PlantationRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.dto.PlantationResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Coordinate;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.service.PlantationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plantations")
public class PlantationApiController {

    private final PlantationService plantationService;
    private final AuthService authService;

    public PlantationApiController(PlantationService plantationService, AuthService authService) {
        this.plantationService = plantationService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<PlantationResponseDto>> list(Authentication authentication) {
        requireRole(authentication, Role.ADMIN);

        List<PlantationResponseDto> plantations = plantationService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(plantations);
    }

    @GetMapping("/{plantationId}")
    public ResponseEntity<PlantationResponseDto> detail(
            @PathVariable String plantationId,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        Plantation plantation = plantationService.findById(plantationId);
        if (plantation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plantation not found");
        }

        return ResponseEntity.ok(toResponse(plantation));
    }

    @PostMapping
    public ResponseEntity<PlantationResponseDto> create(
            @RequestBody PlantationRequestDto request,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        Plantation created = plantationService.create(toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{plantationId}")
    public ResponseEntity<PlantationResponseDto> update(
            @PathVariable String plantationId,
            @RequestBody PlantationRequestDto request,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        Plantation existing = plantationService.findById(plantationId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plantation not found");
        }

        Plantation updated = plantationService.update(plantationId, toModel(request));
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{plantationId}")
    public ResponseEntity<Void> delete(
            @PathVariable String plantationId,
            Authentication authentication
    ) {
        requireRole(authentication, Role.ADMIN);

        Plantation existing = plantationService.findById(plantationId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plantation not found");
        }

        plantationService.delete(plantationId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    private void requireRole(Authentication authentication, Role requiredRole) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        AuthResponse session = authService.currentSession(authentication.getName());
        if (session.getRole() != requiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private Plantation toModel(PlantationRequestDto request) {
        Plantation plantation = new Plantation();
        plantation.setPlantationCode(request.getPlantationCode());
        plantation.setPlantationName(request.getPlantationName());
        plantation.setAreaHectares(request.getAreaHectares());
        plantation.setCorners(
                request.getCorners() == null
                        ? null
                        : request.getCorners().stream().map(this::toModel).toList()
        );
        return plantation;
    }

    private Coordinate toModel(CoordinateDto coordinateDto) {
        Coordinate coordinate = new Coordinate();
        coordinate.setX(coordinateDto.getX());
        coordinate.setY(coordinateDto.getY());
        return coordinate;
    }

    private PlantationResponseDto toResponse(Plantation plantation) {
        PlantationResponseDto response = new PlantationResponseDto();
        response.setPlantationId(plantation.getPlantationId());
        response.setPlantationCode(plantation.getPlantationCode());
        response.setPlantationName(plantation.getPlantationName());
        response.setAreaHectares(plantation.getAreaHectares());
        response.setCorners(
                plantation.getCorners() == null
                        ? List.of()
                        : plantation.getCorners().stream().map(this::toResponse).toList()
        );
        return response;
    }

    private CoordinateDto toResponse(Coordinate coordinate) {
        CoordinateDto response = new CoordinateDto();
        response.setX(coordinate.getX());
        response.setY(coordinate.getY());
        return response;
    }
}
