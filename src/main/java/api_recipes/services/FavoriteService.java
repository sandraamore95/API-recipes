package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.FavoriteMapper;
import api_recipes.models.Favorite;
import api_recipes.models.Recipe;
import api_recipes.models.User;
import api_recipes.payload.dto.FavoriteDto;
import api_recipes.repository.FavoriteRepository;
import api_recipes.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    private final FavoriteRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           RecipeRepository recipeRepository,FavoriteMapper favoriteMapper) {
        this.favoriteRepository = favoriteRepository;
        this.recipeRepository = recipeRepository;
        this.favoriteMapper=favoriteMapper;
    }

    public List<FavoriteDto> getUserFavorites(User user) {
        logger.info("Obteniendo favoritos del usuario: {}", user.getUsername());
        List<Favorite> favorites = favoriteRepository.findAllByUser(user);
        logger.debug("Se encontraron {} favoritos para el usuario: {}", favorites.size(), user.getUsername());
        return favoriteMapper.toDtoList(favorites);
    }

    @Transactional
    public FavoriteDto addFavorite(User user, Long recipeId) {
        logger.info("Agregando receta a favoritos - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada para agregar a favoritos - ID: {}", recipeId);
                    return new ResourceNotFoundException("Receta no encontrada");
                });

        if (favoriteRepository.existsByUserAndRecipe(user, recipe)) {
            logger.warn("Intento de agregar receta duplicada a favoritos - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
            throw new ResourceAlreadyExistsException("La receta ya está en favoritos");
        }

        if (recipe.getUser().equals(user)) {
            throw new InvalidRequestException("No puedes agregar tus propias recetas a favoritos");
        }

        // Aumentar la popularidad de la receta cada vez que se marca favorita
        recipe.increasePopularity();
        recipeRepository.save(recipe);

        Favorite favorite = new Favorite(user, recipe);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        logger.info("Receta agregada a favoritos exitosamente - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
        return favoriteMapper.toDto(savedFavorite);
    }

    @Transactional
    public void removeFavorite(User user, Long recipeId) {
        logger.info("Eliminando receta de favoritos - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    logger.error("Receta no encontrada para eliminar de favoritos - ID: {}", recipeId);
                    return new ResourceNotFoundException("Receta no encontrada");
                });

        Favorite favorite = favoriteRepository.findByUserAndRecipe(user, recipe)
                .orElseThrow(() -> {
                    logger.error("Favorito no encontrado - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
                    return new ResourceNotFoundException("La receta no está en favoritos");
                });

        favoriteRepository.delete(favorite);
        logger.info("Receta eliminada de favoritos exitosamente - Usuario: {}, Receta ID: {}", user.getUsername(), recipeId);
    }

    public boolean existsByUserAndRecipe(User user, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId));
        return favoriteRepository.existsByUserAndRecipe(user, recipe);
    }


}