package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Driver;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.DriverRepository;
import id.ac.ui.cs.advprog.mysawit.modules.plantation.repository.PlantationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final PlantationRepository plantationRepository;

    public DriverServiceImpl(
            final DriverRepository driverRepository,
            final PlantationRepository plantationRepository) {
        this.driverRepository = driverRepository;
        this.plantationRepository = plantationRepository;
    }

    @Override
    public Driver create(final Driver driver) {
        if (driver.getDriverName() == null || driver.getDriverName().trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty");
        }
        if (driver.getLicenseNumber() == null || driver.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("License number cannot be empty");
        }
        if (driverRepository.isLicenseExists(driver.getLicenseNumber())) {
            throw new IllegalArgumentException(
                    "License number already exists: " + driver.getLicenseNumber());
        }
        return driverRepository.create(driver);
    }

    @Override
    public List<Driver> findAll() {
        final Iterator<Driver> iterator = driverRepository.findAll();
        final List<Driver> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    @Override
    public Driver findById(final String driverId) {
        return driverRepository.findById(driverId);
    }

    @Override
    public void delete(final String driverId) {
        final Iterator<Plantation> plantations = plantationRepository.findAll();
        plantations.forEachRemaining(p -> p.getAssignedDriverIds().remove(driverId));
        driverRepository.delete(driverId);
    }

    @Override
    public void assignToPlantation(final String driverId, final String plantationId) {
        final Driver driver = driverRepository.findById(driverId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            throw new IllegalArgumentException("Plantation not found: " + plantationId);
        }
        if (!plantation.getAssignedDriverIds().contains(driverId)) {
            plantation.getAssignedDriverIds().add(driverId);
        }
    }

    @Override
    public void removeFromPlantation(final String driverId, final String plantationId) {
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            throw new IllegalArgumentException("Plantation not found: " + plantationId);
        }
        plantation.getAssignedDriverIds().remove(driverId);
    }

    @Override
    public List<Driver> findByPlantation(final String plantationId) {
        final Plantation plantation = plantationRepository.findById(plantationId);
        if (plantation == null) {
            return new ArrayList<>();
        }
        final List<Driver> result = new ArrayList<>();
        for (final String id : plantation.getAssignedDriverIds()) {
            final Driver d = driverRepository.findById(id);
            if (d != null) {
                result.add(d);
            }
        }
        return result;
    }

}
