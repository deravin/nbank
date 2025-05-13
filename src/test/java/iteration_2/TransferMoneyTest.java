package iteration_2;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }
// Positive case
    @Test
    public void userCanTransferMoneyToHisAccount() {
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

        // Создаем счет 1
        Account account1 = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account1.getId())
                .balance(RandomData.getBalance())
                .build();

        // Кладем деньги на счет - вот тут постоянно падает тест
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);

        //  Проверяем, что баланс обновился
        assertThat(account1.getBalance()).isEqualTo(depositRequest.getBalance());

        // Создаем счет 2
        Account account2 = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // Создаем сумму для перевода
        float maxAllowedTransfer = account1.getBalance();
        if (maxAllowedTransfer < 0.01f) {
            throw new IllegalStateException("Недостаточно средств для перевода: баланс = " + maxAllowedTransfer);
        }
        float transferAmount;
        do {
            transferAmount = (float) (Math.random() * maxAllowedTransfer);
        } while (transferAmount <= 1.00f); // Минимальная сумма = 1.00

        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .amount(transferAmount)
                .build();

        // Переводим деньги на счет 2
        new TransferMoneyRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(moneyRequest);

        // Проверяем что деньги ушли со счета 1
        // Вычисляем сколько денег должно остаться на счете 1
        float newBalance = account1.getBalance() - transferAmount;

        // Создаем тело ответа для счета 1
        AccountInfoResponse accountInfoResponse1 = AccountInfoResponse.builder()
                .accountNumber(account1.getAccountNumber())
                .balance(newBalance)
                .build();

        new ShowAccountInfoRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get();

    }





// этот тест не дописан

//    public static Stream<Arguments> valuesFoeNegativeTest(){
//        return Stream.of(
//                // Autorized user cannot transfer money to not existed account
//                Arguments.of(1, 200, 1000), // status code 403
//                // Autorized user cannot transfer money if account is the same
//                Arguments.of(1, 1, 1000),
//                // Autorized user cannot transfer money if amount is not enough
//                Arguments.of(1,2,9000),
//                // Autorized user cannot transfer incorrect sum
//                Arguments.of(1,2,-1000)
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("valuesFoeNegativeTest")
//    public void userCanNotTransferMoneyWithIncorrectDatas(int senderAccountId, int receiverAccountId, float amount) {
//        // генерируем пользователя
//        CreateUserRequest userRequest = CreateUserRequest.builder()
//                .username(RandomData.getUsername())
//                .password(RandomData.getPassword())
//                .role(UserRole.USER.toString())
//                .build();
//
//        // передаем этого пользователя на сервер
//        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
//                ResponseSpecs.entityWasCreated())
//                .post(userRequest);  // заводим нового пользователя - вызываем переопределенный post у объекта класса AdminCreateUserRequester
//
//        // Создаем счет 1
//        Account account1 = new CreateAccountRequester(
//                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
//                ResponseSpecs.entityWasCreated())
//                .postWithResponse(null);
//
//        // генерируем сумму депозита
//        AddDepositRequest depositRequest = AddDepositRequest.builder()
//                .id(account1.getId())
//                .balance(RandomData.getBalance())
//                .build();
//
//        // Кладем деньги на счет
//        account1 = new AddDepositRequester(
//                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
//                ResponseSpecs.requestReturnsOK())
//                .post(depositRequest)
//                .extract()
//                .as(Account.class);
//
//        //  Проверяем, что баланс обновился
//        assertThat(account1.getBalance()).isEqualTo(depositRequest.getBalance());
//
//        // Создаем счет 2
//        Account account2 = new CreateAccountRequester(
//                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
//                ResponseSpecs.entityWasCreated())
//                .postWithResponse(null);
//
//        // Создаем сумму для перевода
//        float maxAllowedTransfer = account1.getBalance();
//        if (maxAllowedTransfer < 0.01f) {
//            throw new IllegalStateException("Недостаточно средств для перевода: баланс = " + maxAllowedTransfer);
//        }
//        float transferAmount;
//        do {
//            transferAmount = (float) (Math.random() * maxAllowedTransfer);
//        } while (transferAmount <= 1.00f); // Минимальная сумма = 1.00
//
//        // Создаем тело запроса для перевода денег
//        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
//                .senderAccountId(account1.getId())
//                .receiverAccountId(account2.getId())
//                .amount(transferAmount)
//                .build();
//
//        // Переводим деньги на счет 2
//        new TransferMoneyRequester(
//                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
//                ResponseSpecs.requestReturnsOK())
//                .post(moneyRequest);
//
//        // Проверяем что деньги не ушли со счета 1
//        new ShowAccountInfoRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
//                ResponseSpecs.requestReturnsOK())
//                .get();
//    }
}
