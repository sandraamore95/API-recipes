package api_recipes.controllers;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.IngredientRequest;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.services.ImageUploadService;
import api_recipes.services.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;
    private final ImageUploadService imageUploadService;

    public IngredientController(IngredientService ingredientService, ImageUploadService imageUploadService) {
        this.ingredientService = ingredientService;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping
    public ResponseEntity<?> searchIngredients(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ingredientService.searchIngredients(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientDto> getIngredientById(@PathVariable Long id) {
        IngredientDto ingredient = ingredientService.getIngredientById(id);
        return ResponseEntity.ok(ingredient);
    }

    @PostMapping
    public ResponseEntity<IngredientDto> createIngredient(@Valid @RequestBody IngredientRequest ingredientRequest) {
        IngredientDto createdIngredient = ingredientService.createIngredient(ingredientRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIngredient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDto> updateIngredient(@PathVariable Long id,
            @Valid @RequestBody IngredientRequest ingredientRequest) {
        IngredientDto updatedIngredient = ingredientService.updateIngredient(id, ingredientRequest);
        return ResponseEntity.ok(updatedIngredient);
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<SuccessResponse> disabledIngredient(@PathVariable Long id) {
            ingredientService.disableIngredient(id);
            return ResponseEntity.ok(new SuccessResponse("Ingrediente desactivado correctamente"));
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<SuccessResponse> enableIngredient(@PathVariable Long id) {
        ingredientService.enableIngredient(id);
        return ResponseEntity.ok(new SuccessResponse("Ingrediente activado correctamente"));

    }

    @PatchMapping("/{id}/upload-image")
    public ResponseEntity<SuccessResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new InvalidRequestException("El archivo está vacío");
        }

        Ingredient ingredient = ingredientService.getIngredientEntityById(id);

    // Subir nueva imagen si es válida
        String imageUrl = imageUploadService.uploadImage(file, "ingredients", "ingredient", null);
        ingredientService.updateIngredientImage(id, imageUrl);

        // Eliminar imagen anterior si existe
        if (ingredient.getImageUrl() != null) {
            imageUploadService.deleteImage(ingredient.getImageUrl(), "ingredients", null);
        }

        return ResponseEntity.ok(new SuccessResponse("Imagen subida con éxito"));
    }

}
