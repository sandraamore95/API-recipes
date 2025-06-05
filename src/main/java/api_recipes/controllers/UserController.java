package api_recipes.controllers;
import api_recipes.payload.dto.UserDto;
import api_recipes.payload.request.UserRequest;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "APIs para gestionar usuarios")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Obtener todos los usuarios paginados
    @Operation(summary = "Obtener usuarios paginados", description = "Retorna una página de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios encontrados", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int size) {
        Page<UserDto> users = userService.getAllPageableUser(page, size);
        return ResponseEntity.ok(users);
    }

    // Obtener todos los usuarios
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista completa de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios encontrados", content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsersList() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Obtener un usuario por ID
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

   // Actualizar un usuario
   @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
   @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
   })
   @PutMapping("/{id}")
   public ResponseEntity<UserDto> updateUser( @PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
       UserDto updatedUser = userService.updateUser(id, userRequest);
       return ResponseEntity.ok(updatedUser);
   }

    // Eliminar un usuario
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar este usuario"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable Long id) {
            // Eliminar todas las relaciones que tiene user  + eliminar la imagen
        userService.deleteUserAndRelations(id);
        return ResponseEntity.ok(new SuccessResponse("Usuario eliminado correctamente"));
    }
}