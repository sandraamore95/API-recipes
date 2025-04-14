package api_recipes.mapper;
import api_recipes.models.Category;
import api_recipes.payload.dto.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    List<CategoryDto> toDtoList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recipes", ignore = true) // Para evitar ciclos infinitos
    Category toEntity(CategoryDto dto);
}