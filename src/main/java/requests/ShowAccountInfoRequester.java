package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Account;
import models.AccountInfoResponse;
import models.BaseModel;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class ShowAccountInfoRequester extends Request<AccountInfoResponse> {
    public ShowAccountInfoRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(AccountInfoResponse model) {
        return null;
    }

    @Override
    public ValidatableResponse put(AccountInfoResponse model) {
        return null;
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
