/* Лицензия */
package api.iteration_1;

import api.models.AccountInfoResponse;
import api.models.CreateUserRequest;
import api.models.CustomerAccountsList;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.storage.SessionStorage;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CreateAccountTest extends BaseTest {
  @BeforeAll
  public static void setupRestAssured() {
    RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
  }

  @Test
  public void userCanCreateAccountTest() {
    CreateUserRequest userRequest = AdminSteps.createUser();

    // Создаем счет
    AccountInfoResponse accountInfoResponse = UserSteps.createAccount(userRequest);

    // Получаем список всех счетов пользователя
//    CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);
    List<AccountInfoResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

    // Проверяем что созданный счет там есть
    Assertions.assertEquals(
        List.of(accountInfoResponse).toString(),
            createdAccounts.getFirst().toString(),
        "Счет не создался");
  }
}
