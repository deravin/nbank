package iteration_1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                        "username": "kate21000",
                        "password": "Kate21000!",
                        "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("kate21000"))
                .body("password", Matchers.not(Matchers.equalTo("kate21000")))
                .body("role", Matchers.equalTo("USER"));
    }

    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                // username field validation
                Arguments.of("    " , "Pass1234&0", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab" , "Pass1234&0", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("ryryrutitithfgsd" , "Pass1234&0", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("$%^&*()@#" , "Pass1234&0", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("Alice123" , "   ", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "1234567", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "PASS1234&0", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "pass1234&0", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "passwordpass", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "pass word123!", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("Alice121" , "Pass12349920", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long")

        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithIncorrectDataTest(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format(
                """
                {
                "username": "%s",
                "password": "%s!",
                "role": "%s"
                 }
                """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.equalTo(errorValue));
    }
}

