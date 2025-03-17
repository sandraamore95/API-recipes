package api_recipes.payload.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import api_recipes.models.Recipe.Category;

@Getter @Setter @AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private List<IngredientDto> ingredients;
    private Long userId;
}
