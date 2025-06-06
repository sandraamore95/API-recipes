package api_recipes.services;

import api_recipes.exceptions.ExpiredTokenException;
import api_recipes.exceptions.InvalidCurrentPasswordException;
import api_recipes.exceptions.InvalidTokenException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.TokenUser;
import api_recipes.models.User;
import api_recipes.repository.TokenUserRepository;
import api_recipes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio que maneja todas las operaciones relacionadas con la cuenta de usuario.
 * Incluye funcionalidades para restablecer contraseñas, cambiar email y contraseña,
 * y validar tokens de seguridad.
 *
 * @author Sandy
 * @version 1.0
 */
@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final TokenUserRepository tokenUserRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor del servicio de cuenta.
     *
     * @param tokenUserRepository Repositorio de tokens de usuario
     * @param userRepository Repositorio de usuarios
     * @param javaMailSender Servicio de envío de emails
     * @param passwordEncoder Codificador de contraseñas
     */
    public AccountService(TokenUserRepository tokenUserRepository, UserRepository userRepository, JavaMailSender javaMailSender, PasswordEncoder passwordEncoder) {
        this.tokenUserRepository = tokenUserRepository;
        this.userRepository = userRepository;
        this.mailSender = javaMailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.base.url}")
    private String baseUrl;

    /**
     * Crea un token para restablecer la contraseña de un usuario.
     *
     * @param email Email del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        logger.info("Iniciando proceso de restablecimiento de contraseña para email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para restablecer contraseña - Email: {}", email);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        tokenUserRepository.deleteByUser(user);
        logger.debug("Tokens anteriores eliminados para el usuario: {}", user.getUsername());

        TokenUser tokenUser = new TokenUser();
        tokenUser.setToken(UUID.randomUUID().toString());
        tokenUser.setUser(user);
        tokenUser.setExpiryDate(60 * 24);
        tokenUserRepository.save(tokenUser);
        logger.debug("Nuevo token creado para el usuario: {}", user.getUsername());

        sendPasswordResetEmail(user, tokenUser.getToken());
        logger.info("Email de restablecimiento enviado al usuario: {}", user.getUsername());
    }

    /**
     * Envía un email con el enlace para restablecer la contraseña.
     *
     * @param user Usuario al cual enviar el email
     * @param token Token de restablecimiento
     */
    private void sendPasswordResetEmail(User user, String token) {
        logger.debug("Preparando email de restablecimiento para usuario: {}", user.getUsername());
        
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
        logger.debug("Email de restablecimiento enviado exitosamente a: {}", user.getEmail());
    }

    /**
     * Valida un token de restablecimiento de contraseña.
     *
     * @param token Token a validar
     * @return Usuario asociado al token
     * @throws ResourceNotFoundException si el token no existe
     * @throws InvalidTokenException si el token ha expirado
     */
    public User validatePasswordResetToken(String token) {
        logger.info("Validando token de restablecimiento de contraseña");
        
        TokenUser resetToken = tokenUserRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.error("Token inválido: {}", token);
                    return new InvalidTokenException("El enlace no es válido.");
                });

        if (resetToken.isExpired()) {
            logger.warn("Token expirado: {}", token);
            tokenUserRepository.delete(resetToken);
            throw new ExpiredTokenException("El enlace ha expirado.");
        }
        
        logger.info("Token validado exitosamente para usuario: {}", resetToken.getUser().getUsername());
        return resetToken.getUser();
    }

    /**
     * Invalida un token de usuario.
     *
     * @param token Token a invalidar
     */
    @Transactional
    public void invalidateToken(String token) {
        logger.info("Invalidando token: {}", token);
        tokenUserRepository.deleteByToken(token);
        logger.debug("Token eliminado exitosamente");
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param user Usuario al cual actualizar la contraseña
     * @param newPassword Nueva contraseña
     */
    @Transactional
    public void updatePassword(User user, String newPassword) {
        logger.info("Actualizando contraseña para usuario: {}", user.getUsername());
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        
        logger.info("Contraseña actualizada exitosamente para usuario: {}", user.getUsername());
    }

    /**
     * Cambia el email de un usuario.
     *
     * @param userId ID del usuario
     * @param newEmail Nuevo email
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws ResourceAlreadyExistsException si el nuevo email ya está en uso
     */
    public void changeEmail(Long userId, String newEmail) {
        logger.info("Iniciando cambio de email para usuario ID: {}", userId);
        
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            logger.warn("Intento de cambio a email ya existente: {}", newEmail);
            throw new ResourceAlreadyExistsException("El email ya está en uso por otro usuario");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para cambio de email - ID: {}", userId);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });
        
        user.setEmail(newEmail);
        userRepository.save(user);
        logger.info("Email actualizado exitosamente para usuario: {}", user.getUsername());
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param userId ID del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws IllegalArgumentException si la contraseña actual es incorrecta
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Iniciando cambio de contraseña para usuario ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para cambio de contraseña - ID: {}", userId);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Contraseña actual incorrecta para usuario: {}", user.getUsername());
            throw new InvalidCurrentPasswordException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Contraseña cambiada exitosamente para usuario: {}", user.getUsername());
    }
}
