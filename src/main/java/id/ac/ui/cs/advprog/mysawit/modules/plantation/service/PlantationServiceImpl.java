package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Coordinate;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.PlantationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PlantationServiceImpl implements PlantationService {

    /*
     * Code format: PLT-YYYYMMDD-XXXX
     * Example: PLT-20240315-0001
     */
    private static final Pattern CODE_PATTERN =
            Pattern.compile("^PLT-\\d{8}-\\d{4}$");

    private final PlantationRepository plantationRepository;

    public PlantationServiceImpl(final PlantationRepository plantationRepository) {
        this.plantationRepository = plantationRepository;
    }

    @Override
    public Plantation create(final Plantation plantation) {
        validatePlantation(plantation);
        if (plantationRepository.isCodeExists(plantation.getPlantationCode())) {
            throw new IllegalArgumentException(
                    "Plantation code already exists: " + plantation.getPlantationCode());
        }
        return plantationRepository.create(plantation);
    }

    @Override
    public List<Plantation> findAll() {
        final Iterator<Plantation> iterator = plantationRepository.findAll();
        final List<Plantation> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    @Override
    public Plantation findById(final String plantationId) {
        return plantationRepository.findById(plantationId);
    }

    @Override
    public Plantation update(final String plantationId, final Plantation plantation) {
       
        validateNameAndArea(plantation);
        validateCorners(plantation.getCorners());
        return plantationRepository.update(plantationId, plantation);
    }

    @Override
    public void delete(final String plantationId) {
        plantationRepository.delete(plantationId);
    }


    private void validatePlantation(final Plantation plantation) {
        validateNameAndArea(plantation);
        validateCode(plantation.getPlantationCode());
        validateCorners(plantation.getCorners());
    }

    private void validateNameAndArea(final Plantation plantation) {
        if (plantation.getPlantationName() == null
                || plantation.getPlantationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Plantation name cannot be empty");
        }
        if (plantation.getAreaHectares() <= 0) {
            throw new IllegalArgumentException("Area must be greater than 0");
        }
    }

    private void validateCode(final String code) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Plantation code must match format PLT-YYYYMMDD-XXXX (e.g. PLT-20240315-0001)");
        }
    }

    private void validateCorners(final List<Coordinate> corners) {
        if (corners == null || corners.size() != 4) {
            throw new IllegalArgumentException(
                    "Plantation must have exactly 4 corner coordinates");
        }
    }

}
