package api_recipes.services;
import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.mapper.CategoryMapper;
import api_recipes.models.Category;
import api_recipes.payload.dto.CategoryDto;
import api_recipes.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }

    public CategoryDto createCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidRequestException("El nombre de la categoría no puede estar vacío");
        }

        // Verificar si ya existe
        if (categoryRepository.existsByNameIgnoreCase(categoryName.trim())) {
            throw new ResourceAlreadyExistsException("La categoría " + categoryName + " ya existe");
        }

        Category category = new Category();
        category.setName(categoryName.trim().toUpperCase());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }
}