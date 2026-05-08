package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.UUID;

@Service
public class LocalHarvestImageStorageService implements HarvestImageStorageService {

    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"), "uploads");

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

            String safeFilename =
                    originalFilename.replaceAll("[\\\\/:*?\"<>| ]", "_");

            String filename =
                    UUID.randomUUID() + "_" + safeFilename;

            Path destination = uploadDir.resolve(filename);

            BufferedImage originalImage =
                    ImageIO.read(file.getInputStream());

            if (originalImage == null) {
                throw new IllegalArgumentException("Invalid image file.");
            }

            int targetWidth = 800;
            int targetHeight = 600;

            BufferedImage resizedImage =
                    new BufferedImage(
                            targetWidth,
                            targetHeight,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D graphics = resizedImage.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            graphics.drawImage(
                    originalImage,
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    null
            );

            graphics.dispose();

            ImageIO.write(
                    resizedImage,
                    "jpg",
                    destination.toFile()
            );

            return destination.toString();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to save compressed image.",
                    e
            );
        }
    }
}