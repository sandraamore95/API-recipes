package api_recipes.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Objects;

/**
 * Entidad que representa una receta marcada como favorita por un usuario.
 * Esta clase mantiene la relación entre usuarios y sus recetas favoritas.
 *
 * @author Sandy
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "recipe_id"}))

public class Favorite {

    /**
     * Identificador único de la relación favorita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que marcó la receta como favorita.
     * Relación muchos a uno con la entidad User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Receta marcada como favorita.
     * Relación muchos a uno con la entidad Recipe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public Favorite(User user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
    }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Favorite favorite)) return false;
        return Objects.equals(user, favorite.user) &&
                Objects.equals(recipe, favorite.recipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, recipe);
    }
}
