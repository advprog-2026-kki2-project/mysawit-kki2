package id.ac.ui.cs.advprog.mysawit.modules.harvest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyHarvestRequestDto {

    private String laborerName;
    private LocalDate harvestDate;
    private Double weightKg;
    private String notes;
}
