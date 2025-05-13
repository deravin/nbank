package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel{
    private String id;
    private float amount;
    private String type;
    private String relatedAccountID;
}
