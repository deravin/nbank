package requests;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.TransferMoneyRequest;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class TransferMoneyRequester extends Request<TransferMoneyRequest> {

    public TransferMoneyRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(TransferMoneyRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(TransferMoneyRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }
}
