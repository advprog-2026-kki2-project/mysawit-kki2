package id.ac.ui.cs.advprog.mysawit.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignForemanRequest {
    /** The ID of the Foreman to assign the Laborer to. */
    private Long foremanId;
}
