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

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
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

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        UserDto newUser = userService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        accountService.createPasswordResetTokenForUser(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok(new SuccessResponse("Se ha enviado un correo para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        User user = accountService.validatePasswordResetToken(resetPasswordRequest.getToken());
        accountService.updatePassword(user, resetPasswordRequest.getNewPassword());
        accountService.invalidateToken(resetPasswordRequest.getToken());
        return ResponseEntity.ok(new SuccessResponse("Contraseña actualizada correctamente."));
    }
}
