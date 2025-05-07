package api_recipes.payload.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class RecipeRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 100, message = "La descripción debe tener entre 10 y 100 caracteres")
    private String description;

    @NotBlank(message = "El método de preparación es obligatorio")
    @Size(min = 5, max = 100, message = "El método de preparación debe tener entre 5 y 100 caracteres")
    private String preparation;

    private Set<String> categories; // la receta se puede crear sin categorias

    @Valid
    @NotNull(message = "La lista de ingredientes no puede ser nula")
    @NotEmpty(message = "Debe haber al menos un ingrediente")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Set<RecipeIngredientRequest> ingredients;
}
