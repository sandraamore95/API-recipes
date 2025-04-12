package api_recipes.mapper;
import api_recipes.models.Ingredient;
import api_recipes.payload.dto.IngredientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    IngredientDto toDto(Ingredient ingredient);

    List<IngredientDto> toDtoList(List<Ingredient> ingredients);

    @Mapping(target = "id", ignore = true)
    Ingredient toEntity(IngredientDto dto);
}