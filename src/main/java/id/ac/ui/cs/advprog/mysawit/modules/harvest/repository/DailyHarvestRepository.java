package id.ac.ui.cs.advprog.mysawit.modules.harvest.repository;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyHarvestRepository extends JpaRepository<DailyHarvest, String> {

    boolean existsByLaborerNameIgnoreCaseAndHarvestDate(String laborerName, LocalDate harvestDate);

    List<DailyHarvest> findByLaborerNameIgnoreCase(String laborerName);

    List<DailyHarvest> findByStatusIgnoreCase(String status);

    List<DailyHarvest> findByLaborerNameContainingIgnoreCase(String laborerName);

    List<DailyHarvest> findByHarvestDate(LocalDate harvestDate);

    List<DailyHarvest> findByLaborerNameContainingIgnoreCaseAndHarvestDate(String laborerName, LocalDate harvestDate);

    @Query("""
            SELECT h FROM DailyHarvest h
            WHERE (:laborerName IS NULL OR :laborerName = '' OR LOWER(h.laborerName) LIKE LOWER(CONCAT('%', :laborerName, '%')))
            AND (:status IS NULL OR :status = '' OR LOWER(h.status) = LOWER(:status))
            AND (:startDate IS NULL OR h.harvestDate >= :startDate)
            AND (:endDate IS NULL OR h.harvestDate <= :endDate)
            ORDER BY h.harvestDate DESC
            """)
    List<DailyHarvest> findLaborerHarvestHistory(
            @Param("laborerName") String laborerName,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}