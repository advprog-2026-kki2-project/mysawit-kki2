package id.ac.ui.cs.advprog.mysawit.modules.harvest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HarvestSubmissionResponseDto {

    private String message;
    private String id;
    private String laborerName;
    private LocalDate harvestDate;
    private Double weightKg;
    private String notes;
    private String status;
}
