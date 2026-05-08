package id.ac.ui.cs.advprog.mysawit.modules.auth.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Secure session management configuration for the auth module.
 *
 * <p>Hardening measures applied here:
 * <ul>
 *   <li>Session timeout set to 30 minutes via the {@link HttpSessionListener} bean
 *       (supplements {@code server.servlet.session.timeout} in application.yaml).</li>
 *   <li>Session creation / destruction audit logging so security incidents can be traced.</li>
 * </ul>
 *
 * <p>Additional session hardening (fixation protection, max sessions per user,
 * session-cookie flags HttpOnly + Secure) is configured in {@code SecurityConfig}
 * via Spring Security's {@code sessionManagement()} DSL.
 */
@Configuration
public class SessionManagementConfig {

    private static final Logger log = LoggerFactory.getLogger(SessionManagementConfig.class);

    /** Session lifetime: 30 minutes (in seconds). */
    public static final int SESSION_MAX_INACTIVE_SECONDS = 1800;

    /**
     * Registers a listener that:
     * <ul>
     *   <li>Sets the session max-inactive interval to {@value #SESSION_MAX_INACTIVE_SECONDS} seconds.</li>
     *   <li>Logs session creation and destruction for audit purposes.</li>
     * </ul>
     */
    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {

            @Override
            public void sessionCreated(HttpSessionEvent se) {
                se.getSession().setMaxInactiveInterval(SESSION_MAX_INACTIVE_SECONDS);
                log.debug("[SESSION] Created sessionId={}", se.getSession().getId());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                log.info("[SESSION] Destroyed sessionId={} — reason: timeout or explicit logout.",
                        se.getSession().getId());
            }
        };
    }
}
