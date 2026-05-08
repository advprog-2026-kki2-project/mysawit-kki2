package id.ac.ui.cs.advprog.mysawit.modules.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event fired for general user lifecycle notifications:
 * user registered, user deleted.
 */
public class UserLifecycleEvent extends ApplicationEvent {

    public enum Type {
        REGISTERED,
        DELETED
    }

    private final Type type;
    private final Long userId;
    private final String userEmail;
    private final String username;

    public UserLifecycleEvent(Object source, Type type,
                              Long userId, String userEmail,
                              String username) {
        super(source);
        this.type = type;
        this.userId = userId;
        this.userEmail = userEmail;
        this.username = username;
    }

    public Type getType() {
        return type;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUsername() {
        return username;
    }
}
