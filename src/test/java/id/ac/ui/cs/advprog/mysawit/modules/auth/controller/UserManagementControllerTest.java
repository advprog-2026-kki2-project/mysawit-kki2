package id.ac.ui.cs.advprog.mysawit.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AssignForemanRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-mgmt-test;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession adminSession;
    private MockHttpSession laborerSession;
    private Long laborerId;
    private Long foremanId;

    @BeforeEach
    void setUp() throws Exception {
        // Register admin
        RegisterRequest adminReg = new RegisterRequest();
        adminReg.setEmail("admin@test.com");
        adminReg.setUsername("admin1");
        adminReg.setPassword("Admin123!");
        adminReg.setRole(Role.ADMIN);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReg)));

        // Register laborer
        RegisterRequest laborerReg = new RegisterRequest();
        laborerReg.setEmail("laborer@test.com");
        laborerReg.setUsername("laborer1");
        laborerReg.setPassword("Laborer123!");
        laborerReg.setRole(Role.LABORER);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(laborerReg)));

        // Register foreman
        RegisterRequest foremanReg = new RegisterRequest();
        foremanReg.setEmail("foreman@test.com");
        foremanReg.setUsername("foreman1");
        foremanReg.setPassword("Foreman123!");
        foremanReg.setRole(Role.FOREMAN);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(foremanReg)));

        // Log in as admin to get session
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@test.com");
        adminLogin.setPassword("Admin123!");
        adminSession = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andReturn().getRequest().getSession(false);

        // Log in as laborer to get session
        LoginRequest laborerLogin = new LoginRequest();
        laborerLogin.setEmail("laborer@test.com");
        laborerLogin.setPassword("Laborer123!");
        laborerSession = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laborerLogin)))
                .andReturn().getRequest().getSession(false);

        // Fetch user IDs
        String usersJson = mockMvc.perform(get("/api/users").session(adminSession))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode nodes = objectMapper.readTree(usersJson);
        for (com.fasterxml.jackson.databind.JsonNode node : nodes) {
            if ("LABORER".equals(node.get("role").asText())) {
                laborerId = node.get("id").asLong();
            } else if ("FOREMAN".equals(node.get("role").asText())) {
                foremanId = node.get("id").asLong();
            }
        }
    }

    @Test
    void listUsers_adminCanListAllUsers() throws Exception {
        mockMvc.perform(get("/api/users").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listUsers_nonAdminIsRejected() throws Exception {
        mockMvc.perform(get("/api/users").session(laborerSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsers_filterByRole() throws Exception {
        mockMvc.perform(get("/api/users?role=LABORER").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("LABORER"));
    }

    @Test
    void assignForeman_success() throws Exception {
        AssignForemanRequest req = new AssignForemanRequest();
        req.setForemanId(foremanId);

        mockMvc.perform(put("/api/users/" + laborerId + "/foreman")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foremanId").value(foremanId));
    }

    @Test
    void unassignForeman_success() throws Exception {
        // First assign
        AssignForemanRequest req = new AssignForemanRequest();
        req.setForemanId(foremanId);
        mockMvc.perform(put("/api/users/" + laborerId + "/foreman")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Then unassign
        mockMvc.perform(delete("/api/users/" + laborerId + "/foreman")
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foremanId").isEmpty());
    }

    @Test
    void deleteUser_laborer_success() throws Exception {
        mockMvc.perform(delete("/api/users/" + laborerId)
                        .session(adminSession))
                .andExpect(status().isNoContent());
    }
}
