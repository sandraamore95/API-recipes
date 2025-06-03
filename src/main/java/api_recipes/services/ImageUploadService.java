package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ImageUploadService {
    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
    ));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif"
    ));

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // ingredients | recipes -> baseDir
    // ingredient  | recipes -> prefix
    public String uploadImage(MultipartFile file, String baseDir, String prefix, Long id) throws IOException {
        logger.info("Iniciando subida de imagen - Directorio base: {}, Prefijo: {}, ID: {}", baseDir, prefix, id);
        
        validateFile(file);

        // Crear directorio si no existe ->   (hay recetas creadas sin carpeta de imagen )
        String uploadDir = "uploads/images/" + baseDir;
        if (id != null) {
            uploadDir += "/" + id;
        }

        logger.debug("Creando directorio de subida: {}", uploadDir);
        Files.createDirectories(Paths.get(uploadDir));

        // Generar nombre único para el archivo
        String extension = getFileExtension(file.getOriginalFilename());
        String newFilename = prefix + "_" + UUID.randomUUID() + extension;
        Path newFilePath = Paths.get(uploadDir, newFilename);
        
        logger.debug("Guardando archivo: {}", newFilePath);
        Files.copy(file.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

        String imageUrl = "/images/" + baseDir + (id != null ? "/" + id : "") + "/" + newFilename;
        logger.info("Imagen subida exitosamente - URL: {}", imageUrl);
        return imageUrl;
    }

    public void deleteImage(String imageUrl, String baseDir, Long id) throws IOException {
        logger.info("Iniciando eliminación de imagen - URL: {}, Directorio base: {}, ID: {}", imageUrl, baseDir, id);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            logger.warn("URL de imagen vacía o nula");
            return;
        }

        String expectedPrefix = "/images/" + baseDir + (id != null ? "/" + id : "") + "/";
        if (!imageUrl.startsWith(expectedPrefix)) {
            logger.error("URL de imagen inválida: {}", imageUrl);
            throw new InvalidRequestException("URL de imagen inválida: " + imageUrl);
        }

        String uploadDir = "uploads/images/" + baseDir;
        if (id != null) {
            uploadDir += "/" + id;
        }
        String filename = imageUrl.substring(expectedPrefix.length());
        Path filePath = Paths.get(uploadDir, filename);

        logger.debug("Eliminando archivo: {}", filePath);
        Files.deleteIfExists(filePath);
        logger.info("Imagen eliminada exitosamente");
    }

    private void validateFile(MultipartFile file) {
        logger.debug("Validando archivo: {}", file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            logger.error("Archivo vacío o nulo");
            throw new InvalidRequestException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.error("Tipo de contenido inválido: {}", contentType);
            throw new InvalidRequestException("Solo se permiten archivos de imagen");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            logger.error("Nombre de archivo nulo");
            throw new InvalidRequestException("Nombre de archivo inválido");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            logger.error("Extensión no permitida: {}", extension);
            throw new InvalidRequestException("Extensión no permitida. Se permiten solo .jpg, .jpeg, .png y .gif");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            logger.error("Archivo excede tamaño máximo - Tamaño: {} bytes", file.getSize());
            throw new InvalidRequestException("El archivo excede el tamaño máximo permitido de 5MB");
        }

        logger.debug("Archivo validado exitosamente");
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    public void deleteDirectoryAndImage(String baseDir, Long id) throws IOException {
        logger.info("Iniciando eliminación de directorio y sus contenidos - Directorio base: {}, ID: {}", baseDir, id);

        if (baseDir == null || id == null) {
            logger.warn("Parámetros inválidos - baseDir: {}, id: {}", baseDir, id);
            return;
        }

        File dir = new File("uploads/images/", baseDir + "/" + id);

        if (!dir.exists()) {
            logger.warn("El directorio no existe: {}", dir.getPath());
            return;
        }

        try {
            logger.debug("Eliminando directorio: {}", dir.getPath());
            FileUtils.deleteDirectory(dir);
            logger.info("Directorio eliminado exitosamente");
        } catch (IOException e) {
            logger.error("Error al eliminar el directorio: {}", e.getMessage());
            throw new IOException("Error al eliminar el directorio", e);
        }
    }
}