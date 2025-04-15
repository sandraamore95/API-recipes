package api_recipes.payload.request;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    @NotNull(message = "El ID de receta es obligatorio")
    private Long recipeId;

}