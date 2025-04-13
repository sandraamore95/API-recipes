package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.RecipeMapper;
import api_recipes.models.*;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeIngredientRequest;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.repository.CategoryRepository;
import api_recipes.repository.IngredientRepository;
import api_recipes.repository.RecipeRepository;
import api_recipes.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, RecipeMapper recipeMapper, CategoryRepository categoryRepository,
                         IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
    }


    public Page<RecipeDto> getAllRecipes(Pageable pageable) {
        Page<Recipe> recipePage = recipeRepository.findAllWithRelationships(pageable);
        return recipePage.map(recipe -> recipeMapper.toDTO(recipe));
    }

    public RecipeDto getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .map(recipeMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Receta con el id '" + id + "' no encontrada"));
    }

    @Transactional
    public RecipeDto incrementPopularityRecipe(Long id){
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receta con el id '" + id + "' no encontrada"));

        recipe.setPopularity(recipe.getPopularity() + 1);
        recipeRepository.save(recipe);

        return recipeMapper.toDTO(recipe);
    }

    public RecipeDto getRecipeByTitle(String title) {
        return recipeRepository.findByTitle(title)
                .map(recipeMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Receta con el título '" + title + "' no encontrada"));
    }

    //crear receta
    public RecipeDto createRecipe(@Valid RecipeRequest recipeRequest, String username) {

        //  Verificar si la receta ya existe
        if (recipeRepository.findByTitle(recipeRequest.getTitle()).isPresent()) {
            throw new ResourceAlreadyExistsException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        //  Obtener el usuario creador de la receta
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar ingredientes duplicados
        Set<Long> uniqueIds = new HashSet<>();
        for (RecipeIngredientRequest req : recipeRequest.getIngredients()) {
            if (!uniqueIds.add(req.getIngredientId())) {
                throw new InvalidRequestException("Ingrediente duplicado con ID " + req.getIngredientId());
            }
        }

        //  Convertir RecipeRequest a Recipe (sin categorías ni ingredientes aún)
        Recipe recipe = recipeMapper.toEntity(recipeRequest);
        recipe.setUser(user);
        recipe.setStatus(Recipe.RecipeStatus.PENDING);

        //  Buscar y asignar las categorías por nombre
        Set<String> requestedNames = new HashSet<>(recipeRequest.getCategories());
        Set<Category> foundCategories = categoryRepository.findByNameIn(requestedNames);

        if (foundCategories.size() != requestedNames.size()) {
            Set<String> foundNames = foundCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());
            requestedNames.removeAll(foundNames);
            throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", requestedNames));
        }

        recipe.setCategories(foundCategories);

        //  Procesar los ingredientes y asignarlos a la receta
        Set<RecipeIngredient> recipeIngredients = recipeRequest.getIngredients().stream().map(req -> {
            Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrediente con ID " + req.getIngredientId() + " no encontrado"));

            return new RecipeIngredient(null, recipe, ingredient, req.getQuantity());
        }).collect(Collectors.toSet());

        recipe.setRecipeIngredients(recipeIngredients);

        //  Guardar receta en la BD
        Recipe savedRecipe = recipeRepository.save(recipe);

        //  Convertir a RecipeDto y devolver
        return recipeMapper.toDTO(savedRecipe);
    }

    //delete
    public void deleteRecipe(Long recipeId, String username) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada"));

        if (!recipe.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta receta");
        }

        recipeRepository.delete(recipe);
    }

}
