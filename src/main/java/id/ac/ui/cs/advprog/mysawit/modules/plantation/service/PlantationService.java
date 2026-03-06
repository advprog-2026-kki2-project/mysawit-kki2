package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Plantation;

import java.util.List;

public interface PlantationService {

    Plantation create(Plantation plantation);

    List<Plantation> findAll();

    Plantation findById(String plantationId);

    Plantation update(String plantationId, Plantation plantation);

    void delete(String plantationId);

}
