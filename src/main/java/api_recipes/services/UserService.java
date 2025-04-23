package api_recipes.services;
import api_recipes.exceptions.InvalidTokenException;
import api_recipes.mapper.UserMapper;
import api_recipes.models.TokenUser;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import api_recipes.repository.TokenUserRepository;
import api_recipes.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenUserRepository tokenUserRepository;


    public UserService(UserRepository userRepository,UserMapper userMapper,TokenUserRepository tokenUserRepository) {
        this.userRepository = userRepository;
        this.userMapper=userMapper;
        this.tokenUserRepository=tokenUserRepository;
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