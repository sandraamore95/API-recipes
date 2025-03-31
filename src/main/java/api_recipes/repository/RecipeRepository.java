package api_recipes.repository;
import api_recipes.models.Recipe;
import api_recipes.payload.dto.RecipeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository  extends JpaRepository <Recipe, Long>{

    Optional<Recipe> findByTitle(String title);
}
