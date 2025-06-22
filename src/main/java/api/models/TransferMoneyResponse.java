package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel{
    private long senderAccountId;
    private long receiverAccountId;
    private float amount;
    private String message;
}
