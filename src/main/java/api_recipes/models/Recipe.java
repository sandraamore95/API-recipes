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

    @Enumerated(EnumType.STRING)
    private RecipeStatus status = RecipeStatus.PENDING; // Estado inicial

    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "recipe_categories", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Category> categories = new HashSet<>();



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Usuario que creó la receta


    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public boolean hasCategory(Category category) {
        return categories.contains(category);
    }

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories);
    }
    public enum Category {
        SALUDABLE, SENCILLA, CENAR, COMER, POSTRE, VEGETARIANA,DESAYUNO
    }
    public enum RecipeStatus {
        PENDING, APPROVED, REJECTED
    }

}