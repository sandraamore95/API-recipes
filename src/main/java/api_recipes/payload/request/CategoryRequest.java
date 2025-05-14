package api_recipes.payload.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "El nombre de la categoria es obligatorio")
    private String name;
}
