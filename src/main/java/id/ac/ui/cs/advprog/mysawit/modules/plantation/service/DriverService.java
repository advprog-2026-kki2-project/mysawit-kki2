package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Driver;

import java.util.List;

public interface DriverService {

    Driver create(Driver driver);

    List<Driver> findAll();

    Driver findById(String driverId);

    void delete(String driverId);

    void assignToPlantation(String driverId, String plantationId);

    void removeFromPlantation(String driverId, String plantationId);

    List<Driver> findByPlantation(String plantationId);

}
