package id.ac.ui.cs.advprog.mysawit.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Session management security tests.
 *
 * <p>Covers:
 * <ul>
 *   <li>Session fixation — the session ID must change after a successful login.</li>
 *   <li>Session invalidation — an invalidated session must be rejected on subsequent requests.</li>
 *   <li>Logout invalidates the session — accessing /session after logout returns 401.</li>
 *   <li>Unauthenticated access — /session without a session returns 401.</li>
 *   <li>Re-login after logout — a fresh login must succeed and produce a new valid session.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:session-mgmt-test;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SessionManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---- helpers ----

    private void register(String email, String username, String password, Role role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setUsername(username);
        req.setPassword(password);
        req.setRole(role);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    private MockHttpSession login(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    // ---- tests ----

    /**
     * Session fixation protection: the session obtained before login must be
     * different from the one issued after login.
     */
    @Test
    void login_changesSessionId_sessionFixationProtection() throws Exception {
        register("fix@test.com", "fixUser", "Harvest123!", Role.LABORER);

        // Pre-login: obtain an anonymous session
        MvcResult preLogin = mockMvc.perform(get("/api/auth/session"))
                .andReturn();
        MockHttpSession anonSession = (MockHttpSession) preLogin.getRequest().getSession(true);
        String anonSessionId = anonSession.getId();

        // Perform login using the anonymous session to simulate a fixation attack
        LoginRequest req = new LoginRequest();
        req.setEmail("fix@test.com");
        req.setPassword("Harvest123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .session(anonSession))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession postLoginSession =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        // The session must be valid and its ID must differ from the pre-login session
        assertThat(postLoginSession).isNotNull();
        assertThat(postLoginSession.getId()).isNotEqualTo(anonSessionId);
    }

    /**
     * After logout the session is invalidated; the /session endpoint must return 401.
     */
    @Test
    void logout_invalidatesSession_subsequentRequestReturns401() throws Exception {
        register("logout@test.com", "logoutUser", "Harvest123!", Role.LABORER);
        MockHttpSession session = login("logout@test.com", "Harvest123!");

        // Confirm session is active
        mockMvc.perform(get("/api/auth/session").session(session))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        // Session must now be rejected
        mockMvc.perform(get("/api/auth/session").session(session))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Accessing /session without any session returns 401.
     */
    @Test
    void session_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/session"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * An explicitly invalidated session must be rejected.
     */
    @Test
    void invalidatedSession_isRejected() throws Exception {
        register("inv@test.com", "invUser", "Harvest123!", Role.LABORER);
        MockHttpSession session = login("inv@test.com", "Harvest123!");

        // Manually invalidate the session (simulates server-side expiry / admin revocation)
        session.invalidate();

        mockMvc.perform(get("/api/auth/session").session(session))
                .andExpect(status().isUnauthorized());
    }

    /**
     * After logout a user can log in again and receive a valid new session.
     */
    @Test
    void relogin_afterLogout_succeeds() throws Exception {
        register("relogin@test.com", "reloginUser", "Harvest123!", Role.LABORER);
        MockHttpSession firstSession = login("relogin@test.com", "Harvest123!");

        // First logout
        mockMvc.perform(post("/api/auth/logout").session(firstSession))
                .andExpect(status().isOk());

        // Second login — must succeed
        MockHttpSession secondSession = login("relogin@test.com", "Harvest123!");
        assertThat(secondSession).isNotNull();

        mockMvc.perform(get("/api/auth/session").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reloginUser"));
    }

    /**
     * Attempting to log in with wrong credentials must return 401.
     */
    @Test
    void login_withWrongCredentials_returns401() throws Exception {
        register("creds@test.com", "credsUser", "Harvest123!", Role.LABORER);

        LoginRequest req = new LoginRequest();
        req.setEmail("creds@test.com");
        req.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
