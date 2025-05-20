package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.CategoryMapper;
import api_recipes.models.Category;
import api_recipes.payload.dto.CategoryDto;
import api_recipes.payload.request.CategoryRequest;
import api_recipes.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

    public CategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria con el id '" + id + "' no encontrada"));
    }

    @Transactional
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

    @Transactional
    public CategoryDto updateCategory(String categoryName, Long id) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new InvalidRequestException("El nombre de la categoría no puede estar vacío");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Verificar si ya existe
        String newName = categoryName.trim().toUpperCase();
        if (!category.getName().equalsIgnoreCase(newName)
                && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new ResourceAlreadyExistsException("La categoría " + categoryName + " ya existe");
        }

        category.setName(newName);
        categoryRepository.save(category);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        categoryRepository.delete(category);
    }
}