package api.models;

import api.generators.GeneratingRule;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regexp = "^[a-zA-Z0-9._-]{3,15}$")
    private String username;
    @GeneratingRule(regexp = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingRule(regexp = "^USER$")
    private String role;
}