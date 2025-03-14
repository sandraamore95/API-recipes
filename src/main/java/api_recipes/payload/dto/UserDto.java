package api_recipes.payload.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter @Setter @AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
}