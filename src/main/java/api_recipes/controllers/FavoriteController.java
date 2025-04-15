package api_recipes.controllers;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.Favorite;
import api_recipes.models.User;
import api_recipes.payload.dto.FavoriteDto;
import api_recipes.payload.dto.IngredientDto;
import api_recipes.payload.request.FavoriteRequest;
import api_recipes.payload.response.ErrorResponse;
import api_recipes.repository.UserRepository;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService,UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository=userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getUserFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            List<FavoriteDto> favorites = favoriteService.getUserFavorites(user);
            return ResponseEntity.ok(favorites);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error interno", "Error al obtener favoritos"));
        }
    }


    @PostMapping("/{recipeId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long recipeId, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            FavoriteDto favorite = favoriteService.addFavorite(user, recipeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Receta no encontrada", ex.getMessage()));
        } catch (ResourceAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Conflicto", ex.getMessage()));
        } catch (InvalidRequestException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Solicitud inválida", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error interno", "Ocurrió un error inesperado"));
        }
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            favoriteService.removeFavorite(user, recipeId);
            return ResponseEntity.ok().body(Map.of("message", "Receta eliminada de favoritos "));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Receta no encontrada", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error interno", "Ocurrió un error al eliminar favorito"));
        }
    }

    @GetMapping("/exists/{recipeId}")
    public ResponseEntity<?> isFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            boolean exists = favoriteService.existsByUserAndRecipe(user, recipeId);
            return ResponseEntity.ok(Map.of("isFavorite", exists));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Usuario no encontrado", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error interno", "No se pudo verificar el favorito"));
        }
    }



}