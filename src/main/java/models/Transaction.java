package models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel{
    private String id;
    private float amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}
