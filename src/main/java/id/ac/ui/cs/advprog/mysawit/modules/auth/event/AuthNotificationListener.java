package id.ac.ui.cs.advprog.mysawit.modules.auth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for auth-domain events and logs them.
 *
 * <p>Each listener method is annotated with {@link Async} so that event
 * processing does not block the originating request thread — making
 * notifications truly non-blocking.  To enable async processing, add
 * {@code @EnableAsync} to any {@code @Configuration} class (e.g. SecurityConfig).
 *
 * <p>Replace the log statements here with real notification logic
 * (e-mail, push notification, WebSocket broadcast, etc.) as needed.
 */
@Component
public class AuthNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AuthNotificationListener.class);

    /**
     * Handles {@link UserAssignmentEvent}: logs when a laborer is assigned
     * to or unassigned from a foreman.
     */
    @Async
    @EventListener
    public void onAssignmentChange(UserAssignmentEvent event) {
        switch (event.getType()) {
            case ASSIGNED -> log.info(
                    "[NOTIFICATION] Laborer '{}' (id={}) has been ASSIGNED to foreman id={}.",
                    event.getLaborerEmail(), event.getLaborerId(), event.getForemanId()
            );
            case UNASSIGNED -> log.info(
                    "[NOTIFICATION] Laborer '{}' (id={}) has been UNASSIGNED from their foreman.",
                    event.getLaborerEmail(), event.getLaborerId()
            );
        }
    }

    /**
     * Handles {@link UserLifecycleEvent}: logs when a user registers or is deleted.
     */
    @Async
    @EventListener
    public void onUserLifecycle(UserLifecycleEvent event) {
        switch (event.getType()) {
            case REGISTERED -> log.info(
                    "[NOTIFICATION] New user registered: username='{}', email='{}' (id={}).",
                    event.getUsername(), event.getUserEmail(), event.getUserId()
            );
            case DELETED -> log.info(
                    "[NOTIFICATION] User account deleted: username='{}', email='{}' (id={}).",
                    event.getUsername(), event.getUserEmail(), event.getUserId()
            );
        }
    }
}
