package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
    ));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif"
    ));

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadImage(MultipartFile file, String baseDir, String prefix, Long id) throws IOException {
        validateFile(file);

        // Crear directorio si no existe
        String uploadDir = "src/main/resources/static/images/" + baseDir;
        if (id != null) {
            uploadDir += "/" + id;
        }
        Files.createDirectories(Paths.get(uploadDir));

        // Generar nombre único para el archivo
        String extension = getFileExtension(file.getOriginalFilename());
        String newFilename = prefix + "_" + UUID.randomUUID().toString() + extension;
        Path newFilePath = Paths.get(uploadDir, newFilename);

        // Guardar el archivo
        Files.copy(file.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la URL de la imagen
        return "/images/" + baseDir + (id != null ? "/" + id : "") + "/" + newFilename;
    }

    public void deleteImage(String imageUrl, String baseDir, Long id) throws IOException {
        if (imageUrl != null) {
            String uploadDir = "src/main/resources/static/images/" + baseDir;
            if (id != null) {
                uploadDir += "/" + id;
            }
            String filename = imageUrl.replace("/images/" + baseDir + (id != null ? "/" + id : "") + "/", "");
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidRequestException("Tipo de archivo no permitido. Se permiten solo imágenes JPG, PNG y GIF");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidRequestException("Nombre de archivo inválido");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidRequestException("Extensión no permitida. Se permiten solo .jpg, .jpeg, .png y .gif");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("El archivo excede el tamaño máximo permitido de 5MB");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}