package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Foreman;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.ForemanRepository;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.PlantationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ForemanServiceImpl implements ForemanService {

    private final ForemanRepository foremanRepository;
    private final PlantationRepository plantationRepository;

    public ForemanServiceImpl(
            final ForemanRepository foremanRepository,
            final PlantationRepository plantationRepository) {
        this.foremanRepository = foremanRepository;
        this.plantationRepository = plantationRepository;
    }

    @Override
    public Foreman create(final Foreman foreman) {
        if (foreman.getForemanName() == null || foreman.getForemanName().trim().isEmpty()) {
            throw new IllegalArgumentException("Foreman name cannot be empty");
        }
        if (foreman.getEmployeeCode() == null || foreman.getEmployeeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be empty");
        }
        if (foremanRepository.isEmployeeCodeExists(foreman.getEmployeeCode())) {
            throw new IllegalArgumentException(
                    "Employee code already exists: " + foreman.getEmployeeCode());
        }
        return foremanRepository.create(foreman);
    }

    @Override
    public List<Foreman> findAll() {
        final Iterator<Foreman> iterator = foremanRepository.findAll();
        final List<Foreman> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    @Override
    public Foreman findById(final String foremanId) {
        return foremanRepository.findById(foremanId);
    }

    @Override
    public void delete(final String foremanId) {
        final Iterator<Plantation> plantations = plantationRepository.findAll();
        plantations.forEachRemaining(p -> p.getAssignedForemanIds().remove(foremanId));
        foremanRepository.delete(foremanId);
    }

    @Override
    public void assignToPlantation(final String foremanId, final String plantationId) {
        final Foreman foreman = foremanRepository.findById(foremanId);
        if (foreman == null) {
            throw new IllegalArgumentException("Foreman not found: " + foremanId);
        }
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            throw new IllegalArgumentException("Plantation not found: " + plantationId);
        }
        if (!plantation.getAssignedForemanIds().contains(foremanId)) {
            plantation.getAssignedForemanIds().add(foremanId);
        }
    }

    @Override
    public void removeFromPlantation(final String foremanId, final String plantationId) {
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            throw new IllegalArgumentException("Plantation not found: " + plantationId);
        }
        plantation.getAssignedForemanIds().remove(foremanId);
    }

    @Override
    public List<Foreman> findByPlantation(final String plantationId) {
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            return new ArrayList<>();
        }
        final List<Foreman> result = new ArrayList<>();
        for (final String id : plantation.getAssignedForemanIds()) {
            final Foreman f = foremanRepository.findById(id);
            if (f != null) {
                result.add(f);
            }
        }
        return result;
    }

}
