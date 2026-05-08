package id.ac.ui.cs.advprog.mysawit.modules.plantation.repository;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantationRepositoryTest {

    private PlantationRepository plantationRepository;

    @BeforeEach
    void setUp() {
        plantationRepository = new PlantationRepository();

        Plantation p1 = new Plantation();
        p1.setPlantationName("Alpha Estate");
        p1.setPlantationCode("PLT-20240101-0001");
        p1.setAssignedForemanIds(List.of("F001", "F002"));
        plantationRepository.create(p1);

        Plantation p2 = new Plantation();
        p2.setPlantationName("Beta Estate");
        p2.setPlantationCode("PLT-20240102-0002");
        p2.setAssignedForemanIds(List.of("F002", "F003"));
        plantationRepository.create(p2);

        Plantation p3 = new Plantation();
        p3.setPlantationName("Gamma Plantation");
        p3.setPlantationCode("PLT-20240103-0003");
        p3.setAssignedForemanIds(List.of("F001", "F004"));
        plantationRepository.create(p3);
    }

    @Test
    void testSearchByName() {
        Iterator<Plantation> result = plantationRepository.search("estate", null, null);
        List<Plantation> plantations = new ArrayList<>();
        result.forEachRemaining(plantations::add);
        
        assertEquals(2, plantations.size());
        assertTrue(plantations.stream().anyMatch(p -> p.getPlantationName().equals("Alpha Estate")));
        assertTrue(plantations.stream().anyMatch(p -> p.getPlantationName().equals("Beta Estate")));
    }

    @Test
    void testSearchByCode() {
        Iterator<Plantation> result = plantationRepository.search(null, "PLT-20240102-0002", null);
        List<Plantation> plantations = new ArrayList<>();
        result.forEachRemaining(plantations::add);
        
        assertEquals(1, plantations.size());
        assertEquals("Beta Estate", plantations.get(0).getPlantationName());
    }

    @Test
    void testSearchByForemanId() {
        Iterator<Plantation> result = plantationRepository.search(null, null, "F002");
        List<Plantation> plantations = new ArrayList<>();
        result.forEachRemaining(plantations::add);
        
        assertEquals(2, plantations.size());
        assertTrue(plantations.stream().anyMatch(p -> p.getPlantationName().equals("Alpha Estate")));
        assertTrue(plantations.stream().anyMatch(p -> p.getPlantationName().equals("Beta Estate")));
    }

    @Test
    void testSearchByAllCriteria() {
        Iterator<Plantation> result = plantationRepository.search("gamma", "PLT-20240103-0003", "F001");
        List<Plantation> plantations = new ArrayList<>();
        result.forEachRemaining(plantations::add);
        
        assertEquals(1, plantations.size());
        assertEquals("Gamma Plantation", plantations.get(0).getPlantationName());
    }

    @Test
    void testSearchNoMatch() {
        Iterator<Plantation> result = plantationRepository.search("Delta", null, null);
        List<Plantation> plantations = new ArrayList<>();
        result.forEachRemaining(plantations::add);
        
        assertEquals(0, plantations.size());
    }
}
