package iteration_1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.CustomerAccountsList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.List;

public class CreateAccountTest extends BaseTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет
        AccountInfoResponse accountInfoResponse = UserSteps.createAccount(userRequest);

        // Получаем список всех счетов пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Проверяем что созданный счет там есть
        Assertions.assertEquals(
                List.of(accountInfoResponse).toString(),
                listOfAccounts.getAccounts().toString(),
                "Счет не создался");
    }
}
