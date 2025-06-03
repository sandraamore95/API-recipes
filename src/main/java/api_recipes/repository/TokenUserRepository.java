package api_recipes.repository;
import api_recipes.models.TokenUser;
import api_recipes.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenUserRepository extends JpaRepository<TokenUser, Long> {

    Optional<TokenUser> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
    void deleteAllByUserId(Long userId);
}
