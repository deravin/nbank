/* Лицензия */
package api.iteration_2;

import api.iteration_1.BaseTest;
import api.models.AccountInfoResponse;
import api.models.AddDepositRequest;
import api.models.CreateUserRequest;
import api.models.CustomerAccountsList;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import common.storage.SessionStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;

public class CreateDepositTest extends BaseTest {

  // Позитивные кейсы
  // - число от 0 до 5000 - 4999
  // - 5000
  @ParameterizedTest
  @ValueSource(floats = {4999, 5000})
  public void userCanAddDepositToHisAccount(float balance) {
    CreateUserRequest userRequest = AdminSteps.createUser();

    // Создаем счет
    AccountInfoResponse account = UserSteps.createAccount(userRequest);

    // генерируем данные депозита
    AddDepositRequest depositRequest =
        AddDepositRequest.builder().id(account.getId()).balance(balance).build();

    // Кладем деньги на счет
    UserSteps.addDeposit(depositRequest, userRequest, ResponseSpecs.requestReturnsOK());

    // Получаем информацию про счета пользователя
//    CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);
    List<AccountInfoResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

//    // Обновляем account1
//    AccountInfoResponse accountUpdated = UserSteps.updateAccount(account, createdAccounts);
//
//    // Проверяем что деньги появились на счете
//    Assertions.assertEquals(
//        balance, accountUpdated.getBalance(), 0.001, "Баланс после перевода не совпадает");
  }

  // Негативные тесты
  public static Stream<Arguments> userInvalidData() {
    return Stream.of(
        // граничные значения и классы эквивалентности:
        Arguments.of(-1, "Invalid account or amount"),
        Arguments.of(0, "Invalid account or amount"),
        Arguments.of(5001, "Invalid account or amount") // граничное значение - выше 5000
        );
  }

  //    @ParameterizedTest
  //    @MethodSource("userInvalidData")
  //    public void userCanNotAddIncorrectDepositToHisAccount(float balance, String error) {
  //        CreateUserRequest userRequest = AdminSteps.createUser(); // заводим нового пользователя
  // - вызываем переопределенный post у объекта класса AdminCreateUserRequester
  //
  //        // Создаем счет
  //        AccountInfoResponse accountInfoResponse = UserSteps.createAccount(userRequest);
  //
  //        // генерируем данные депозита
  //        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse);
  //
  //        // Кладем деньги на счет
  //        new CrudRequester(
  //                RequestSpecs.authAsUserSpec(userRequest.getUsername(),
  // userRequest.getPassword()),
  //                Endpoint.DEPOSIT,
  //                ResponseSpecs.requestReturnsBadRequestWithErrorInString())
  //                .post(depositRequest);
  //    }

  // Негативный тест 2 - неверный аккаунт
  @Test
  public void userCanNotAddDepositToIncorrectAccount() {
    CreateUserRequest userRequest = AdminSteps.createUser();

    // Создаем счет
    AccountInfoResponse accountInfoResponse = UserSteps.createAccount(userRequest);

    // генерируем неверные данные депозита
    AddDepositRequest depositRequest =
        AddDepositRequest.builder().id(accountInfoResponse.getId() + 1).balance(1000).build();

    // Кладем деньги на счет
    new CrudRequester(
            RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
            Endpoint.DEPOSIT,
            ResponseSpecs.requestReturnsForbidden())
        .post(depositRequest);
  }
}
