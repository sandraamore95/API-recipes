package api_recipes.payload.dto;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class IngredientDto {
    private Long id;
    private String name;
    private String quantity;
}