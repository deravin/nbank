package models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddDepositRequest extends BaseModel {
    private long id;
    private float balance;
}
