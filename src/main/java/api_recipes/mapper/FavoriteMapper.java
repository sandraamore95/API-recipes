package api_recipes.mapper;
import api_recipes.models.Favorite;
import api_recipes.payload.dto.FavoriteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RecipeMapper.class})
public interface FavoriteMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userUsername", source = "user.username")
    @Mapping(target = "recipe", source = "recipe") // Usa el RecipeMapper
    FavoriteDto toDto(Favorite favorite);

    default List<FavoriteDto> toDtoList(List<Favorite> favorites) {
        return favorites.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}