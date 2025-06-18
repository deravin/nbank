package models;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserNameResponse extends BaseModel{
    private String message;
    private AccountResponse customer;
}
