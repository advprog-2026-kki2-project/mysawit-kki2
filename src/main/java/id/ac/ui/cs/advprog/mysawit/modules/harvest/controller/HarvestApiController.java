package id.ac.ui.cs.advprog.mysawit.modules.harvest.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.HarvestSubmissionRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.HarvestSubmissionResponseDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.service.DailyHarvestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/harvests")
public class HarvestApiController {

    private final DailyHarvestService dailyHarvestService;
    private final AuthService authService;

    public HarvestApiController(DailyHarvestService dailyHarvestService, AuthService authService) {
        this.dailyHarvestService = dailyHarvestService;
        this.authService = authService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HarvestSubmissionResponseDto> submitHarvest(
            @ModelAttribute HarvestSubmissionRequestDto request,
            @RequestParam("photo") MultipartFile photo,
            Authentication authentication
    ) {
        AuthResponse session = requireRole(authentication, Role.LABORER);

        DailyHarvestRequestDto dailyHarvestRequest = new DailyHarvestRequestDto();
        dailyHarvestRequest.setLaborerName(session.getUsername());
        dailyHarvestRequest.setHarvestDate(request.getHarvestDate());
        dailyHarvestRequest.setWeightKg(request.getWeightKg());
        dailyHarvestRequest.setNotes(request.getNotes());

        DailyHarvest createdHarvest = dailyHarvestService.recordHarvest(dailyHarvestRequest, photo);

        HarvestSubmissionResponseDto response = new HarvestSubmissionResponseDto();
        response.setMessage("Harvest submitted successfully.");
        response.setId(createdHarvest.getId());
        response.setLaborerName(createdHarvest.getLaborerName());
        response.setHarvestDate(createdHarvest.getHarvestDate());
        response.setWeightKg(createdHarvest.getWeightKg());
        response.setNotes(createdHarvest.getNotes());
        response.setStatus(createdHarvest.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    private AuthResponse requireRole(Authentication authentication, Role requiredRole) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        AuthResponse session = authService.currentSession(authentication.getName());
        if (session.getRole() != requiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return session;
    }
}
