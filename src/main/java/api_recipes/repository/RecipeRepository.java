package api_recipes.repository;
import api_recipes.models.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RecipeRepository  extends JpaRepository <Recipe, Long>{

    Optional<Recipe> findByTitle(String title);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.categories " +
            "LEFT JOIN FETCH r.recipeIngredients ri " +
            "LEFT JOIN FETCH ri.ingredient")
    Page<Recipe> findAllWithRelationships(Pageable pageable);


}
