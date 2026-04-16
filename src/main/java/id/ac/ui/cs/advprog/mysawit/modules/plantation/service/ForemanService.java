package id.ac.ui.cs.advprog.mysawit.modules.plantation.service;

import id.ac.ui.cs.advprog.mysawit.modules.plantation.model.Foreman;

import java.util.List;

public interface ForemanService {

    Foreman create(Foreman foreman);

    List<Foreman> findAll();

    Foreman findById(String foremanId);

    void delete(String foremanId);

    void assignToPlantation(String foremanId, String plantationId);

    void removeFromPlantation(String foremanId, String plantationId);

    List<Foreman> findByPlantation(String plantationId);

}
