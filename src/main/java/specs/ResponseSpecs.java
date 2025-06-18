package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs(){}

    public static ResponseSpecBuilder defaultResponseSpecBuilder(){
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated(){
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK(){
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String errorValue){
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.equalTo(errorValue))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequestWithErrorInString(){
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"))
                .build();
    }

    public static ResponseSpecification requestReturnsForbidden(){
        return defaultResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.equalTo("Unauthorized access to account"))
                .build();
    }

}
