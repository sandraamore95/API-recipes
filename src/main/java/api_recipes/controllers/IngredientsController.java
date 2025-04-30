package api_recipes.controllers;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.services.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientsController {


    private final IngredientService ingredientService;

    public IngredientsController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
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
    }}

