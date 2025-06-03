package api_recipes.services;

import api_recipes.exceptions.InvalidRequestException;
import api_recipes.exceptions.ResourceAlreadyExistsException;
import api_recipes.exceptions.ResourceNotFoundException;
import api_recipes.mapper.CategoryMapper;
import api_recipes.models.Category;
import api_recipes.payload.dto.CategoryDto;
import api_recipes.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryDto> getAllCategories() {
        logger.info("Obteniendo todas las categorías");
        List<Category> categories = categoryRepository.findAll();
        logger.debug("Se encontraron {} categorías", categories.size());
        return categoryMapper.toDtoList(categories);
    }

    public CategoryDto getCategoryById(Long id) {
        logger.info("Buscando categoría por ID: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> {
                    logger.error("Categoría no encontrada con ID: {}", id);
                    return new ResourceNotFoundException("Categoria con el id '" + id + "' no encontrada");
                });
    }

    @Transactional
    public CategoryDto createCategory(String categoryName) {
        logger.info("Iniciando creación de nueva categoría: {}", categoryName);
        
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Intento de crear categoría con nombre vacío");
            throw new InvalidRequestException("El nombre de la categoría no puede estar vacío");
        }

        if (categoryRepository.existsByNameIgnoreCase(categoryName.trim())) {
            logger.warn("Intento de crear categoría duplicada: {}", categoryName);
            throw new ResourceAlreadyExistsException("La categoría " + categoryName + " ya existe");
        }

        Category category = new Category();
        category.setName(categoryName.trim().toUpperCase());
        Category saved = categoryRepository.save(category);
        logger.info("Categoría creada exitosamente con ID: {}", saved.getId());
        return categoryMapper.toDto(saved);
    }

    @Transactional
    public CategoryDto updateCategory(String categoryName, Long id) {
        logger.info("Iniciando actualización de categoría ID: {} con nuevo nombre: {}", id, categoryName);
        
        if (categoryName == null || categoryName.trim().isEmpty()) {
            logger.error("Intento de actualizar categoría con nombre vacío");
            throw new InvalidRequestException("El nombre de la categoría no puede estar vacío");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Categoría no encontrada para actualización - ID: {}", id);
                    return new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
                });

        String newName = categoryName.trim().toUpperCase();
        if (!category.getName().equalsIgnoreCase(newName)
                && categoryRepository.existsByNameIgnoreCase(newName)) {
            logger.warn("Intento de actualización con nombre duplicado: {}", newName);
            throw new ResourceAlreadyExistsException("La categoría " + categoryName + " ya existe");
        }

        category.setName(newName);
        categoryRepository.save(category);
        logger.info("Categoría actualizada exitosamente - ID: {}", id);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        logger.info("Iniciando eliminación de categoría ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Categoría no encontrada para eliminación - ID: {}", id);
                    return new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
                });
        
        categoryRepository.delete(category);
        logger.info("Categoría eliminada exitosamente - ID: {}", id);
    }
}