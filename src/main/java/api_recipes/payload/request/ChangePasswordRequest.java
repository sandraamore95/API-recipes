package api_recipes.payload.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequest {


    @NotBlank(message = "La contraseña actual no puede estar vacía")
    private String oldPassword;
    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String newPassword;
}
