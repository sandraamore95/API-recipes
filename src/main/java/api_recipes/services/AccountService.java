package api_recipes.services;

import api_recipes.exceptions.InvalidTokenException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.TokenUser;
import api_recipes.models.User;
import api_recipes.repository.TokenUserRepository;
import api_recipes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;


@Service
public class AccountService {

    private final TokenUserRepository tokenUserRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public AccountService(TokenUserRepository tokenUserRepository, UserRepository userRepository, JavaMailSender javaMailSender,PasswordEncoder passwordEncoder) {
        this.tokenUserRepository = tokenUserRepository;
        this.userRepository = userRepository;
        this.mailSender = javaMailSender;
        this.passwordEncoder=passwordEncoder;
    }

    @Value("${app.base.url}")
    private String baseUrl;


    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        tokenUserRepository.deleteByUser(user);

        // Crea nuevo token
        TokenUser tokenUser = new TokenUser();
        tokenUser.setToken(UUID.randomUUID().toString());
        tokenUser.setUser(user);
        tokenUser.setExpiryDate(60 * 24);
        tokenUserRepository.save(tokenUser);
        sendPasswordResetEmail(user, tokenUser.getToken());
    }

    private void sendPasswordResetEmail(User user, String token) {
        String url = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Restablecer contraseña");
        mail.setText(
                "Hola " + user.getUsername() + ",\n\n" +
                        "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
                        "Haz clic en el siguiente enlace para crear una nueva:\n" + url + "\n\n" +
                        "Este enlace expirará en 24 horas."

        );

        mailSender.send(mail);
    }

    public User validatePasswordResetToken(String token) {
        TokenUser resetToken = tokenUserRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("El enlace no es válido o ha expirado."));

        if (resetToken.isExpired()) {
            tokenUserRepository.delete(resetToken);
            throw new InvalidTokenException("El enlace no es válido o ha expirado.");
        }
        return resetToken.getUser();
    }

    @Transactional
    public void invalidateToken(String token) {
        tokenUserRepository.deleteByToken(token);
    }

    //update password
    @Transactional
    public void updatePassword(User user, String newPassword) {
        System.out.println(newPassword);
        String hashedPassword = passwordEncoder.encode(newPassword);
        System.out.println(hashedPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }



}
