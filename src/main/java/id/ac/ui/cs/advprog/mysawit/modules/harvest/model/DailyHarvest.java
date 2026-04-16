package id.ac.ui.cs.advprog.mysawit.modules.harvest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "daily_harvest")
public class DailyHarvest {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String laborerName;

    @Column(nullable = false)
    private LocalDate harvestDate;

    @Column(nullable = false)
    private Double weightKg;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(nullable = false)
    private String photoPath;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String rejectionReason;

    private String reviewedBy;

    private LocalDateTime reviewedAt;
}