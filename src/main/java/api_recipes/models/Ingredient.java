package api_recipes.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un ingrediente en el sistema.
 * Los ingredientes son los componentes básicos de las recetas y pueden
 * estar asociados a múltiples recetas con diferentes cantidades.
 *
 * @author Sandy
 * @version 1.0
 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "ingredients")
    public class Ingredient {
    /**
     * Identificador único del ingrediente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del ingrediente.
     * No puede estar vacío y debe tener entre 2 y 50 caracteres.
     */
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * URL de la imagen asociada al ingrediente.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Unidad de medida del ingrediente (ej: gramos, litros, unidades).
     */
    @Enumerated(EnumType.STRING)
    private UnitMeasure unit_measure;

    /**
     * Indica si el ingrediente está activo en el sistema.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Recetas que utilizan este ingrediente.
     * Relación uno a muchos con la entidad RecipeIngredient.
     */
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public enum UnitMeasure {
        GRAMOS,
        MILILITROS,
        TAZAS,
        UNIDADES,
        LITROS,
        CUCHARADAS,
        CUCHARADITAS
    }

}
