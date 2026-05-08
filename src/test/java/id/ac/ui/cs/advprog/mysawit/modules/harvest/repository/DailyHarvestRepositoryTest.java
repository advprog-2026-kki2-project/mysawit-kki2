package id.ac.ui.cs.advprog.mysawit.modules.harvest.repository;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DailyHarvestRepositoryTest {

    @Autowired
    private DailyHarvestRepository repository;

    @Test
    void findLaborerHarvestHistoryShouldFilterByLaborerNameStatusAndDateRange() {
        DailyHarvest harvest1 = createHarvest(
                "Rani",
                LocalDate.of(2026, 5, 1),
                "PENDING"
        );

        DailyHarvest harvest2 = createHarvest(
                "Rani",
                LocalDate.of(2026, 5, 5),
                "APPROVED"
        );

        DailyHarvest harvest3 = createHarvest(
                "Budi",
                LocalDate.of(2026, 5, 5),
                "PENDING"
        );

        repository.save(harvest1);
        repository.save(harvest2);
        repository.save(harvest3);

        List<DailyHarvest> result = repository.findLaborerHarvestHistory(
                "Rani",
                "PENDING",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3)
        );

        assertEquals(1, result.size());
        assertEquals("Rani", result.get(0).getLaborerName());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals(LocalDate.of(2026, 5, 1), result.get(0).getHarvestDate());
    }

    @Test
    void findLaborerHarvestHistoryShouldReturnRejectedHarvestWithReason() {
        DailyHarvest rejectedHarvest = createHarvest(
                "Rani",
                LocalDate.of(2026, 5, 7),
                "REJECTED"
        );
        rejectedHarvest.setRejectionReason("Photo evidence is unclear.");

        repository.save(rejectedHarvest);

        List<DailyHarvest> result = repository.findLaborerHarvestHistory(
                "Rani",
                "REJECTED",
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("REJECTED", result.get(0).getStatus());
        assertEquals("Photo evidence is unclear.", result.get(0).getRejectionReason());
    }

    private DailyHarvest createHarvest(String laborerName, LocalDate harvestDate, String status) {
        DailyHarvest harvest = new DailyHarvest();
        harvest.setLaborerName(laborerName);
        harvest.setHarvestDate(harvestDate);
        harvest.setWeightKg(100.0);
        harvest.setNotes("Test harvest");
        harvest.setPhotoPath("uploads/test.jpg");
        harvest.setStatus(status);
        return harvest;
    }
}