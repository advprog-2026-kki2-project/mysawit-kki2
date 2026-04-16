package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalHarvestImageStorageService implements HarvestImageStorageService {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

    @Override
    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Photo evidence is required.");
            }

            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "image.jpg";
            }

            String safeFilename = originalFilename.replaceAll("[\\\\/:*?\"<>| ]", "_");
            String filename = UUID.randomUUID() + "_" + safeFilename;

            Path destination = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), destination);

            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image.", e);
        }
    }
}