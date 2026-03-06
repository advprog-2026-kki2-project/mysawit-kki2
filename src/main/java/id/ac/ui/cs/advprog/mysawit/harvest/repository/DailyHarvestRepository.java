package id.ac.ui.cs.advprog.mysawit.harvest.repository;

import id.ac.ui.cs.advprog.mysawit.harvest.model.DailyHarvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyHarvestRepository extends JpaRepository<DailyHarvest, String> {

    boolean existsByLaborerNameIgnoreCaseAndHarvestDate(String laborerName, LocalDate harvestDate);

    List<DailyHarvest> findByLaborerNameIgnoreCase(String laborerName);
}