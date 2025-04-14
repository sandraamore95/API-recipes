package api_recipes.repository;
import api_recipes.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Set<Category> findByNameIn(Set<String> names);
    List<Category> findByNameContainingIgnoreCase(String name);
}
