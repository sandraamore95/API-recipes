package api_recipes.controllers;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.User;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.repository.UserRepository;
import api_recipes.security.services.UserDetailsImpl;
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
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;

    public RecipeController(RecipeService recipeService,UserRepository userRepository) {
        this.recipeService = recipeService;
        this.userRepository=userRepository;
    }

    //  Obtener todas las recetas
    @GetMapping
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<RecipeDto> recipes = recipeService.getAllRecipes(pageable);
        return recipes.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(recipes);
    }


    //  Obtener receta por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        try {
            recipeService.incrementPopularityRecipe(id);
            RecipeDto recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Receta no encontrada", ex.getMessage()));
        }
    }

    //  Obtener receta por título
    @GetMapping("/title/{title}")
    public ResponseEntity<?> getRecipeByTitle(@PathVariable String title) {
        try {
            RecipeDto recipe = recipeService.getRecipeByTitle(title);
            return ResponseEntity.status(HttpStatus.OK).body(recipe);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", ex.getMessage()));
        }
    }


    // crear receta
    @PostMapping
    public ResponseEntity<?> createRecipe(
            @Valid @RequestBody RecipeRequest recipeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            RecipeDto createdRecipe = recipeService.createRecipe(recipeRequest, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", ex.getMessage()));
        } catch (ResourceAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Receta ya existe", ex.getMessage()));
        } catch (InvalidRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error de validación", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear receta", "Ocurrió un error inesperado"));
        }
    }


    //delete receta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            recipeService.deleteRecipe(id, user);
            return ResponseEntity.ok().body(Map.of("message", "Receta eliminada correctamente"));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Acceso denegado", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar receta", "Ocurrió un error inesperado"));
        }
    }

    //update receta
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @Valid @RequestBody RecipeRequest recipeRequest,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            RecipeDto updatedRecipe = recipeService.updateRecipe(id, recipeRequest, user);
            return ResponseEntity.ok(updatedRecipe);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Acceso denegado", ex.getMessage()));
        } catch (ResourceAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Receta ya existe", ex.getMessage()));

        } catch (InvalidRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error de validación", ex.getMessage()));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar receta", "Ocurrió un error inesperado"));
        }
    }
}

