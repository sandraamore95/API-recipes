package api_recipes.controllers;

import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import api_recipes.payload.request.ForgotPasswordRequest;
import api_recipes.payload.request.LoginRequest;
import api_recipes.payload.request.ResetPasswordRequest;
import api_recipes.payload.request.SignupRequest;
import api_recipes.payload.response.JwtResponse;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.security.jwt.JwtUtils;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import api_recipes.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "APIs para autenticación y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager,
            UserService userService,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.accountService = accountService;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
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

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El username o email ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        UserDto newUser = userService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @Operation(summary = "Solicitar restablecimiento de contraseña", 
               description = "Envía un email con un enlace para restablecer la contraseña")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Email inválido"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        accountService.createPasswordResetTokenForUser(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok(new SuccessResponse("Se ha enviado un correo para restablecer tu contraseña."));
    }

    @Operation(summary = "Restablecer contraseña", 
               description = "Establece una nueva contraseña usando el token recibido por email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Token o contraseña inválidos"),
        @ApiResponse(responseCode = "404", description = "Token no encontrado")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        User user = accountService.validatePasswordResetToken(resetPasswordRequest.getToken());
        accountService.updatePassword(user, resetPasswordRequest.getNewPassword());
        accountService.invalidateToken(resetPasswordRequest.getToken());
        return ResponseEntity.ok(new SuccessResponse("Contraseña actualizada correctamente."));
    }
}
