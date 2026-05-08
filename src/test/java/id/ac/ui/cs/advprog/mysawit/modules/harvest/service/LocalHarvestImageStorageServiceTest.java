package id.ac.ui.cs.advprog.mysawit.modules.harvest.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalHarvestImageStorageServiceTest {

    private final LocalHarvestImageStorageService storageService =
            new LocalHarvestImageStorageService();

    @Test
    void storeShouldResizeAndCompressUploadedImage() throws Exception {
        BufferedImage originalImage = new BufferedImage(
                1600,
                1200,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = originalImage.createGraphics();
        graphics.fillRect(0, 0, 1600, 1200);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "png", outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "large-image.png",
                "image/png",
                outputStream.toByteArray()
        );

        String savedPath = storageService.store(file);

        Path path = Path.of(savedPath);

        assertTrue(Files.exists(path));

        BufferedImage savedImage = ImageIO.read(path.toFile());

        assertNotNull(savedImage);
        assertEquals(800, savedImage.getWidth());
        assertEquals(600, savedImage.getHeight());

        Files.deleteIfExists(path);
    }
}