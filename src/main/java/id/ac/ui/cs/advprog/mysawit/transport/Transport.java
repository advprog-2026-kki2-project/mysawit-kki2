package id.ac.ui.cs.advprog.mysawit.transport;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverId; // Assigned Driver

    private double totalWeight; // Max 400kg capacity

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransportStatus status = TransportStatus.LOADING; // Default

    private LocalDateTime createdAt;

    private String rejectionReason; // For rejected results

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}