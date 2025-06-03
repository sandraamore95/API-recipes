package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.RecipeMapper;
import api_recipes.models.*;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeIngredientRequest;
import api_recipes.payload.request.RecipeRequest;
import api_recipes.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository,  RecipeMapper recipeMapper, CategoryRepository categoryRepository,
                         IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
    }


    public Page<RecipeDto> getAllRecipes(Pageable pageable) {
        logger.info("Obteniendo todas las recetas paginadas");
        Page<Recipe> recipePage = recipeRepository.findAllWithRelationships(pageable);
        return recipePage.map(recipe -> recipeMapper.toDTO(recipe));
    }

    public RecipeDto getRecipeById(Long id) {
        logger.info("Buscando receta por ID: {}", id);
        return recipeRepository.findById(id)
                .map(recipeMapper::toDTO)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada con ID: {}", id);
                    return new ResourceNotFoundException("Receta con el id '" + id + "' no encontrada");
                });
    }

    public Recipe getRecipeEntityById(Long id) {
        logger.info("Buscando entidad de receta por ID: {}", id);
        return recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Entidad de receta no encontrada con ID: {}", id);
                    return new ResourceNotFoundException("Receta con el id '" + id + "' no encontrada");
                });
    }


    public RecipeDto getRecipeByTitle(String title) {
        logger.info("Buscando receta por título: {}", title);
        return recipeRepository.findByTitle(title)
                .map(recipeMapper::toDTO)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada con título: {}", title);
                    return new ResourceNotFoundException("Receta con el título '" + title + "' no encontrada");
                });
    }

    //crear receta
    @Transactional
    public RecipeDto createRecipe( RecipeRequest recipeRequest, User user) {
        logger.info("Iniciando creación de nueva receta: {} por usuario: {}", recipeRequest.getTitle(), user.getUsername());

        //  Verificar si la receta ya existe
        if (recipeRepository.findByTitle(recipeRequest.getTitle()).isPresent()) {
            logger.warn("Intento de crear receta con título duplicado: {}", recipeRequest.getTitle());
            throw new ResourceAlreadyExistsException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        //  Convertir RecipeRequest a Recipe (sin categorías ni ingredientes aún)
        Recipe recipe = recipeMapper.toEntity(recipeRequest);
        recipe.setUser(user);
        recipe.setStatus(Recipe.RecipeStatus.PENDING);

        // Verificar ingredientes duplicados
        validateUniqueIngredients(recipeRequest.getIngredients());
        logger.debug("Ingredientes validados para la receta: {}", recipeRequest.getTitle());

        // Asignar categorías a la receta
        updateRecipeCategories(recipe, recipeRequest.getCategories());
        logger.debug("Categorías actualizadas para la receta: {}", recipeRequest.getTitle());

        // Asignar ingredientes a la receta
        updateRecipeIngredients(recipe, recipeRequest.getIngredients());
        logger.debug("Ingredientes actualizados para la receta: {}", recipeRequest.getTitle());

        // Guardar
        Recipe savedRecipe = recipeRepository.save(recipe);
        logger.info("Receta creada exitosamente con ID: {}", savedRecipe.getId());


        return recipeMapper.toDTO(savedRecipe);
    }

    //delete
    @Transactional
    public void deleteRecipe(Long recipeId, User user) {
        logger.info("Iniciando eliminación de receta ID: {} por usuario: {}", recipeId, user.getUsername());
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada para eliminación - ID: {}", recipeId);
                    return new ResourceNotFoundException("Receta no encontrada");
                });

        if (!recipe.getUser().getId().equals(user.getId())) {
            logger.warn("Intento de eliminación no autorizado - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
            throw new AccessDeniedException("No tienes permiso para eliminar esta receta");
        }

        recipeRepository.delete(recipe);
        logger.info("Receta eliminada exitosamente - ID: {}", recipeId);
    }


    //update
    @Transactional
    public RecipeDto updateRecipe(Long recipeId, RecipeRequest recipeRequest, User user) {
        logger.info("Iniciando actualización de receta ID: {} por usuario: {}", recipeId, user.getUsername());

        //  Buscar receta existente
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada para actualización - ID: {}", recipeId);
                    return new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId);
                });

        // Verificar que el usuario sea el creador
        if (!recipe.getUser().getUsername().equals(user.getUsername())) {
            logger.warn("Intento de actualización no autorizado - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
            throw new AccessDeniedException("No tienes permiso para editar esta receta");
        }

        // Verificar si la receta ya existe y no es su propia reecta
        Optional<Recipe> existingRecipe = recipeRepository.findByTitle(recipeRequest.getTitle());
        if (existingRecipe.isPresent() && !existingRecipe.get().getId().equals(recipe.getId())) {
            logger.warn("Intento de actualización con título duplicado: {}", recipeRequest.getTitle());
            throw new ResourceAlreadyExistsException("La receta con el título '" + recipeRequest.getTitle() + "' ya existe.");
        }

        //  Validar ingredientes dupllicados
        validateUniqueIngredients(recipeRequest.getIngredients());
        logger.debug("Ingredientes validados para actualización - Receta ID: {}", recipeId);

        //  Actualizar campos básicos
        updateInfo(recipe, recipeRequest);

        //  Actualizar categorías
        updateRecipeCategories(recipe, recipeRequest.getCategories());

        // Actualizar ingredientes
        updateRecipeIngredients(recipe, recipeRequest.getIngredients());

        Recipe updatedRecipe = recipeRepository.save(recipe);
        logger.info("Receta actualizada exitosamente - ID: {}", updatedRecipe.getId());
        return recipeMapper.toDTO(updatedRecipe);
    }

    //METODOS

    private void updateInfo(Recipe recipe, RecipeRequest request) {
        logger.debug("Actualizando información básica de la receta - ID: {}", recipe.getId());
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setPreparation(request.getPreparation());
        recipe.setStatus(Recipe.RecipeStatus.PENDING);
    }

    private void validateUniqueIngredients(Set<RecipeIngredientRequest> ingredients) {
        logger.debug("Validando ingredientes únicos");
        Set<Long> uniqueIds = new HashSet<>();
        for (RecipeIngredientRequest req : ingredients) {
            System.out.println(req.getIngredientId());
            if (!uniqueIds.add(req.getIngredientId())) {
                logger.error("Ingrediente duplicado encontrado - ID: {}", req.getIngredientId());
                throw new InvalidRequestException("Ingrediente duplicado con ID " + req.getIngredientId());
            }
        }
    }


    private void updateRecipeIngredients(Recipe recipe, Set<RecipeIngredientRequest> ingredientRequests) {
        logger.debug("Actualizando ingredientes de la receta - ID: {}", recipe.getId());
        Set<RecipeIngredient> existingIngredients = recipe.getRecipeIngredients();

        // Mapa para buscar rápidamente por ingredientId
        Map<Long, RecipeIngredientRequest> requestMap = ingredientRequests.stream()
                .collect(Collectors.toMap(RecipeIngredientRequest::getIngredientId, req -> req));

        // Eliminar los que ya no están en el request
        existingIngredients.removeIf(ri -> !requestMap.containsKey(ri.getIngredient().getId()));

        // Actualizar o agregar nuevos
        for (RecipeIngredientRequest req : ingredientRequests) {
            Long ingredientId = req.getIngredientId();
            Optional<RecipeIngredient> existing = existingIngredients.stream()
                    .filter(ri -> ri.getIngredient().getId().equals(ingredientId))
                    .findFirst();

            if (existing.isPresent()) {
                // Si ya existe, actualizamos la cantidad
                existing.get().setQuantity(req.getQuantity());
            } else {
                // Si no existe, lo agregamos
                Ingredient ingredient = ingredientRepository.findById(ingredientId)
                        .orElseThrow(() -> {
                            logger.error("Ingrediente no encontrado - ID: {}", ingredientId);
                            return new ResourceNotFoundException(
                                    "Ingrediente con ID " + ingredientId + " no encontrado");
                        });

                RecipeIngredient newRI = new RecipeIngredient(null, recipe, ingredient, req.getQuantity());
                existingIngredients.add(newRI);
            }
        }
    }

    private void updateRecipeCategories(Recipe recipe,  Set<String> categories){
        logger.debug("Actualizando categorías de la receta - ID: {}", recipe.getId());

        if (categories == null || categories.isEmpty()) {
            recipe.setCategories(Collections.emptySet());
            return;
        }

        Set<Category> existingCategories = categoryRepository.findByNameIn(categories);
        if (existingCategories.size() != categories.size()) {
            Set<String> foundNames = existingCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());

            categories.removeAll(foundNames);
            logger.error("Categorías no encontradas: {}", categories);
            throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", categories));
        }
        recipe.setCategories(existingCategories);
    }

    @Transactional
    public void updateRecipeImage(Long id, String imageUrl) {
        logger.info("Actualizando imagen de receta - ID: {}", id);
        Recipe recipe = getRecipeEntityById(id);
        recipe.setImageUrl(imageUrl);
        recipeRepository.save(recipe);
        logger.info("Imagen de receta actualizada exitosamente - ID: {}", id);
    }


    public List<RecipeDto> getRecipesByUserId(Long userId) {
  
       // Verificar si el usuario tiene recetas
       if (recipeRepository.findByUserId(userId).isEmpty()) {
        throw new ResourceNotFoundException("El usuario con ID: " + userId + " no tiene recetas");
       }

       // Obtener todas las recetas del usuario
       List<Recipe> recipes = recipeRepository.findByUserId(userId);

       return recipes.stream().map(recipeMapper::toDTO).collect(Collectors.toList());
    }
}
