package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import id.ac.ui.cs.advprog.mysawit.modules.harvest.dto.DailyHarvestRequestDto;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.model.DailyHarvest;
import id.ac.ui.cs.advprog.mysawit.modules.harvest.repository.DailyHarvestRepository;

@Service
public class DailyHarvestService {

    private final DailyHarvestRepository repository;
    private final HarvestImageStorageService imageStorageService;

    public DailyHarvestService(DailyHarvestRepository repository,
                               HarvestImageStorageService imageStorageService) {
        this.repository = repository;
        this.imageStorageService = imageStorageService;
    }

    public void recordHarvest(DailyHarvestRequestDto dto, MultipartFile photo) {
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

        repository.save(harvest);
    }
}