package api_recipes.mapper;
import api_recipes.models.Role;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(rolesToStringSet(user.getRoles()))")
    UserDto toDTO(User user);

    @Mapping(target = "roles", expression = "java(stringSetToRoleSet(userDTO.getRoles()))")
    User toEntity(UserDto userDTO);

    default Set<String> rolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    default Set<Role> stringSetToRoleSet(Set<String> roleNames) {
        if (roleNames == null) {
            return null;
        }
        return roleNames.stream()
                .map(name -> {
                    Role role = new Role();
                    role.setName(Role.RoleName.valueOf(name));
                    return role;
                })
                .collect(Collectors.toSet());
    }
}