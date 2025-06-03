package api_recipes.controllers;

import api_recipes.payload.request.ChangeEmailRequest;
import api_recipes.payload.request.ChangePasswordRequest;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping("/change-email")
    public ResponseEntity<SuccessResponse> changeEmail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ChangeEmailRequest changeEmailRequest) {
        accountService.changeEmail(userDetails.getId(), changeEmailRequest.getNewEmail());
        return ResponseEntity.ok(new SuccessResponse("Email actualizado correctamente"));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<SuccessResponse> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {
        accountService.changePassword(userDetails.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new SuccessResponse("Contraseña actualizada correctamente"));
    }
}
