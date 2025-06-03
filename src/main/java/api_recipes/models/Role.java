package api_recipes.models;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un rol de usuario en el sistema.
 * Los roles definen los permisos y capacidades que tiene un usuario en la aplicación.
 *
 * @author Sandy
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    /**
     * Identificador único del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol.
     * Define el tipo de rol (ej: ROLE_USER, ROLE_ADMIN).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    /**
     * Usuarios que tienen asignado este rol.
     * Relación muchos a muchos con la entidad User.
     */
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    /**
     * Enumeración que define los tipos de roles disponibles en el sistema.
     */
    public enum RoleName {
        ROLE_USER,
        ROLE_MODERATOR,
        ROLE_ADMIN
    }

    // Constructor con solo el nombre del rol
    public Role(RoleName name) {
        this.name = name;
    }

}
