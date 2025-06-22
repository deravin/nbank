package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddDepositRequest extends BaseModel {
    private long id;
    private float balance;
}
