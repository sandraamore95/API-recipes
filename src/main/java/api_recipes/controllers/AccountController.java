package api_recipes.controllers;

import api_recipes.payload.request.ChangeEmailRequest;
import api_recipes.payload.request.ChangePasswordRequest;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Cambiar email", description = "Permite cambiar el email del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email actualizado correctamente",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/change-email")
    public ResponseEntity<SuccessResponse> changeEmail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ChangeEmailRequest changeEmailRequest) {
        accountService.changeEmail(userDetails.getId(), changeEmailRequest.getNewEmail());
        return ResponseEntity.ok(new SuccessResponse("Email actualizado correctamente"));
    }

    @Operation(summary = "Cambiar contraseña", description = "Permite cambiar la contraseña del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "401", description = "Contraseña actual incorrecta")
    })
    @PatchMapping("/change-password")
    public ResponseEntity<SuccessResponse> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {
        accountService.changePassword(userDetails.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new SuccessResponse("Contraseña actualizada correctamente"));
    }
}
