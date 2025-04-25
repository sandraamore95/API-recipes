package api_recipes.controllers;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.InvalidTokenException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.User;
import api_recipes.payload.request.ForgotPasswordRequest;
import api_recipes.payload.request.LoginRequest;
import api_recipes.payload.request.ResetPasswordRequest;
import api_recipes.payload.request.SignupRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.payload.response.JwtResponse;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.security.jwt.JwtUtils;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import api_recipes.services.UserService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils,
                          AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.userService=userService;
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
        try {
            User newUser = userService.registerUser(signUpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Recurso no encontrado", e.getMessage()));
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear usuario", "Ocurrió un error inesperado"));
        }
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
