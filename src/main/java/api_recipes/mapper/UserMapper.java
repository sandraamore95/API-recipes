package api_recipes.mapper;
import api_recipes.models.User;
import api_recipes.payload.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDTO(User user);
    User toEntity(UserDto userDTO);
}