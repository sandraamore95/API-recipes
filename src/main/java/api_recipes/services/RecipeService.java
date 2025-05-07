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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    public Recipe getRecipeEntityById(Long id) {
        return recipeRepository.findById(id)
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
    @Transactional
    public RecipeDto createRecipe(@Valid RecipeRequest recipeRequest, User user) {

        //  Verificar si la receta ya existe
        if (recipeRepository.findByTitle(recipeRequest.getTitle()).isPresent()) {
            throw new ResourceAlreadyExistsException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        //  Convertir RecipeRequest a Recipe (sin categorías ni ingredientes aún)
        Recipe recipe = recipeMapper.toEntity(recipeRequest);
        recipe.setUser(user);
        recipe.setStatus(Recipe.RecipeStatus.PENDING);

        // Verificar ingredientes duplicados
        validateUniqueIngredients(recipeRequest.getIngredients());

        // Asignar categorías a la receta
        updateRecipeCategories(recipe, recipeRequest.getCategories());

        // Asignar ingredientes a la receta
        updateRecipeIngredients(recipe, recipeRequest.getIngredients());

        // Guardar
        Recipe savedRecipe = recipeRepository.save(recipe);


        return recipeMapper.toDTO(savedRecipe);
    }

    //delete
    @Transactional
    public void deleteRecipe(Long recipeId, User user) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada"));

        if (!recipe.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("No tienes permiso para eliminar esta receta");
        }

        recipeRepository.delete(recipe);
    }


    //update
    @Transactional
    public RecipeDto updateRecipe(Long recipeId, @Valid RecipeRequest recipeRequest, User user) {

        //  Buscar receta existente
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId));

        // Verificar que el usuario sea el creador
        if (!recipe.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("No tienes permiso para editar esta receta");
        }

        // Verificar si la receta ya existe y no es su propia reecta
        Optional<Recipe> existingRecipe = recipeRepository.findByTitle(recipeRequest.getTitle());
        if (existingRecipe.isPresent() && !existingRecipe.get().getId().equals(recipe.getId())) {
            throw new ResourceAlreadyExistsException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        //  Validar ingredientes dupllicados
        validateUniqueIngredients(recipeRequest.getIngredients());

        //  Actualizar campos básicos
        updateInfo(recipe, recipeRequest);

        //  Actualizar categorías
        updateRecipeCategories(recipe, recipeRequest.getCategories());

        // Actualizar ingredientes
        updateRecipeIngredients(recipe, recipeRequest.getIngredients());

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toDTO(updatedRecipe);
    }

    //METODOS

    private void updateInfo(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setPreparation(request.getPreparation());
        recipe.setStatus(Recipe.RecipeStatus.PENDING);
    }

    private void validateUniqueIngredients(Set<RecipeIngredientRequest> ingredients) {
        Set<Long> uniqueIds = new HashSet<>();
        for (RecipeIngredientRequest req : ingredients) {
            if (!uniqueIds.add(req.getIngredientId())) {
                throw new InvalidRequestException("Ingrediente duplicado con ID " + req.getIngredientId());
            }
        }
    }

    private void updateRecipeCategories(Recipe recipe, Set<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return;
        }

        Set<String> requestedNames = new HashSet<>(categoryNames);
        Set<Category> foundCategories = categoryRepository.findByNameIn(requestedNames);

        if (foundCategories.size() != requestedNames.size()) {
            Set<String> foundNames = foundCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());
            requestedNames.removeAll(foundNames);
            throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", requestedNames));
        }
        recipe.setCategories(foundCategories);
    }

    private void updateRecipeIngredients(Recipe recipe, Set<RecipeIngredientRequest> ingredientRequests) {
        Set<RecipeIngredient> existingIngredients = recipe.getRecipeIngredients();
        existingIngredients.clear();

        for (RecipeIngredientRequest req : ingredientRequests) {
            Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ingrediente con ID " + req.getIngredientId() + " no encontrado"));

            RecipeIngredient recipeIngredient = new RecipeIngredient(null, recipe, ingredient, req.getQuantity());
            existingIngredients.add(recipeIngredient);
        }
    }

    @Transactional
    public void updateRecipeImage(Long id, String imageUrl) {
        Recipe recipe = getRecipeEntityById(id);
        recipe.setImageUrl(imageUrl);
        recipeRepository.save(recipe);
    }
}
