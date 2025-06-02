package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.UserMapper;
import api_recipes.models.Role;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import api_recipes.payload.request.SignupRequest;
import api_recipes.payload.request.UserRequest;
import api_recipes.repository.RoleRepository;
import api_recipes.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
        this.encoder = passwordEncoder;
    }

    
    public Page<UserDto> getAllPageableUser(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    // Obtener todos los usuarios
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener usuario por ID
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario  con el id '" + id + "' no encontrado"));

    }

    // Crear usuario
    public UserDto registerUser(SignupRequest signUpRequest) {
        // Verificar si el nombre de usuario ya está registrado
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        // Verificar si el email ya está registrado
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("El email ya está en uso!");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        // Asignar roles
        Set<Role> roles = assignRoles(signUpRequest.getRole());
        if (roles.isEmpty()) {
            roles.add(roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto no encontrado.")));
        }
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    // Actualizar usuario
    public UserDto updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar si el nuevo username ya existe (excepto para el usuario actual)
        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        // Verificar si el nuevo email ya existe (excepto para el usuario actual)
        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("El email ya está en uso");
        }

        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());

        // Update roles
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            Set<Role> roles = assignRoles(userRequest.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    // Eliminar usuario
    public void deleteUser(Long userId) {
        User deleteUser = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + userId));
        userRepository.delete(deleteUser);
    }


    // METODOS 
    
    private Set<Role> assignRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty())
            return roles;

        return strRoles.stream()
                .map(role -> {
                    try {
                        Role.RoleName roleName = Role.RoleName.valueOf("ROLE_" + role.toUpperCase());
                        return roleRepository.findByName(roleName)
                                .orElseThrow(() -> new ResourceNotFoundException("Rol " + role + " no encontrado"));
                    } catch (IllegalArgumentException e) {
                        throw new InvalidRequestException("Rol inválido: " + role);
                    }
                })
                .collect(Collectors.toSet());
    }

}