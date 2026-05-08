package id.ac.ui.cs.advprog.mysawit.transport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;
import id.ac.ui.cs.advprog.mysawit.transport.dto.AdminVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.ForemanVerificationDto;
import id.ac.ui.cs.advprog.mysawit.transport.dto.PickupRequestDto;
import id.ac.ui.cs.advprog.mysawit.transport.model.Transport;
import id.ac.ui.cs.advprog.mysawit.transport.model.TransportStatus;
import id.ac.ui.cs.advprog.mysawit.transport.repository.TransportRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: Full harvest-to-factory data flow.
 *
 * This test verifies the complete lifecycle:
 * 1. Laborer harvest is pre-seeded as APPROVED (simulating Foreman harvest approval)
 * 2. Foreman assigns a Truck Driver to pick up approved harvest (400kg constraint)
 * 3. Driver updates status: LOADING -> TRANSPORTING -> ARRIVED
 * 4. Foreman verifies the arrived delivery (Stage 1 — triggers Driver payroll)
 * 5. Central Admin verifies the foreman-approved delivery (Stage 2 — triggers Foreman payroll)
 *
 * All interactions use HTTP API endpoints via MockMvc, validating the system design
 * constraint that modules interact through clearly defined communication mechanisms.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DailyHarvestRepository harvestRepository;

    @Autowired
    private TransportRepository transportRepository;

    private static String harvestId;

    @BeforeEach
    void setUp() {
        transportRepository.deleteAll();
        harvestRepository.deleteAll();

        // Pre-seed an approved harvest (simulating the completed harvest module flow)
        DailyHarvest harvest = new DailyHarvest();
        harvest.setLaborerName("Laborer-Ahmad");
        harvest.setHarvestDate(LocalDate.now());
        harvest.setWeightKg(250.0);
        harvest.setNotes("Good quality harvest from Block A");
        harvest.setPhotoPath("/uploads/harvest-evidence.jpg");
        harvest.setStatus("APPROVED");
        harvest = harvestRepository.save(harvest);
        harvestId = harvest.getId();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "foreman-budi", roles = {"FOREMAN"})
    @DisplayName("Full Flow: Assign Pickup -> Driver Status Updates -> Foreman Approve -> Admin Approve")
    void testFullHarvestToFactoryFlow() throws Exception {
        // Step 1: Foreman assigns a Truck Driver to pick up the approved harvest
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-rudi");
        pickupDto.setHarvestIds(List.of(harvestId));

        MvcResult pickupResult = mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Transport transport = objectMapper.readValue(
                pickupResult.getResponse().getContentAsString(), Transport.class);
        Long transportId = transport.getId();

        assertNotNull(transportId);
        assertEquals(250.0, transport.getTotalWeight());
        assertEquals(TransportStatus.LOADING, transport.getStatus());

        // Step 2: Driver updates status to TRANSPORTING
        mockMvc.perform(patch("/api/transport/" + transportId + "/status")
                        .param("status", "TRANSPORTING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TRANSPORTING"));

        // Step 3: Driver updates status to ARRIVED
        mockMvc.perform(patch("/api/transport/" + transportId + "/status")
                        .param("status", "ARRIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARRIVED"));

        // Step 4: Foreman approves the arrived delivery (Stage 1)
        ForemanVerificationDto foremanDto = new ForemanVerificationDto();
        foremanDto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + transportId + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foremanDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOREMAN_APPROVED"))
                .andExpect(jsonPath("$.foremanApproved").value(true));

        // Step 5: Central Admin approves the delivery (Stage 2 — full approval)
        AdminVerificationDto adminDto = new AdminVerificationDto();
        adminDto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + transportId + "/admin-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMIN_APPROVED"))
                .andExpect(jsonPath("$.adminApproved").value(true))
                .andExpect(jsonPath("$.recognizedWeight").value(250.0));

        // Final assertion: verify the full state in the database
        Transport finalTransport = transportRepository.findById(transportId).orElseThrow();
        assertEquals(TransportStatus.ADMIN_APPROVED, finalTransport.getStatus());
        assertTrue(finalTransport.getForemanApproved());
        assertTrue(finalTransport.getAdminApproved());
        assertEquals(250.0, finalTransport.getRecognizedWeight());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "foreman-budi", roles = {"FOREMAN"})
    @DisplayName("Foreman Rejection Flow: Assign Pickup -> Arrive -> Foreman Reject")
    void testForemanRejectionFlow() throws Exception {
        // Assign pickup
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-siti");
        pickupDto.setHarvestIds(List.of(harvestId));

        MvcResult pickupResult = mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Transport transport = objectMapper.readValue(
                pickupResult.getResponse().getContentAsString(), Transport.class);
        Long transportId = transport.getId();

        // Driver arrives
        mockMvc.perform(patch("/api/transport/" + transportId + "/status")
                        .param("status", "ARRIVED"))
                .andExpect(status().isOk());

        // Foreman rejects with reason
        ForemanVerificationDto rejectDto = new ForemanVerificationDto();
        rejectDto.setApproved(false);
        rejectDto.setRejectionReason("Quality below standard — palm oil appears contaminated");

        mockMvc.perform(post("/api/transport/" + transportId + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOREMAN_REJECTED"))
                .andExpect(jsonPath("$.foremanApproved").value(false))
                .andExpect(jsonPath("$.foremanRejectionReason")
                        .value("Quality below standard — palm oil appears contaminated"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin-central", roles = {"ADMIN"})
    @DisplayName("Admin Partial Rejection Flow: Foreman Approved -> Admin Partial Reject")
    void testAdminPartialRejectionFlow() throws Exception {
        // Assign pickup
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-ari");
        pickupDto.setHarvestIds(List.of(harvestId));

        MvcResult pickupResult = mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Transport transport = objectMapper.readValue(
                pickupResult.getResponse().getContentAsString(), Transport.class);
        Long transportId = transport.getId();

        // Driver arrives
        mockMvc.perform(patch("/api/transport/" + transportId + "/status")
                        .param("status", "ARRIVED"))
                .andExpect(status().isOk());

        // Foreman approves
        ForemanVerificationDto foremanDto = new ForemanVerificationDto();
        foremanDto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + transportId + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foremanDto)))
                .andExpect(status().isOk());

        // Admin partial rejection: only 180kg out of 250kg recognized
        AdminVerificationDto adminDto = new AdminVerificationDto();
        adminDto.setApproved(false);
        adminDto.setRecognizedWeight(180.0);
        adminDto.setRejectionReason("70kg of palm oil was spoiled during transit");

        mockMvc.perform(post("/api/transport/" + transportId + "/admin-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMIN_APPROVED"))
                .andExpect(jsonPath("$.adminApproved").value(true))
                .andExpect(jsonPath("$.recognizedWeight").value(180.0))
                .andExpect(jsonPath("$.adminRejectionReason")
                        .value("70kg of palm oil was spoiled during transit"));

        // Verify in database
        Transport finalTransport = transportRepository.findById(transportId).orElseThrow();
        assertEquals(180.0, finalTransport.getRecognizedWeight());
        assertEquals(TransportStatus.ADMIN_APPROVED, finalTransport.getStatus());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "foreman-budi", roles = {"FOREMAN"})
    @DisplayName("400kg Capacity Constraint: Reject pickup exceeding limit")
    void testCapacityConstraintEnforcement() throws Exception {
        // Create a second harvest to exceed 400kg
        DailyHarvest heavyHarvest = new DailyHarvest();
        heavyHarvest.setLaborerName("Laborer-Dewi");
        heavyHarvest.setHarvestDate(LocalDate.now().minusDays(1));
        heavyHarvest.setWeightKg(200.0);
        heavyHarvest.setNotes("Large batch harvest");
        heavyHarvest.setPhotoPath("/uploads/harvest-heavy.jpg");
        heavyHarvest.setStatus("APPROVED");
        heavyHarvest = harvestRepository.save(heavyHarvest);

        // Try to assign both harvests (250 + 200 = 450kg > 400kg limit)
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-budi");
        pickupDto.setHarvestIds(List.of(harvestId, heavyHarvest.getId()));

        mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "foreman-budi", roles = {"FOREMAN"})
    @DisplayName("State Machine: Cannot verify non-ARRIVED delivery")
    void testInvalidStateTransition() throws Exception {
        // Create a transport in LOADING state
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-test");
        pickupDto.setHarvestIds(List.of(harvestId));

        MvcResult pickupResult = mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Transport transport = objectMapper.readValue(
                pickupResult.getResponse().getContentAsString(), Transport.class);

        // Try to verify before arrival — should fail
        ForemanVerificationDto dto = new ForemanVerificationDto();
        dto.setApproved(true);

        mockMvc.perform(post("/api/transport/" + transport.getId() + "/foreman-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "foreman-budi", roles = {"FOREMAN"})
    @DisplayName("Driver Delivery History: View deliveries for a specific driver")
    void testDriverDeliveryHistory() throws Exception {
        // Assign a pickup first
        PickupRequestDto pickupDto = new PickupRequestDto();
        pickupDto.setDriverId("driver-history-test");
        pickupDto.setHarvestIds(List.of(harvestId));

        mockMvc.perform(post("/api/transport/assign-pickup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isCreated());

        // Query driver deliveries
        mockMvc.perform(get("/api/transport/driver/driver-history-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].driverId").value("driver-history-test"));
    }
}
