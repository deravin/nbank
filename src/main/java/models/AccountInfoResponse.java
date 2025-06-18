package models;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountInfoResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions;
}