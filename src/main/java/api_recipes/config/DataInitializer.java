package api_recipes.config;

import api_recipes.models.Category;
import api_recipes.models.Role;
import api_recipes.models.User;
import api_recipes.repository.CategoryRepository;
import api_recipes.repository.RoleRepository;
import api_recipes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Inicializar roles
        initializeRoles();
        
        // Inicializar categorías
        initializeCategories();
        
        // Inicializar usuarios
        initializeUsers();
    }

    private void initializeRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void initializeCategories() {
        String[] categories = {
            "Desayunos",
            "Almuerzos",
            "Cenas",
            "Postres",
            "Bebidas",
        };

        for (String categoryName : categories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = Category.builder()
                        .name(categoryName)
                        .build();
                categoryRepository.save(category);
            }
        }
    }

    private void initializeUsers() {
        // Crear usuario admin si no existe
        if (!userRepository.existsByUsername("admin")) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado")));
            adminRoles.add(roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Rol USER no encontrado")));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(adminRoles)
                    .build();
            userRepository.save(admin);
        }

        // Crear usuario normal si no existe
        if (!userRepository.existsByUsername("user")) {
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Rol USER no encontrado")));

            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .roles(userRoles)
                    .build();
            userRepository.save(user);
        }
    }
} 