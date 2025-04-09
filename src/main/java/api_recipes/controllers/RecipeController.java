package api_recipes.controllers;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.services.RecipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
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
            RecipeDto recipe = recipeService.getRecipeById(id);
            return ResponseEntity.status(HttpStatus.OK).body(recipe);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", ex.getMessage()));
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
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        try {
            RecipeDto createdRecipe = recipeService.createRecipe(recipeRequest, username);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear receta", "Ocurrió un error inesperado"));
        }
    }


}

