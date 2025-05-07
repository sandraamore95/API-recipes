package api_recipes.payload.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeIngredientRequest {

    @NotNull(message = "El ID del ingrediente es obligatorio")
    private Long ingredientId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Double quantity;
}