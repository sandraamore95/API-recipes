package api_recipes.repository;
import api_recipes.models.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository  extends JpaRepository<Ingredient, Long> {

}