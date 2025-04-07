package api_recipes.services;
import api_recipes.mapper.RecipeMapper;
import api_recipes.models.*;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeIngredientRequest;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.repository.CategoryRepository;
import api_recipes.repository.IngredientRepository;
import api_recipes.repository.RecipeRepository;
import api_recipes.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
        this.userRepository=userRepository;
        this.categoryRepository=categoryRepository;
        this.ingredientRepository=ingredientRepository;
    }

    public List<RecipeDto> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(recipeMapper::toDTO)
                .toList();
    }

    public Optional<RecipeDto> getRecipeById(Long id) {
        return recipeRepository.findById(id).map(recipeMapper::toDTO);
    }
    public Optional<RecipeDto> getRecipeByTitle(String title) {
        return recipeRepository.findByTitle(title)
                .map(recipeMapper::toDTO);
    }

   //crear receta
    public RecipeDto createRecipe(@Valid RecipeRequest recipeRequest, String username) {

        // 1️⃣ Verificar si la receta ya existe
        if (recipeRepository.findByTitle(recipeRequest.getTitle()).isPresent()) {
            throw new RuntimeException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        // 2️⃣ Obtener el usuario creador de la receta
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        Set<Long> uniqueIds = new HashSet<>();
        for (RecipeIngredientRequest req : recipeRequest.getIngredients()) {
            if (!uniqueIds.add(req.getIngredientId())) {
                throw new RuntimeException("Ingrediente duplicado con ID " + req.getIngredientId());
            }
        }

        // 3️⃣ Convertir RecipeRequest a Recipe (sin categorías ni ingredientes aún)
        Recipe recipe = recipeMapper.toEntity(recipeRequest);
        recipe.setUser(user); // Asignar usuario a la receta
        recipe.setStatus(Recipe.RecipeStatus.PENDING); // ✅ Establecer estado por defecto

        // 4️⃣ Buscar y asignar las categorías por nombre
        //  convertir set(Categories String )  en objetos Category
        Set<Category> categories = categoryRepository.findByNameIn(recipeRequest.getCategories());
        recipe.setCategories(categories);

        // 5️⃣ Procesar los ingredientes y asignarlos a la receta
        Set<RecipeIngredient> recipeIngredients = recipeRequest.getIngredients().stream().map(req -> {
            Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingrediente con ID " + req.getIngredientId() + " no encontrado"));

            return new RecipeIngredient(null, recipe, ingredient, req.getQuantity());
        }).collect(Collectors.toSet());

        recipe.setRecipeIngredients(recipeIngredients);

        // 6️⃣ Guardar receta en la BD
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 7️⃣ Convertir a RecipeDto y devolver
        return recipeMapper.toDTO(savedRecipe);
    }

}
