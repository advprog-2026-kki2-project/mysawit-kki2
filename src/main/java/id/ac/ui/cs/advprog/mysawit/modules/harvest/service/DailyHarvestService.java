package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DailyHarvestService {

    private final DailyHarvestRepository repository;
    private final HarvestImageStorageService imageStorageService;

    public DailyHarvestService(DailyHarvestRepository repository,
                               HarvestImageStorageService imageStorageService) {
        this.repository = repository;
        this.imageStorageService = imageStorageService;
    }

    public DailyHarvest recordHarvest(DailyHarvestRequestDto dto, MultipartFile photo) {
        if (dto.getLaborerName() == null || dto.getLaborerName().isBlank()) {
            throw new IllegalArgumentException("Laborer name is required.");
        }

        if (dto.getHarvestDate() == null) {
            throw new IllegalArgumentException("Harvest date is required.");
        }

        if (dto.getWeightKg() == null) {
            throw new IllegalArgumentException("Weight is required.");
        }

        if (dto.getWeightKg() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }

        if (dto.getNotes() == null || dto.getNotes().isBlank()) {
            throw new IllegalArgumentException("Notes are required.");
        }

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Photo evidence is required.");
        }

        boolean exists = repository.existsByLaborerNameIgnoreCaseAndHarvestDate(
                dto.getLaborerName(),
                dto.getHarvestDate()
        );

        if (exists) {
            throw new IllegalArgumentException("Harvest already submitted for this date.");
        }

        String photoPath = imageStorageService.store(photo);

        DailyHarvest harvest = new DailyHarvest();
        harvest.setLaborerName(dto.getLaborerName());
        harvest.setHarvestDate(dto.getHarvestDate());
        harvest.setWeightKg(dto.getWeightKg());
        harvest.setNotes(dto.getNotes());
        harvest.setPhotoPath(photoPath);
        harvest.setStatus("PENDING");
        harvest.setRejectionReason(null);
        harvest.setReviewedBy(null);
        harvest.setReviewedAt(null);

        return repository.save(harvest);
    }

    public List<DailyHarvest> getAllHarvests(String laborerName, LocalDate harvestDate) {
        boolean hasName = laborerName != null && !laborerName.isBlank();
        boolean hasDate = harvestDate != null;

        if (hasName && hasDate) {
            return repository.findByLaborerNameContainingIgnoreCaseAndHarvestDate(laborerName, harvestDate);
        } else if (hasName) {
            return repository.findByLaborerNameContainingIgnoreCase(laborerName);
        } else if (hasDate) {
            return repository.findByHarvestDate(harvestDate);
        }

        return repository.findAll();
    }
    public List<DailyHarvest> getLaborerHarvestHistory(
        String laborerName,
        String status,
        LocalDate startDate,
        LocalDate endDate
) {
    return repository.findLaborerHarvestHistory(
            laborerName,
            status,
            startDate,
            endDate
    );
}

    public DailyHarvest getHarvestById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Harvest not found."));
    }

    public DailyHarvest approveHarvest(String id, String foremanName) {
        DailyHarvest harvest = getHarvestById(id);

        if (!"PENDING".equalsIgnoreCase(harvest.getStatus())) {
            throw new IllegalArgumentException("Only pending harvest can be approved.");
        }

        harvest.setStatus("APPROVED");
        harvest.setRejectionReason(null);
        harvest.setReviewedBy(foremanName);
        harvest.setReviewedAt(LocalDateTime.now());

        return repository.save(harvest);
    }

    public DailyHarvest rejectHarvest(String id, String foremanName, String reason) {
        DailyHarvest harvest = getHarvestById(id);

        if (!"PENDING".equalsIgnoreCase(harvest.getStatus())) {
            throw new IllegalArgumentException("Only pending harvest can be rejected.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        harvest.setStatus("REJECTED");
        harvest.setRejectionReason(reason);
        harvest.setReviewedBy(foremanName);
        harvest.setReviewedAt(LocalDateTime.now());

        return repository.save(harvest);
    }
    
}