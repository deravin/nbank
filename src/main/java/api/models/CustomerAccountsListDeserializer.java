package api.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class CustomerAccountsListDeserializer extends JsonDeserializer<CustomerAccountsList> {
    @Override
    public CustomerAccountsList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        List<AccountInfoResponse> accounts = mapper.readValue(p, mapper.getTypeFactory().constructCollectionType(List.class, AccountInfoResponse.class));

        CustomerAccountsList result = new CustomerAccountsList();
        result.setAccounts(accounts);
        return result;
    }

}
