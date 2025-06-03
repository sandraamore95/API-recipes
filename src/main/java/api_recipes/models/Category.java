package api_recipes.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una categoría de recetas en el sistema.
 * Las categorías se utilizan para clasificar y organizar las recetas.
 *
 * @author Sandy
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    /**
     * Identificador único de la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la categoría.
     * No puede estar vacío y debe tener entre 3 y 50 caracteres.
     */
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Recetas asociadas a esta categoría.
     * Relación muchos a muchos con la entidad Recipe.
     */
    @ManyToMany(mappedBy = "categories")
    private Set<Recipe> recipes = new HashSet<>();
}