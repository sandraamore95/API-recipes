package api_recipes.controllers;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.Recipe;
import api_recipes.models.User;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.repository.UserRepository;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.ImageUploadService;
import api_recipes.services.RecipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    public RecipeController(RecipeService recipeService, UserRepository userRepository,
            ImageUploadService imageUploadService) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
    }

    private User getAuthenticatedUser(UserDetailsImpl userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        Page<RecipeDto> recipes = recipeService.getAllRecipes(pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) {
        RecipeDto recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<RecipeDto> getRecipeByTitle(@PathVariable String title) {
        RecipeDto recipe = recipeService.getRecipeByTitle(title);
        return ResponseEntity.ok(recipe);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(
            @Valid @RequestBody RecipeRequest recipeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        RecipeDto createdRecipe = recipeService.createRecipe(recipeRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest recipeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        RecipeDto updatedRecipe = recipeService.updateRecipe(id, recipeRequest, user);
        return ResponseEntity.ok(updatedRecipe);
    }

    @PatchMapping("/{id}/upload-image")
    public ResponseEntity<SuccessResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        User user = getAuthenticatedUser(userDetails);
        Recipe recipe = recipeService.getRecipeEntityById(id);

        // Si la receta Si existe pero no le pertenece al usuario
        if (!recipe.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("No tienes permiso para modificar esta receta");
        }
        if (file.isEmpty()) {
            throw new InvalidRequestException("La imagen está vacía");
        }

        // Validar antes de subir la imagen
        String imageUrl = imageUploadService.uploadImage(file, "recipes", "recipe", id);

        // Borrar imagen anterior si existe
        if (recipe.getImageUrl() != null) {
            imageUploadService.deleteImage(recipe.getImageUrl(), "recipes", id);
        }

        recipeService.updateRecipeImage(id, imageUrl);
        return ResponseEntity.ok(new SuccessResponse("Imagen subida con éxito"));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        User user = getAuthenticatedUser(userDetails);
        Recipe recipe = recipeService.getRecipeEntityById(id);

        if (recipe.getImageUrl() != null) {
            imageUploadService.deleteDirectoryAndImage("recipes", id);
        }

        recipeService.deleteRecipe(id, user);
        return ResponseEntity.ok(new SuccessResponse("Receta eliminada correctamente"));
    }
}
