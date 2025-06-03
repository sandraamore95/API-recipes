package api_recipes.controllers;

import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.models.User;
import api_recipes.payload.dto.FavoriteDto;
import api_recipes.payload.response.SuccessResponse;
import api_recipes.repository.UserRepository;
import api_recipes.security.services.UserDetailsImpl;
import api_recipes.services.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(UserDetailsImpl userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDto>> getUserFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        List<FavoriteDto> favorites = favoriteService.getUserFavorites(user);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{recipeId}")
    public ResponseEntity<SuccessResponse> addFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        favoriteService.addFavorite(user, recipeId);
        return ResponseEntity.ok(new SuccessResponse("Receta agregada a favoritos"));
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<SuccessResponse> removeFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        favoriteService.removeFavorite(user, recipeId);
        return ResponseEntity.ok(new SuccessResponse("Receta eliminada de favoritos"));

    }

    @GetMapping("/exists/{recipeId}")
    public ResponseEntity<?> isFavorite(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getAuthenticatedUser(userDetails);
        boolean exists = favoriteService.existsByUserAndRecipe(user, recipeId);
        return ResponseEntity.ok(Map.of("isFavorite", exists));

    }
}