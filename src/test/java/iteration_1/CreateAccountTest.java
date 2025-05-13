package iteration_1;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);


        // Создаем счет
        new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);


        // проверяем что счет появился у этого пользователя
//        given()
//                .header("Authorization", userAuthHeader)
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .get("http://localhost:4111/api/v1/customer/accounts")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .body("id", Matchers.notNullValue())
//                .body("accountNumber", Matchers.not(Matchers.isEmptyOrNullString()))
//                .body("[0].balance", Matchers.comparesEqualTo(0.0f))
//                .body("[0].transactions", Matchers.empty());
    }
}
