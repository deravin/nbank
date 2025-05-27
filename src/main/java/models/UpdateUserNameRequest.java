package models;

import generators.GeneratingRule;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserNameRequest extends BaseModel {
    @GeneratingRule(regexp = "^[a-zA-Z0-9._-]{3,15}$")
    private String name;
}
