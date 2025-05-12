package api_recipes.models;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "recipe_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"recipe_id", "ingredient_id"})
        })
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient   ingredient;

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