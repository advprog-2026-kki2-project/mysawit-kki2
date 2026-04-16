package id.ac.ui.cs.advprog.mysawit.modules.plantation.repository;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Foreman;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public class ForemanRepository {

    private final List<Foreman> foremanData = new ArrayList<>();

    public Foreman create(final Foreman foreman) {
        foreman.setForemanId(UUID.randomUUID().toString());
        foremanData.add(foreman);
        return foreman;
    }

    public Iterator<Foreman> findAll() {
        return foremanData.iterator();
    }

    public Foreman findById(final String foremanId) {
        for (final Foreman foreman : foremanData) {
            if (foreman.getForemanId().equals(foremanId)) {
                return foreman;
            }
        }
        return null;
    }

    public boolean isEmployeeCodeExists(final String employeeCode) {
        for (final Foreman foreman : foremanData) {
            if (foreman.getEmployeeCode().equals(employeeCode)) {
                return true;
            }
        }
        return false;
    }

    public void delete(final String foremanId) {
        foremanData.removeIf(f -> f.getForemanId().equals(foremanId));
    }

}
