package api_recipes.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un usuario en el sistema.
 * Esta clase almacena la información básica de un usuario incluyendo sus credenciales
 * y roles asignados.
 *
 * @author Sandy
 * @version 1.0
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class User {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único.
     * No puede estar vacío y debe tener entre 3 y 20 caracteres.
     */
    @NotBlank
    @Size( max = 20)
    @Column(nullable = false)
    private String username;

    /**
     * Correo electrónico único del usuario.
     * Debe ser una dirección de correo válida.
     */
    @NotBlank
    @Size(max = 50)
    @Email
    @Column(nullable = false)
    private String email;

    /**
     * Contraseña encriptada del usuario.
     * No puede estar vacía y debe tener entre 6 y 40 caracteres.
     */
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String password;

    /**
     * Roles asignados al usuario.
     * Relación muchos a muchos con la entidad Role.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}