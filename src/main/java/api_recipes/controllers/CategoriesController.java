package api_recipes.controllers;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.payload.dto.CategoryDto;
import api_recipes.payload.request.CategoryRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {
    private final CategoryService categoryService;

    public CategoriesController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryDto> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error al obtener las categorías."));
        }
    }


    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        try {
            CategoryDto createdCategory = categoryService.createCategory(categoryRequest.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Error al crear la categoría"));
        }
    }

}
