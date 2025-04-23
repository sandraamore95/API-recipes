package api_recipes.controllers;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.InvalidTokenException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.Role;
import api_recipes.models.User;
import api_recipes.payload.request.ForgotPasswordRequest;
import api_recipes.payload.request.LoginRequest;
import api_recipes.payload.request.ResetPasswordRequest;
import api_recipes.payload.request.SignupRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.payload.response.JwtResponse;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.repository.RoleRepository;
import api_recipes.repository.UserRepository;
import api_recipes.security.jwt.JwtUtils;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils,
                          AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.accountService=accountService;
    }


    //LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    //REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // Verificar si el nombre de usuario ya está registrado
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Error: El nombre de usuario ya está en uso");
        }
        // Verificar si el email ya está registrado
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Error: ¡El email  ya está en uso!");
        }

        // Crear cuenta de nuevo usuario
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // Asignar roles
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            // Asignar rol por defecto ROLE_USER
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new InvalidRequestException("El rol ROLE_USER no fue encontrado."));
            roles.add(userRole);
        } else {
            for (String role : strRoles) {
                try {
                    Role.RoleName roleName = Role.RoleName.valueOf(role.toUpperCase());
                    Role foundRole = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new InvalidRequestException("El rol " + role + " no fue encontrado."));
                    roles.add(foundRole);
                } catch (IllegalArgumentException e) {
                    throw new InvalidRequestException("Rol inválido: " + role);
                }
            }
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new SuccessResponse("¡Usuario registrado exitosamente!"));
    }

    //forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            accountService.createPasswordResetTokenForUser(request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("Se ha enviado un correo para restablecer tu contraseña."));
        } catch (ResourceNotFoundException e) {
            // Captura el error cuando el usuario no se encuentra
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Usuario no encontrado", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: Algo salió mal, intenta nuevamente más tarde" , e.getMessage()));
        }
    }

    //reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            User user = accountService.validatePasswordResetToken(request.getToken());
            accountService.updatePassword(user, request.getNewPassword());
            accountService .invalidateToken(request.getToken());
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SuccessResponse("Error: El token de restablecimiento de contraseña no es válido."));
        }
    }

}
