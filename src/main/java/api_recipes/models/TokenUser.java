package api_recipes.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TokenUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
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
