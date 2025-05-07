package api_recipes.payload.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class RecipeRequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "El método de preparación es obligatorio")
    private String preparation;

    private Set<String> categories; // la receta se puede crear sin categorias

    @Valid
    @NotNull(message = "La lista de ingredientes no puede ser nula")
    @NotEmpty(message = "Debe haber al menos un ingrediente")
    private Set<RecipeIngredientRequest> ingredients;
}
