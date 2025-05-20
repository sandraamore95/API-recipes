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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
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
        List<Favorite> favorites = favoriteRepository.findAllByUser(user);
        return favoriteMapper.toDtoList(favorites);
    }

    @Transactional
    public FavoriteDto addFavorite(User user, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId));

        //verificamos que la receta no este ya en favoritos
        if (favoriteRepository.existsByUserAndRecipe(user, recipe)) {
            throw new ResourceAlreadyExistsException("La receta ya está en tus favoritos");
        }

        if (recipe.getUser().equals(user)) {
            throw new InvalidRequestException("No puedes agregar tus propias recetas a favoritos");
        }

        // Aumentar la popularidad de la receta cada vez que se marca favorita
        recipe.increasePopularity();
        recipeRepository.save(recipe);

        Favorite favorite = new Favorite(user, recipe);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        return favoriteMapper.toDto(savedFavorite);
    }

    @Transactional
    public void removeFavorite(User user, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId));

        Favorite favorite = favoriteRepository.findByUserAndRecipe(user, recipe)
                .orElseThrow(() -> new ResourceNotFoundException("La receta no estaba en tus favoritos"));

        favoriteRepository.delete(favorite);
    }

    public boolean existsByUserAndRecipe(User user, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta no encontrada con ID: " + recipeId));
        return favoriteRepository.existsByUserAndRecipe(user, recipe);
    }


}