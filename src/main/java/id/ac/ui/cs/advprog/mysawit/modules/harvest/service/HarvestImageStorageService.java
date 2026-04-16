package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.springframework.web.multipart.MultipartFile;

public interface HarvestImageStorageService {
    String store(MultipartFile file);
}