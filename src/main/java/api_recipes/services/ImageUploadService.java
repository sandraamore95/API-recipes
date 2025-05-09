package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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
        validateFile(file);

        // Crear directorio si no existe ->   (hay recetas creadas sin carpeta de imagen )
        String uploadDir = "uploads/images/" + baseDir;
        if (id != null) {
            uploadDir += "/" + id;
        }

        Files.createDirectories(Paths.get(uploadDir));

        // Generar nombre único para el archivo
        String extension = getFileExtension(file.getOriginalFilename());
        String newFilename = prefix + "_" + UUID.randomUUID() + extension;
        Path newFilePath = Paths.get(uploadDir, newFilename);
        // Guardar el archivo
        Files.copy(file.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la URL de la imagen
        return "/images/" + baseDir + (id != null ? "/" + id : "") + "/" + newFilename;
    }

    public void deleteImage(String imageUrl, String baseDir, Long id) throws IOException {

        if (imageUrl == null || imageUrl.trim().isEmpty()) return;

        // Evitar rutas mal formadas..
        String expectedPrefix = "/images/" + baseDir + (id != null ? "/" + id : "") + "/";
        if (!imageUrl.startsWith(expectedPrefix)) {
            throw new InvalidRequestException("URL de imagen inválida: " + imageUrl);
        }

        String uploadDir = "uploads/images/" + baseDir;
        if (id != null) {
            uploadDir += "/" + id;
        }
        String filename = imageUrl.substring(expectedPrefix.length());
        Path filePath = Paths.get(uploadDir, filename);

        Files.deleteIfExists(filePath);
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidRequestException("Solo se permiten archivos de imagen");
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

    public void deleteDirectoryAndImage(String baseDir, Long id) throws IOException {
        if (baseDir == null || id == null) return;

        File dir = new File("uploads/images/", baseDir + "/" + id);

        if (!dir.exists()) return;

        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw new IOException("Error al eliminar el directorio", e);
        }
    }
}