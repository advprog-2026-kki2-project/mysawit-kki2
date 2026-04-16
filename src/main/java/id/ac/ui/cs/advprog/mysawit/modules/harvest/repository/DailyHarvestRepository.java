package id.ac.ui.cs.advprog.mysawit.modules.harvest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;

import java.time.LocalDate;
import java.util.List;

public interface DailyHarvestRepository extends JpaRepository<DailyHarvest, String> {

    boolean existsByLaborerNameIgnoreCaseAndHarvestDate(String laborerName, LocalDate harvestDate);

    List<DailyHarvest> findByLaborerNameIgnoreCase(String laborerName);

    List<DailyHarvest> findByStatusIgnoreCase(String status);

    List<DailyHarvest> findByLaborerNameContainingIgnoreCase(String laborerName);

    List<DailyHarvest> findByHarvestDate(LocalDate harvestDate);

    List<DailyHarvest> findByLaborerNameContainingIgnoreCaseAndHarvestDate(String laborerName, LocalDate harvestDate);
}