package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Account;
import models.AddDepositRequest;

import static io.restassured.RestAssured.given;

public class AddDepositRequester extends Request<AddDepositRequest>{
    public AddDepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(AddDepositRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(AddDepositRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }
}
