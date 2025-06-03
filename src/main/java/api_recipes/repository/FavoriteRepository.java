package api_recipes.repository;

import api_recipes.models.Favorite;
import api_recipes.models.Recipe;
import api_recipes.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndRecipe(User user, Recipe recipe);

    boolean existsByUserAndRecipe(User user, Recipe recipe);

    List<Favorite> findAllByUser(User user);

    void deleteByUserAndRecipe(User user, Recipe recipe);

    void deleteAllByUserId(Long userId);
}