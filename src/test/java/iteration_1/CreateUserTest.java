package iteration_1;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest)
                .extract().as(CreateUserResponse.class);

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());


    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of("    ", "Pass1234&0", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Pass1234&0", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("ryryrutitithfgsd", "Pass1234&0", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("$%^&*()@#", "Pass1234&0", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("Alice123", "   ", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "1234567", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "PASS1234&0", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "pass1234&0", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "passwordpass", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "pass word123!", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121", "Pass12349920", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long")

        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithIncorrectDataTest(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}

