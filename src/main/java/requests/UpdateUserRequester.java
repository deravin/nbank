package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UpdateUserNameRequest;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public class UpdateUserRequester extends Request<UpdateUserNameRequest> {
    public UpdateUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UpdateUserNameRequest model) {
        return null;
    }

    @Override
    public ValidatableResponse put(UpdateUserNameRequest newName) {
        return  given()
                .spec(requestSpecification)
                .body(newName)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .body("customer.name", Matchers.equalTo(newName.getName()));
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }
}