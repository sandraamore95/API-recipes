package api_recipes.payload.dto;
import api_recipes.models.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter @Setter @AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String title;
    private String description;
    private String preparation;
    private Set<String> categories;
    private  Recipe.RecipeStatus status;
    private Set<RecipeIngredientDto> ingredients;
    private String imageUrl;
    private Long userId;
}
    /*
     "ingredients": [
    {
      "ingredientId": 1,
      "quantity": 2
    },
    {
      "ingredientId": 2,
      "quantity": 2
    },
    {
      "ingredientId": 1,
      "quantity": 2   // este no entra porque esta duplicado (SET)
    }
  ] */