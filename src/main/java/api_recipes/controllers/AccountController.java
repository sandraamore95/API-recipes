package api_recipes.controllers;

import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.User;
import api_recipes.payload.request.ChangeEmailRequest;
import api_recipes.payload.request.ChangePasswordRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.AccountService;
import api_recipes.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping("/change-email")
    public ResponseEntity<?> changeEmail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody @Valid ChangeEmailRequest changeEmailRequest) {
        try {
            accountService.changeEmail(userDetails.getId(), changeEmailRequest.getNewEmail());
            return ResponseEntity.ok("Email actualizado correctamente");
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error al actualizar el email"));
        }
    }


    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody @Valid ChangePasswordRequest request) {
        try {
            accountService.changePassword(userDetails.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_PASSWORD", e.getMessage()));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
        }
    }
}
