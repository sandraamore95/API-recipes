package api_recipes.payload.dto;
import api_recipes.models.Ingredient;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class IngredientDto {
    private Long id;
    private String name;
    private Ingredient.UnitMeasure unit_measure;
}