package id.ac.ui.cs.advprog.mysawit.transport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.transport.dto.AdminVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.ForemanVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.transport.repository.TransportRepository;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller endpoint behavior tests using @SpringBootTest.
 * Validates HTTP status codes, request/response contracts, and error handling
 * through the full application context (real security config loaded).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TransportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private DailyHarvestRepository harvestRepository;

    private String harvestId;

    @BeforeEach
    void setUp() {
        transportRepository.deleteAll();
        harvestRepository.deleteAll();

        DailyHarvest harvest = new DailyHarvest();
        harvest.setLaborerName("Test-Laborer");
        harvest.setHarvestDate(LocalDate.now());
        harvest.setWeightKg(200.0);
        harvest.setNotes("Test harvest");
        harvest.setPhotoPath("/uploads/test.jpg");
        harvest.setStatus("APPROVED");
        harvest = harvestRepository.save(harvest);
        harvestId = harvest.getId();
    }

    // --- Authentication Tests ---

    @Test
    @DisplayName("POST /assign-pickup: Redirects (302) without authentication")
    void assignPickup_unauthorized() throws Exception {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setDriverId("driver-1");
        dto.setHarvestIds(List.of("h1"));

        mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is3xxRedirection());
    }

    // --- Assign Pickup Endpoint Tests ---

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("POST /assign-pickup: Returns 201 on successful pickup assignment")
    void assignPickup_success() throws Exception {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setDriverId("driver-123");
        dto.setHarvestIds(List.of(harvestId));

        mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value("driver-123"))
                .andExpect(jsonPath("$.totalWeight").value(200.0));
    }

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("POST /assign-pickup: Returns 400 when harvest not found")
    void assignPickup_harvestNotFound() throws Exception {
        PickupRequestDto dto = new PickupRequestDto();
        dto.setDriverId("driver-1");
        dto.setHarvestIds(List.of("nonexistent-harvest"));

        mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- Status Update Endpoint Tests ---

    @Test
    @WithMockUser(roles = {"DRIVER"})
    @DisplayName("PATCH /{id}/status: Returns 200 on status update to TRANSPORTING")
    void updateStatus_success() throws Exception {
        // Create transport first
        Transport t = Transport.builder()
                .driverId("driver-x").totalWeight(100.0).status(TransportStatus.LOADING).build();
        t = transportRepository.save(t);

        mockMvc.perform(patch("/api/transport/" + t.getId() + "/status")
                        .param("status", "TRANSPORTING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TRANSPORTING"));
    }

    @Test
    @WithMockUser(roles = {"DRIVER"})
    @DisplayName("PATCH /{id}/status: Returns 404 when transport not found")
    void updateStatus_notFound() throws Exception {
        mockMvc.perform(patch("/api/transport/99999/status")
                        .param("status", "ARRIVED"))
                .andExpect(status().isNotFound());
    }

    // --- Foreman Verify Endpoint Tests ---

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("POST /{id}/foreman-verify: Returns 200 on approval")
    void foremanVerify_approve() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-y").totalWeight(100.0).status(TransportStatus.ARRIVED).build();
        t = transportRepository.save(t);

        ForemanVerificationDto dto = new ForemanVerificationDto();
        dto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + t.getId() + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOREMAN_APPROVED"))
                .andExpect(jsonPath("$.foremanApproved").value(true));
    }

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("POST /{id}/foreman-verify: Returns 409 on wrong state")
    void foremanVerify_invalidState() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-z").totalWeight(100.0).status(TransportStatus.LOADING).build();
        t = transportRepository.save(t);

        ForemanVerificationDto dto = new ForemanVerificationDto();
        dto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + t.getId() + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("POST /{id}/foreman-verify: Returns 400 when reject reason missing")
    void foremanVerify_rejectWithoutReason() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-w").totalWeight(100.0).status(TransportStatus.ARRIVED).build();
        t = transportRepository.save(t);

        ForemanVerificationDto dto = new ForemanVerificationDto();
        dto.setApproved(false);
        // No rejection reason set

        mockMvc.perform(post("/api/transport/" + t.getId() + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- Admin Verify Endpoint Tests ---

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /{id}/admin-verify: Returns 200 on full approval")
    void adminVerify_fullApproval() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-admin").totalWeight(200.0)
                .status(TransportStatus.FOREMAN_APPROVED).foremanApproved(true).build();
        t = transportRepository.save(t);

        AdminVerificationDto dto = new AdminVerificationDto();
        dto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + t.getId() + "/admin-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMIN_APPROVED"))
                .andExpect(jsonPath("$.recognizedWeight").value(200.0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /{id}/admin-verify: Returns 409 when not foreman-approved")
    void adminVerify_invalidState() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-admin2").totalWeight(200.0)
                .status(TransportStatus.ARRIVED).build();
        t = transportRepository.save(t);

        AdminVerificationDto dto = new AdminVerificationDto();
        dto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + t.getId() + "/admin-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // --- List Endpoint Tests ---

    @Test
    @WithMockUser(roles = {"FOREMAN"})
    @DisplayName("GET /ongoing: Returns ongoing deliveries")
    void getOngoingDeliveries() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-ongoing").totalWeight(100.0).status(TransportStatus.LOADING).build();
        transportRepository.save(t);

        mockMvc.perform(get("/api/transport/ongoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /foreman-approved: Returns foreman-approved deliveries")
    void getForemanApprovedDeliveries() throws Exception {
        Transport t = Transport.builder()
                .driverId("driver-approved").totalWeight(100.0)
                .status(TransportStatus.FOREMAN_APPROVED).foremanApproved(true).build();
        transportRepository.save(t);

        mockMvc.perform(get("/api/transport/foreman-approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FOREMAN_APPROVED"));
    }
}
