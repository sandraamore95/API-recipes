package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.IngredientMapper;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.IngredientRequest;
import api_recipes.repository.IngredientRepository;
import api_recipes.repository.RecipeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que maneja todas las operaciones relacionadas con ingredientes.
 * Incluye funcionalidades para crear, actualizar, eliminar y consultar ingredientes,
 * así como búsqueda por nombre.
 *
 * @author Sandy
 * @version 1.0
 */
@Service
public class IngredientService {
    private static final Logger logger = LoggerFactory.getLogger(IngredientService.class);
    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final RecipeRepository recipeRepository;

    /**
     * Constructor del servicio de ingredientes.
     *
     * @param ingredientRepository Repositorio de ingredientes
     * @param ingredientMapper Mapper para convertir entre entidades y DTOs
     */
    public IngredientService(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper, RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
        this.recipeRepository = recipeRepository;
    }

    /**
     * Obtiene todos los ingredientes del sistema.
     *
     * @return Lista de ingredientes convertidos a DTOs
     */
    public List<IngredientDto> getAllIngredients() {
        logger.info("Obteniendo todos los ingredientes");
        List<Ingredient> ingredients = ingredientRepository.findAll();
        logger.debug("Se encontraron {} ingredientes", ingredients.size());
        return ingredientMapper.toDtoList(ingredients);
    }

    /**
     * Busca ingredientes por nombre.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de ingredientes que coinciden con el término de búsqueda
     */
    public List<IngredientDto> searchIngredients(String searchTerm) {
        logger.info("Buscando ingredientes con término: {}", searchTerm);
        
        List<Ingredient> ingredients;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            ingredients = ingredientRepository.findByNameContainingIgnoreCase(searchTerm);
            logger.debug("Se encontraron {} ingredientes que coinciden con el término de búsqueda", ingredients.size());
        } else {
            ingredients = ingredientRepository.findAll();
            logger.debug("Se obtuvieron todos los ingredientes (sin término de búsqueda)");
        }

        List<IngredientDto> activeIngredients = ingredientMapper.toDtoList(ingredients.stream()
                .filter(Ingredient::isActive)
                .collect(Collectors.toList()));
        
        logger.debug("Se filtraron {} ingredientes activos", activeIngredients.size());
        return activeIngredients;
    }

    /**
     * Obtiene una entidad de ingrediente por su ID.
     *
     * @param id ID del ingrediente a buscar
     * @return Entidad de ingrediente
     * @throws ResourceNotFoundException si el ingrediente no existe
     */
    public Ingredient getIngredientEntityById(Long id) {
        logger.info("Buscando entidad de ingrediente por ID: {}", id);
        return ingredientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Ingrediente no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Ingrediente con el id '" + id + "' no encontrada");
                });
    }

    /**
     * Obtiene un ingrediente por su ID.
     *
     * @param id ID del ingrediente a buscar
     * @return Ingrediente convertido a DTO
     * @throws ResourceNotFoundException si el ingrediente no existe
     */
    public IngredientDto getIngredientById(Long id) {
        logger.info("Buscando ingrediente por ID: {}", id);
        return ingredientRepository.findById(id)
                .map(ingredientMapper::toDto)
                .orElseThrow(() -> {
                    logger.error("Ingrediente no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id);
                });
    }

    /**
     * Crea un nuevo ingrediente.
     *
     * @param ingredientRequest Datos del ingrediente a crear
     * @return Ingrediente creado convertido a DTO
     * @throws InvalidRequestException si el nombre está vacío
     * @throws ResourceAlreadyExistsException si ya existe un ingrediente con el mismo nombre
     */
    @Transactional
    public IngredientDto createIngredient(IngredientRequest ingredientRequest) {
        logger.info("Iniciando creación de nuevo ingrediente: {}", ingredientRequest.getName());
        
        
        if (ingredientRepository.existsByNameIgnoreCase(ingredientRequest.getName().trim())) {
            logger.warn("Intento de crear ingrediente duplicado: {}", ingredientRequest.getName());
            throw new ResourceAlreadyExistsException("El ingrediente " + ingredientRequest.getName() + " ya existe");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientRequest.getName().trim().toUpperCase());
        ingredient.setUnit_measure(ingredientRequest.getUnitMeasure());
        ingredient.setActive(true);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        logger.info("Ingrediente creado exitosamente con ID: {}", savedIngredient.getId());
        return ingredientMapper.toDto(savedIngredient);
    }

    /**
     * Actualiza un ingrediente existente.
     *
     * @param id ID del ingrediente a actualizar
     * @param ingredientRequest Datos del ingrediente actualizado
     * @return Ingrediente actualizado convertido a DTO
     * @throws InvalidRequestException si el nombre está vacío
     * @throws ResourceNotFoundException si el ingrediente no existe
     * @throws ResourceAlreadyExistsException si ya existe otro ingrediente con el nuevo nombre
     */
    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientRequest ingredientRequest) {
        logger.info("Iniciando actualización de ingrediente ID: {}", id);

        Ingredient ingredient = ingredientRepository.findById(id)
                .filter(Ingredient::isActive)
                .orElseThrow(() -> {
                    logger.error("Ingrediente no encontrado o inactivo - ID: {}", id);
                    return new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id);
                });

        String newName = ingredientRequest.getName().trim().toUpperCase();
        if (!ingredient.getName().equalsIgnoreCase(newName) &&
                ingredientRepository.existsByNameIgnoreCase(newName)) {
            logger.warn("Intento de actualización con nombre duplicado: {}", newName);
            throw new ResourceAlreadyExistsException("El ingrediente " + newName + " ya existe");
        }

        ingredient.setName(newName);
        ingredient.setUnit_measure(ingredientRequest.getUnitMeasure());

        ingredientRepository.save(ingredient);
        logger.info("Ingrediente actualizado exitosamente - ID: {}", id);
        return ingredientMapper.toDto(ingredient);
    }

    
    public void disableIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));

        // Si una receta tiene este ingrediente, no se puede desactivar
        if (!ingredient.isActive() && recipeRepository.existsByIngredientsId(id)) {
            throw new InvalidRequestException("No se puede desactivar un ingrediente en uso");
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
