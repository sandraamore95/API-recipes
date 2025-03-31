package api_recipes.mapper;
import api_recipes.models.Recipe;
import api_recipes.models.Category;
import api_recipes.payload.dto.RecipeDto;
import api_recipes.payload.request.RecipeRequest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecipeMapper {
    RecipeMapper INSTANCE = Mappers.getMapper(RecipeMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "categories", target = "categories", qualifiedByName = "categoriesToNames")
    RecipeDto toDTO(Recipe recipe);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "categories", target = "categories", qualifiedByName = "namesToCategories")
    Recipe toEntity(RecipeDto recipeDTO);

    // ✅ Agregamos la conversión de categorías en el mapeo de RecipeRequest -> Recipe
    @Mapping(source = "categories", target = "categories", qualifiedByName = "namesToCategories")
    Recipe toEntity(RecipeRequest recipeRequest);

    @Named("categoriesToNames")
    default Set<String> mapCategoriesToNames(Set<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }

    @Named("namesToCategories")
    default Set<Category> mapNamesToCategories(Set<String> categoryNames) {
        if (categoryNames == null) {
            return null;
        }
        return categoryNames.stream()
                .map(categoryName -> {
                    Category category = new Category();
                    category.setName(categoryName);
                    return category;
                })
                .collect(Collectors.toSet());
    }
}