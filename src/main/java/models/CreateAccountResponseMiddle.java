package models;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponseMiddle extends BaseModel{
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<AccountInfoResponse> accountInfoRespons;

}
