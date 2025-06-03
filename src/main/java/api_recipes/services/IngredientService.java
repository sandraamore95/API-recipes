package api_recipes.services;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.IngredientMapper;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.IngredientRequest;
import api_recipes.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository,IngredientMapper ingredientMapper) {
        this.ingredientRepository=ingredientRepository;
        this.ingredientMapper=ingredientMapper;
    }


    public List<IngredientDto> getAllIngredients() {
        List<Ingredient> ingredients = ingredientRepository.findAll(); 
        return ingredientMapper.toDtoList(ingredients);
    }

    public List<IngredientDto> searchIngredients(String searchTerm) {
        List<Ingredient> ingredients;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            ingredients = ingredientRepository.findByNameContainingIgnoreCase(searchTerm);
        } else {
            ingredients = ingredientRepository.findAll();
        }

        return ingredientMapper.toDtoList(ingredients.stream()
                .filter(Ingredient::isActive)
                .collect(Collectors.toList()));
    }

    public Ingredient getIngredientEntityById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente con el id '" + id + "' no encontrada"));
    }
    public IngredientDto getIngredientById(Long id){
        return ingredientRepository.findById(id)
                .map(ingredientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente con el id '" + id + "' no encontrada"));
    }

    @Transactional
    public IngredientDto createIngredient(IngredientRequest ingredientRequest) {
        if (ingredientRepository.existsByNameIgnoreCase(ingredientRequest.getName().trim())) {
            throw new ResourceAlreadyExistsException("El ingrediente ya existe");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientRequest.getName());
        ingredient.setUnit_measure(ingredientRequest.getUnitMeasure());
        ingredient.setActive(true);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(savedIngredient);
    }

    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientRequest ingredientRequest) {

        // Verificar si existe el ingredinente al que voy a modificar
        Ingredient ingredient = ingredientRepository.findById(id)
                .filter(Ingredient::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));


        // Verificar si ya existe el ingrediente
        String newName = ingredientRequest.getName().trim();
        if (!ingredient.getName().equalsIgnoreCase(newName) &&
                ingredientRepository.existsByNameIgnoreCase(newName)) {
            throw new ResourceAlreadyExistsException("El ingrediente " + newName + " ya existe");
        }

        // Update campos
        ingredient.setName(newName);
        ingredient.setUnit_measure(ingredientRequest.getUnitMeasure());

        ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(ingredient);
    }

    
    public void disableIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));

        // Si ya esta activado no se hace nada
        if (!ingredient.isActive()) {
            return;
        }

        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
    }

    
    public void enableIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));

        if (!ingredient.isActive()) {
            ingredient.setActive(true);
            ingredientRepository.save(ingredient);
        }
    }

    @Transactional
    public void updateIngredientImage(Long id, String imageUrl) {
        Ingredient ingredient = getIngredientEntityById(id);
        ingredient.setImageUrl(imageUrl);
        ingredientRepository.save(ingredient);
    }


}
