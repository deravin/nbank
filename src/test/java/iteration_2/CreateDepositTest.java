package iteration_2;

import generators.RandomData;
import models.Account;
import models.AddDepositRequest;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AddDepositRequester;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateDepositTest {

    // Позитивные кейсы
    // - число от 0 до 5000 - 4999
    // - 5000
    @ParameterizedTest
    @ValueSource(floats = {4999,5000})
    public void userCanAddDepositToHisAccount(float balance) {
        // генерируем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // передаем этого пользователя на сервер
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
                ResponseSpecs.entityWasCreated())
                .post(userRequest);  // заводим нового пользователя - вызываем переопределенный post у объекта класса AdminCreateUserRequester

        // Создаем счет
        Account account = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем данные депозита
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account.getId())
                .balance(balance)
                .build();

        // Кладем деньги на счет
        new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }

    // Негативные тесты
    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                // граничные значения и классы эквивалентности:
                Arguments.of(-1, "Invalid account or amount"),
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(5001, "Invalid account or amount") // граничное значение - выше 5000
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void userCanNotAddIncorrectDepositToHisAccount(float balance, String error) {
        // генерируем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // передаем этого пользователя на сервер
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
                ResponseSpecs.entityWasCreated())
                .post(userRequest);  // заводим нового пользователя - вызываем переопределенный post у объекта класса AdminCreateUserRequester

        // Создаем счет
        Account account = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем данные депозита
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account.getId())
                .balance(balance)
                .build();

        // Кладем деньги на счет
        new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithErrorInString(error))
                .post(depositRequest);
    }

    // Негативный тест 2 - неверный аккаунт
    @Test
    public void userCanNotAddDepositToIncorrectAccount() {
        // генерируем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // передаем этого пользователя на сервер
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
                ResponseSpecs.entityWasCreated())
                .post(userRequest);  // заводим нового пользователя - вызываем переопределенный post у объекта класса AdminCreateUserRequester

        // Создаем счет
        Account account = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем данные депозита
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(999999)
                .balance(1000)
                .build();

        // Кладем деньги на счет
        new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbidden("Unauthorized access to account"))
                .post(depositRequest);
    }
}
