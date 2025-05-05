package api_recipes.services;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.IngredientMapper;
import api_recipes.models.Ingredient;
import api_recipes.models.Recipe;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.IngredientRequest;
import api_recipes.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Ingredient getIngredientEntityById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente con el id '" + id + "' no encontrada"));
    }

    public IngredientDto createIngredient(IngredientRequest ingredientRequest) {
        if (ingredientRepository.existsByNameIgnoreCase(ingredientRequest.getName())) {
            throw new ResourceAlreadyExistsException("El ingrediente ya existe");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientRequest.getName());
        ingredient.setUnit_measure(ingredientRequest.getUnitMeasure());

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(savedIngredient);
    }

    @Transactional
    public void updateIngredientImage(Long id, String imageUrl) {
        Ingredient ingredient = getIngredientEntityById(id);
        ingredient.setImageUrl(imageUrl);
        ingredientRepository.save(ingredient);
    }


    public void save(Ingredient ingredient) {
        ingredientRepository.save(ingredient);
    }
}
