/* Лицензия */
package api.iteration_1;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

public class CreateUserTest extends BaseTest {
  @Test
  public void adminCanCreateUserWithCorrectDataTest() {
    CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

    CreateUserResponse createUserResponse =
        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated())
            .post(createUserRequest);

    ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
  }

  public static Stream<Arguments> userInvalidData() {
    return Stream.of(
        // username field validation
        Arguments.of("    ", "Pass1234&0", "USER", "username", "Username cannot be blank"),
        Arguments.of(
            "ab", "Pass1234&0", "USER", "username", "Username must be between 3 and 15 characters"),
        Arguments.of(
            "ryryrutitithfgsd",
            "Pass1234&0",
            "USER",
            "username",
            "Username must be between 3 and 15 characters"),
        Arguments.of(
            "$%^&*()@#",
            "Pass1234&0",
            "USER",
            "username",
            "Username must contain only letters, digits, dashes, underscores, and dots"),
        Arguments.of(
            "Alice123",
            "   ",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "1234567",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "PASS1234&0",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "pass1234&0",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "passwordpass",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "pass word123!",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
        Arguments.of(
            "Alice121",
            "Pass12349920",
            "USER",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"));
  }

  // Закомментирован, тк сообщения об ошибках показываются рандомно и нужно менять логику в коде.
  // ждем фикса
  //    @ParameterizedTest
  //    @MethodSource("userInvalidData")
  //    public void adminCanNotCreateUserWithIncorrectDataTest(String username, String password,
  // String role, String errorKey, String errorValue) {
  //        CreateUserRequest createUserRequest = CreateUserRequest.builder()
  //                .username(username)
  //                .password(password)
  //                .role(role)
  //                .build();
  //
  //        new CrudRequester(RequestSpecs.adminSpec(),
  //                Endpoint.ADMIN_USER,
  //                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
  //                .post(createUserRequest);
  //    }
}
