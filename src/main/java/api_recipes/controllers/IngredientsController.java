package api_recipes.controllers;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.IngredientRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.services.ImageUploadService;
import api_recipes.services.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientsController {


    private final IngredientService ingredientService;
    private final ImageUploadService imageUploadService;

    public IngredientsController(IngredientService ingredientService, ImageUploadService imageUploadService) {
        this.ingredientService = ingredientService;
        this.imageUploadService=imageUploadService;
    }

        @GetMapping
        public ResponseEntity<?> getAllIngredients(
                @RequestParam(required = false) String name) {
        try{
            return ResponseEntity.ok(ingredientService.searchIngredients(name));
        }catch (Exception e) {
                return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error al obtener los ingredientes."));
    }
    }

    @PostMapping
    public ResponseEntity<?> createIngredient(@Valid @RequestBody IngredientRequest ingredientRequest) {
        try {
            IngredientDto createdIngredient = ingredientService.createIngredient(ingredientRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdIngredient);
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Error al crear el ingrediente"));
        }
    }


    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("BAD_REQUEST", "El archivo está vacío"));
            }

            Ingredient ingredient = ingredientService.getIngredientEntityById(id);


            // Eliminar imagen anterior si existe
            if (ingredient.getImageUrl() != null) {
                imageUploadService.deleteImage(ingredient.getImageUrl(), "ingredients", null);
            }

            // Subir nueva imagen
            String imageUrl = imageUploadService.uploadImage(file, "ingredients", "ingredient", null);
            ingredientService.updateIngredientImage(id, imageUrl);

            return ResponseEntity.ok(new SuccessResponse("Imagen subida con éxito"));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Error al subir imagen: " + e.getMessage()));
        }
    }

}

