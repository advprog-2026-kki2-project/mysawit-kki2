package id.ac.ui.cs.advprog.mysawit.modules.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event fired whenever a Laborer's foreman assignment changes.
 * Published after: assignForeman, unassignForeman.
 */
public class UserAssignmentEvent extends ApplicationEvent {

    public enum Type {
        ASSIGNED,
        UNASSIGNED
    }

    private final Type type;
    private final Long laborerId;
    private final String laborerEmail;
    /** Non-null when type == ASSIGNED; null when type == UNASSIGNED. */
    private final Long foremanId;

    public UserAssignmentEvent(Object source, Type type,
                               Long laborerId, String laborerEmail,
                               Long foremanId) {
        super(source);
        this.type = type;
        this.laborerId = laborerId;
        this.laborerEmail = laborerEmail;
        this.foremanId = foremanId;
    }

    public Type getType() {
        return type;
    }

    public Long getLaborerId() {
        return laborerId;
    }

    public String getLaborerEmail() {
        return laborerEmail;
    }

    public Long getForemanId() {
        return foremanId;
    }
}
