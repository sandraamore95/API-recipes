package api_recipes.controllers;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.User;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.payload.response.SuccessResponse;
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

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;

    public RecipeController(RecipeService recipeService, UserRepository userRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllRecipes(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        try {
            Page<RecipeDto> recipes = recipeService.getAllRecipes(pageable);
            return ResponseEntity.ok(recipes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error al obtener las recetas."));
        }
    }


    //  Obtener receta por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        try {
            recipeService.incrementPopularityRecipe(id);
            RecipeDto recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        }
    }

    //  Obtener receta por título
    @GetMapping("/title/{title}")
    public ResponseEntity<?> getRecipeByTitle(@PathVariable String title) {
        try {
            RecipeDto recipe = recipeService.getRecipeByTitle(title);
            return ResponseEntity.status(HttpStatus.OK).body(recipe);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error inesperado"));
        }
    }


    //delete receta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            recipeService.deleteRecipe(id, user);
            return ResponseEntity.ok(new SuccessResponse("Receta eliminada correctamente"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("ACCESS_DENIED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error inesperado"));
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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("ACCESS_DENIED", e.getMessage()));
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));

        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error inesperado"));
        }
    }
}

