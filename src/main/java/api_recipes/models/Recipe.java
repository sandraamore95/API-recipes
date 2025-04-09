package api_recipes.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String preparation;

    private int popularity;

    @Enumerated(EnumType.STRING)
    private RecipeStatus status = RecipeStatus.PENDING; // Estado inicial

    @ManyToMany
    @JoinTable(
            name = "recipe_categories",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Usuario que creó la receta

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public enum RecipeStatus {
        PENDING, APPROVED, REJECTED
    }

}