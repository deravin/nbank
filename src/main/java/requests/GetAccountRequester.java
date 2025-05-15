package requests;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Account;
import models.BaseModel;

import java.util.List;

import static io.restassured.RestAssured.given;

public class GetAccountRequester extends Request {
    public GetAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }

    public List<Account> getAccounts() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/customer/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract()
                .as(new TypeRef<List<Account>>() {});
    }
}
