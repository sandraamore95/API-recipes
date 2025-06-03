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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recetas", description = "APIs para gestionar recetas")
@SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Obtener todas las recetas", description = "Retorna una página de recetas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recetas encontradas", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(
            @Parameter(description = "Configuración de paginación") @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        Page<RecipeDto> recipes = recipeService.getAllRecipes(pageable);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Obtener receta por ID", description = "Retorna una receta específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receta encontrada", content = @Content(schema = @Schema(implementation = RecipeDto.class))),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) {
        RecipeDto recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @Operation(summary = "Obtener receta por título", description = "Retorna una receta específica por su título")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receta encontrada", content = @Content(schema = @Schema(implementation = RecipeDto.class))),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @GetMapping("/title/{title}")
    public ResponseEntity<RecipeDto> getRecipeByTitle(@PathVariable String title) {
        RecipeDto recipe = recipeService.getRecipeByTitle(title);
        return ResponseEntity.ok(recipe);
    }

    @Operation(summary = "Obtener recetas del usuario", description = "Retorna todas las recetas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recetas encontradas", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/user")
    public ResponseEntity<List<RecipeDto>> getRecipesByUserId(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        List<RecipeDto> recipes = recipeService.getRecipesByUserId(user.getId());
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Crear receta", description = "Crea una nueva receta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receta creada exitosamente", content = @Content(schema = @Schema(implementation = RecipeDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una receta con ese título")
    })
    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(
            @Valid @RequestBody RecipeRequest recipeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        RecipeDto createdRecipe = recipeService.createRecipe(recipeRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    @Operation(summary = "Actualizar receta", description = "Actualiza una receta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receta actualizada exitosamente", content = @Content(schema = @Schema(implementation = RecipeDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para editar esta receta"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe otra receta con ese título")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(
            @Parameter(description = "ID de la receta", required = true) @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la receta", required = true) @Valid @RequestBody RecipeRequest recipeRequest,
            @Parameter(description = "Usuario autenticado", hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        RecipeDto updatedRecipe = recipeService.updateRecipe(id, recipeRequest, user);
        return ResponseEntity.ok(updatedRecipe);
    }

    @Operation(summary = "Subir imagen de receta", description = "Sube una imagen para una receta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para modificar esta receta"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @PatchMapping("/{id}/upload-image")
    public ResponseEntity<SuccessResponse> uploadImage(
            @Parameter(description = "ID de la receta", required = true) @PathVariable Long id,
            @Parameter(description = "Archivo de imagen", required = true) @RequestParam("image") MultipartFile file,
            @Parameter(description = "Usuario autenticado", hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails)
            throws IOException {
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

    @Operation(summary = "Eliminar receta", description = "Elimina una receta específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receta eliminada exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar esta receta"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteRecipe(
            @Parameter(description = "ID de la receta", required = true) @PathVariable Long id,
            @Parameter(description = "Usuario autenticado", hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails)
            throws IOException {
        User user = getAuthenticatedUser(userDetails);
        Recipe recipe = recipeService.getRecipeEntityById(id);

        if (recipe.getImageUrl() != null) {
            imageUploadService.deleteDirectoryAndImage("recipes", id);
        }

        recipeService.deleteRecipe(id, user);
        return ResponseEntity.ok(new SuccessResponse("Receta eliminada correctamente"));
    }
}
