package com.estore.library.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class ImageUpload {

    private static final Logger logger = LoggerFactory.getLogger(ImageUpload.class);

    // 1. Инжектируем путь для сохранения файлов на сервере (из application.properties)
    @Value("${file.upload-dir:./uploads/product-images/}")
    private String uploadDir;

    // 2. Инжектируем базовый публичный URL для доступа к файлам
    @Value("${file.public-base-url:http://localhost:8019/static/img/product-images/}")
    private String publicBaseUrl;

    /**
     * Сохраняет файл в локальной папке и возвращает публичный URL.
     * Создает уникальное имя файла.
     *
     * @param file Загружаемый файл
     * @return Публичный URL изображения или null в случае ошибки
     */
    public String uploadFileAndGetUrl(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        // Создаем директорию, если она не существует
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", uploadPath, e);
            throw new RuntimeException("Could not initialize storage location.", e);
        }

        try {
            // Генерируем уникальное имя файла для предотвращения конфликтов
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFileName);

            // Копируем файл в целевую директорию
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File uploaded successfully: {}", uniqueFileName);

            // Возвращаем публичный URL
            return publicBaseUrl + uniqueFileName;

        } catch (IOException e) {
            logger.error("Error saving file: {}", file.getOriginalFilename(), e);
            return null;
        }
    }

    // Метод для удаления файла (полезен, если удаляется ProductImage)
    public boolean deleteFile(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith(publicBaseUrl)) {
            return false;
        }

        try {
            String fileName = publicUrl.substring(publicBaseUrl.length());
            Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath().normalize();

            if (Files.exists(filePath)) {
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    logger.info("File deleted successfully: {}", fileName);
                } else {
                    logger.warn("Failed to delete file (file existed but couldn't be deleted): {}", fileName);
                }
                return deleted;
            } else {
                logger.warn("File not found for deletion: {}", fileName);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error deleting file: {}", publicUrl, e);
            return false;
        }
    }

}