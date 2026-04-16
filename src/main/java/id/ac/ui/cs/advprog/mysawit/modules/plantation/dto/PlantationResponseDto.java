package id.ac.ui.cs.advprog.mysawit.modules.plantation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlantationResponseDto {

    private String plantationId;
    private String plantationCode;
    private String plantationName;
    private double areaHectares;
    private List<CoordinateDto> corners;
}
