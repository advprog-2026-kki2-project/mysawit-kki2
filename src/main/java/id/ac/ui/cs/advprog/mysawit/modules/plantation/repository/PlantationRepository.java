package id.ac.ui.cs.advprog.mysawit.modules.plantation.repository;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository

public class PlantationRepository {

    private List<Plantation> plantationData = new ArrayList<>();

    public Plantation create(final Plantation plantation) {
        plantation.setPlantationId(UUID.randomUUID().toString());
        plantationData.add(plantation);
        return plantation;
    }

    public Iterator<Plantation> findAll() {
        return plantationData.iterator();
    }

    public Plantation findById(final String plantationId) {
        Plantation found = null;
        for (final Plantation plantation : plantationData) {
            if (plantation.getPlantationId().equals(plantationId)) {
                found = plantation;
                break;
            }
        }
        return found;
    }

    public boolean isCodeExists(final String plantationCode) {
        for (final Plantation plantation : plantationData) {
            if (plantation.getPlantationCode().equals(plantationCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCodeExistsExcluding(final String plantationCode, final String excludeId) {
        for (final Plantation plantation : plantationData) {
            if (plantation.getPlantationCode().equals(plantationCode)
                    && !plantation.getPlantationId().equals(excludeId)) {
                return true;
            }
        }
        return false;
    }

    public Plantation update(final String plantationId, final Plantation updatedPlantation) {
        Plantation result = null;
        for (int i = 0; i < plantationData.size(); i++) {
            final Plantation plantation = plantationData.get(i);
            if (plantation.getPlantationId().equals(plantationId)) {
                updatedPlantation.setPlantationId(plantationId);
                // Code must NOT change on update — preserve the original code
                updatedPlantation.setPlantationCode(plantation.getPlantationCode());
                plantationData.set(i, updatedPlantation);
                result = updatedPlantation;
                break;
            }
        }
        return result;
    }

    public void delete(final String plantationId) {
        plantationData.removeIf(plantation -> plantation.getPlantationId().equals(plantationId));
    }

}
