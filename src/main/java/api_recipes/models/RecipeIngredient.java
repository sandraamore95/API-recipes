package api_recipes.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

/**
 * Entidad que representa la relación entre una receta y sus ingredientes.
 * Esta clase almacena la cantidad de cada ingrediente necesaria para una receta.
 *
 * @author Sandy
 * @version 1.0
 */
@Entity
@Table(name = "recipe_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"recipe_id", "ingredient_id"})
        })
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class RecipeIngredient {
    /**
     * Identificador único de la relación receta-ingrediente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Receta a la que pertenece este ingrediente.
     * Relación muchos a uno con la entidad Recipe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @NotNull
    private Recipe recipe;

    /**
     * Ingrediente asociado a la receta.
     * Relación muchos a uno con la entidad Ingredient.
     */
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient   ingredient;

    /**
     * Cantidad del ingrediente necesaria para la receta.
     * Debe ser un valor positivo.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Double quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredient that)) return false;
        return Objects.equals(recipe, that.recipe) &&
                Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, ingredient);
    }

}