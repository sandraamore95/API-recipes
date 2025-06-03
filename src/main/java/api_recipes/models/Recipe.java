package api_recipes.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.*;

/**
 * Entidad que representa una receta en el sistema.
 * Esta clase almacena toda la información relacionada con una receta, incluyendo
 * sus ingredientes, categorías y el usuario que la creó.
 *
 * @author Sandy
 * @version 1.0
 */
@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    /**
     * Identificador único de la receta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título de la receta.
     * No puede estar vacío y debe tener entre 3 y 100 caracteres.
     */
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    private String title;

    /**
     * Descripción detallada de la receta.
     * No puede estar vacía y debe tener entre 10 y 1000 caracteres.
     */
    @NotBlank
    @Size(min = 10, max = 1000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String preparation;

    /**
     * URL de la imagen asociada a la receta.
     */
    @Column(name = "image_url")
    private String imageUrl;

    private int popularity = 0;

    public void increasePopularity() {
        this.popularity += 1;
    }

    @Enumerated(EnumType.STRING)
    private RecipeStatus status = RecipeStatus.PENDING; // Estado inicial

    /**
     * Categorías a las que pertenece la receta.
     * Relación muchos a muchos con la entidad Category.
     */
    @ManyToMany
    @JoinTable(
            name = "recipe_categories",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /**
     * Usuario que creó la receta.
     * Relación muchos a uno con la entidad User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Usuario que creó la receta

    /**
     * Ingredientes de la receta.
     * Relacion uno a muchos con la entidad RecipeIngredient.
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    /**
     * Usuarios que han marcado esta receta como favorita.
     * Relación uno a muchos con la entidad Favorite.
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    public enum RecipeStatus {
        PENDING, APPROVED, REJECTED
    }

}