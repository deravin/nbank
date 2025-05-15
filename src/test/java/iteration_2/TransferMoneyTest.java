package iteration_2;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

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

        // Кладем деньги на счет
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);  // после этого экстракта account1 - полноценное тело ответа со всеми полями

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

        // Получаем информацию про счета (иначе объекты acc1 и acc2 будут не обновленные)
        List<Account> accounts = new GetAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getAccounts();

        // Вычисляем сколько денег должно быть на счете 1 и счете 2
        float newBalance1 = account1.getBalance() - transferAmount;
        float newBalance2 = account2.getBalance() + transferAmount;

        // Обновляем account1 и account1 новыми данными из списка аккаунтов
        Account account1Updated = null;
        Account account2Updated = null;
        for (Account acc : accounts) {
            if (acc.getId() == account1.getId()) {
                account1Updated = acc;
            }
            if (acc.getId() == account2.getId()) {
                account2Updated = acc;
            }
        }

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(newBalance1, account1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(newBalance2, account2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    // Negative cases
    @Test
    public void userCanNotTransferMoneyToHisOwnAccount() {
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

        // генерируем сумму депозита для счета 1
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account1.getId())
                .balance(RandomData.getBalance())
                .build();

        // Кладем деньги на счет 1
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);

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
                .receiverAccountId(account1.getId())
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 1
        new TransferMoneyRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(moneyRequest);

        // Получаем информацию про счета (иначе объекты acc1 и acc2 будут не обновленные)
        List<Account> accounts = new GetAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getAccounts();

        // Обновляем account1
        Account account1Updated = null;
        for (Account acc : accounts) {
            if (acc.getId() == account1.getId()) {
                account1Updated = acc;
            }
        }

        // Проверяем что на счете 1 сумма не изменилась
        Assertions.assertEquals(account1.getBalance(), account1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferMoneyToNotExistedAccount() {
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

        // генерируем сумму депозита для счета 1
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account1.getId())
                .balance(RandomData.getBalance())
                .build();

        // Кладем деньги на счет 1
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);

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
                .receiverAccountId(1000)
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 1
        new TransferMoneyRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithErrorInString("Invalid transfer: insufficient funds or invalid accounts"))
                .post(moneyRequest);

        // Получаем информацию про счета (иначе объекты acc1 и acc2 будут не обновленные)
        List<Account> accounts = new GetAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getAccounts();

        // Обновляем account1
        Account account1Updated = null;
        for (Account acc : accounts) {
            if (acc.getId() == account1.getId()) {
                account1Updated = acc;
            }
        }

        // Проверяем что на счете 1 сумма не изменилась
        Assertions.assertEquals(account1.getBalance(), account1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferMoneyIfSumInTheAccountNotEnough() {
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

        // Создаем счет 2
        Account account2 = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем сумму депозита для счета 1
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account1.getId())
                .balance(RandomData.getBalance())
                .build();

        // Кладем деньги на счет 1
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);

        // Создаем сумму для перевода
        float maxAllowedTransfer = account1.getBalance();
        float transferAmount = maxAllowedTransfer + 10.00f;

        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 2
        new TransferMoneyRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithErrorInString("Invalid transfer: insufficient funds or invalid accounts"))
                .post(moneyRequest);

        // Получаем информацию про счета (иначе объекты acc1 и acc2 будут не обновленные)
        List<Account> accounts = new GetAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getAccounts();

        // Обновляем account1 и account1 новыми данными из списка аккаунтов
        Account account1Updated = null;
        Account account2Updated = null;
        for (Account acc : accounts) {
            if (acc.getId() == account1.getId()) {
                account1Updated = acc;
            }
            if (acc.getId() == account2.getId()) {
                account2Updated = acc;
            }
        }

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(account1.getBalance(), account1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(account2.getBalance(), account2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferIncorrectSum() {
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

        // Создаем счет 2
        Account account2 = new CreateAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .postWithResponse(null);

        // генерируем сумму депозита для счета 1
        AddDepositRequest depositRequest = AddDepositRequest.builder()
                .id(account1.getId())
                .balance(RandomData.getBalance())
                .build();

        // Кладем деньги на счет 1
        account1 = new AddDepositRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(Account.class);

        // Создаем сумму для перевода
        float transferAmount = -10.00f;

        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 2
        new TransferMoneyRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithErrorInString("Invalid transfer: insufficient funds or invalid accounts"))
                .post(moneyRequest);

        // Получаем информацию про счета (иначе объекты acc1 и acc2 будут не обновленные)
        List<Account> accounts = new GetAccountRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getAccounts();

        // Обновляем account1 и account1 новыми данными из списка аккаунтов
        Account account1Updated = null;
        Account account2Updated = null;
        for (Account acc : accounts) {
            if (acc.getId() == account1.getId()) {
                account1Updated = acc;
            }
            if (acc.getId() == account2.getId()) {
                account2Updated = acc;
            }
        }

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(account1.getBalance(), account1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(account2.getBalance(), account2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }
}


