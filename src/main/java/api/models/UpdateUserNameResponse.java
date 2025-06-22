package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserNameResponse extends BaseModel{
    private String message;
    private AccountResponse customer;
}
