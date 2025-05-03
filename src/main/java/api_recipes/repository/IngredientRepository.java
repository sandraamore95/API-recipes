package api_recipes.repository;
import api_recipes.models.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngredientRepository  extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

}