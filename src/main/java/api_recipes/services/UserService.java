package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.UserMapper;
import api_recipes.models.Role;
import api_recipes.models.Favorite;
import api_recipes.models.Recipe;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import api_recipes.payload.request.SignupRequest;
import api_recipes.payload.request.UserRequest;
import api_recipes.repository.RoleRepository;
import api_recipes.repository.TokenUserRepository;
import api_recipes.repository.UserRepository;
import api_recipes.repository.FavoriteRepository;
import api_recipes.repository.RecipeRepository;
import api_recipes.services.RecipeService;
import api_recipes.services.ImageUploadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final RecipeRepository recipeRepository;
    private final ImageUploadService imageUploadService;
    private final FavoriteRepository favoriteRepository;
    private final TokenUserRepository tokenRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, RecipeRepository recipeRepository, ImageUploadService imageUploadService,
            FavoriteRepository favoriteRepository, TokenUserRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
        this.encoder = passwordEncoder;
        this.recipeRepository = recipeRepository;
        this.imageUploadService = imageUploadService;
        this.favoriteRepository = favoriteRepository;
        this.tokenRepository = tokenRepository;
    }

    public Page<UserDto> getAllPageableUser(int page, int pageSize) {
        logger.info("Obteniendo usuarios paginados - página: {}, tamaño: {}", page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    // Obtener todos los usuarios
    public List<UserDto> getAllUsers() {
        logger.info("Obteniendo todos los usuarios");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener usuario por ID
    public UserDto getUserById(Long id) {
        logger.info("Buscando usuario por ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Usuario con el id '" + id + "' no encontrado");
                });
    }

    @Transactional
    // Crear usuario
    public UserDto registerUser(SignupRequest signUpRequest) {
        logger.info("Iniciando registro de nuevo usuario: {}", signUpRequest.getUsername());
        
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.warn("Intento de registro con username duplicado: {}", signUpRequest.getUsername());
            throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("Intento de registro con email duplicado: {}", signUpRequest.getEmail());
            throw new ResourceAlreadyExistsException("El email ya está en uso!");
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        Set<Role> roles = assignRoles(signUpRequest.getRoles());
        if (roles.isEmpty()) {
            roles.add(roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> {
                        logger.error("Rol por defecto ROLE_USER no encontrado");
                        return new ResourceNotFoundException("Rol por defecto no encontrado.");
                    }));
        }
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        logger.info("Usuario registrado exitosamente con ID: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    @Transactional
    // Actualizar usuario
    public UserDto updateUser(Long id, UserRequest userRequest) {
        logger.info("Iniciando actualización de usuario con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para actualización - ID: {}", id);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            logger.warn("Intento de actualización con username duplicado: {}", userRequest.getUsername());
            throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            logger.warn("Intento de actualización con email duplicado: {}", userRequest.getEmail());
            throw new ResourceAlreadyExistsException("El email ya está en uso");
        }

        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());

        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            Set<Role> roles = assignRoles(userRequest.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        logger.info("Usuario actualizado exitosamente - ID: {}", updatedUser.getId());
        return userMapper.toDTO(updatedUser);
    }

    @Transactional
    public void deleteUserAndRelations(Long userId) throws IOException {
        logger.info("Iniciando eliminación de usuario y sus relaciones - ID: {}", userId);
        
        User deleteUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado para eliminación - ID: {}", userId);
                    return new ResourceNotFoundException("No se encontró el usuario con ID: " + userId);
                });

        tokenRepository.deleteAllByUserId(userId);
        logger.debug("Tokens eliminados para usuario - ID: {}", userId);

        List<Favorite> userFavorites = favoriteRepository.findAllByUser(deleteUser);
        favoriteRepository.deleteAll(userFavorites);
        logger.debug("Favoritos eliminados para usuario - ID: {}", userId);

        List<Recipe> myRecipes = recipeRepository.findByUserId(deleteUser.getId());
        for (Recipe recipe : myRecipes) {
            if (recipe.getImageUrl() != null) {
                try {
                    imageUploadService.deleteDirectoryAndImage("recipes", recipe.getId());
                    logger.debug("Imagen de receta eliminada - ID: {}", recipe.getId());
                } catch (IOException e) {
                    logger.error("Error al eliminar imagen de receta - ID: {}, Error: {}", recipe.getId(), e.getMessage());
                    throw new IOException("Error al eliminar la imagen de la receta: " + e.getMessage());
                }
            }
        }
        
        recipeRepository.deleteAll(myRecipes);
        logger.debug("Recetas eliminadas para usuario - ID: {}", userId);
        
        userRepository.delete(deleteUser);
        logger.info("Usuario y todas sus relaciones eliminadas exitosamente - ID: {}", userId);
    }

    // METODOS

    private Set<Role> assignRoles(Set<String> strRoles) {
        logger.debug("Asignando roles: {}", strRoles);
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty())
            return roles;

        return strRoles.stream()
                .map(role -> {
                    try {
                        Role.RoleName roleName = Role.RoleName.valueOf(role.toUpperCase());
                        return roleRepository.findByName(roleName)
                                .orElseThrow(() -> {
                                    logger.error("Rol no encontrado: {}", role);
                                    return new ResourceNotFoundException("Rol " + role + " no encontrado");
                                });
                    } catch (IllegalArgumentException e) {
                        logger.error("Rol inválido: {}", role);
                        throw new InvalidRequestException("Rol inválido: " + role);
                    }
                })
                .collect(Collectors.toSet());
    }

}