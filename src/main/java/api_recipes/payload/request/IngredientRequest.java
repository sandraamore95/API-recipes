package api_recipes.payload.request;
import api_recipes.models.Ingredient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientRequest {

    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    private String name;

    @NotNull(message = "La unidad de medida es obligatoria")
    private Ingredient.UnitMeasure unitMeasure;


}