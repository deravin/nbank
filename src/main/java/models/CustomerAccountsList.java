package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = CustomerAccountsListDeserializer.class)
@Data
@NoArgsConstructor
public class CustomerAccountsList extends BaseModel {
    private List<AccountInfoResponse> accounts;
}
