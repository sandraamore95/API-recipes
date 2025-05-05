package api_recipes.payload.dto;
import api_recipes.models.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientDto {
    private Long ingredientId;
    private String name;
    private Double quantity;
    private Ingredient.UnitMeasure unit_measure;
    private String imageUrl;

}
