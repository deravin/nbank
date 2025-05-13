package models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserRequest extends BaseModel {
    private String username;
    private String password;
}
