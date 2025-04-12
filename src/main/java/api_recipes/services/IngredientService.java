package api_recipes.services;
import api_recipes.mapper.IngredientMapper;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository,IngredientMapper ingredientMapper) {
        this.ingredientRepository=ingredientRepository;
        this.ingredientMapper=ingredientMapper;
    }

    public List<IngredientDto> searchIngredients(String searchTerm) {
        List<Ingredient> ingredients;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            ingredients = ingredientRepository.findByNameContainingIgnoreCase(searchTerm);
        } else {
            ingredients = ingredientRepository.findAll();
        }

        return ingredientMapper.toDtoList(ingredients);
    }
}
