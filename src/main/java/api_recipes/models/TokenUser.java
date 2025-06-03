package api_recipes.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Entidad que representa un token de usuario en el sistema.
 * Esta clase se utiliza para manejar tokens de restablecimiento de contraseña
 * y otros tokens temporales asociados a usuarios.
 *
 * @author Sandy
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class TokenUser {
    /**
     * Identificador único del token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token generado para el usuario.
     * Debe ser único en el sistema.
     */
    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    // Calcula la fecha de expiración (24 horas)
    public void setExpiryDate(int minutes) {
        this.expiryDate = Date.from(
                LocalDateTime.now().plusMinutes(minutes)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    public boolean isExpired() {
        return expiryDate.before(new Date());
    }
}
