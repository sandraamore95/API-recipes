package api_recipes.services;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.InvalidTokenException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.mapper.UserMapper;
import api_recipes.models.Role;
import api_recipes.models.TokenUser;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import api_recipes.payload.request.SignupRequest;
import api_recipes.repository.RoleRepository;
import api_recipes.repository.TokenUserRepository;
import api_recipes.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final  RoleRepository roleRepository;
    private final  PasswordEncoder encoder;


    public UserService(UserRepository userRepository, UserMapper userMapper, RoleRepository roleRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper=userMapper;
        this.roleRepository=roleRepository;
        this.encoder=passwordEncoder;
    }

    // Obtener todos los usuarios
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener usuario por ID
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDTO);
    }

    //Crear usuario
    public User registerUser(SignupRequest signUpRequest) {
        // Verificar si el nombre de usuario ya está registrado
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Error: El nombre de usuario ya está en uso");
        }

        // Verificar si el email ya está registrado
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Error: ¡El email ya está en uso!");
        }

        // Crear cuenta de nuevo usuario
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // Asignar roles
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            // Asignar rol por defecto ROLE_USER
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new InvalidRequestException("El rol ROLE_USER no fue encontrado."));
            roles.add(userRole);
        } else {
            for (String role : strRoles) {
                try {
                    Role.RoleName roleName = Role.RoleName.valueOf(role.toUpperCase());
                    Role foundRole = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new InvalidRequestException("El rol " + role + " no fue encontrado."));
                    roles.add(foundRole);
                } catch (IllegalArgumentException e) {
                    throw new InvalidRequestException("Rol inválido: " + role);
                }
            }
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }



    // Actualizar usuario
    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            User updatedUser = userRepository.save(user);
            return userMapper.toDTO(updatedUser);
        });
    }

    // Eliminar usuario
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }


}