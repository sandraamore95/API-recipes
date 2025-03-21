package api_recipes.payload.dto;
import api_recipes.models.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

import api_recipes.models.Recipe.Category;

@Getter @Setter @AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String title;
    private String description;
    private Set<Category> categories;
    private List<IngredientDto> ingredients;
    private Long userId;
}
