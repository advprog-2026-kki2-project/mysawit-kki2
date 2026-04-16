package id.ac.ui.cs.advprog.mysawit.modules.plantation.repository;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Driver;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public class DriverRepository {

    private final List<Driver> driverData = new ArrayList<>();

    public Driver create(final Driver driver) {
        driver.setDriverId(UUID.randomUUID().toString());
        driverData.add(driver);
        return driver;
    }

    public Iterator<Driver> findAll() {
        return driverData.iterator();
    }

    public Driver findById(final String driverId) {
        for (final Driver driver : driverData) {
            if (driver.getDriverId().equals(driverId)) {
                return driver;
            }
        }
        return null;
    }

    public boolean isLicenseExists(final String licenseNumber) {
        for (final Driver driver : driverData) {
            if (driver.getLicenseNumber().equals(licenseNumber)) {
                return true;
            }
        }
        return false;
    }

    public void delete(final String driverId) {
        driverData.removeIf(d -> d.getDriverId().equals(driverId));
    }

}
