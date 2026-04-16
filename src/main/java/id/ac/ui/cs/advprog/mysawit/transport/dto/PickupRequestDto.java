package id.ac.ui.cs.advprog.mysawit.transport.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PickupRequestDto {
    private String driverId;
    private List<String> harvestIds; // IDs from DailyHarvest module
}