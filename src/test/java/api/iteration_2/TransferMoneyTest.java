package api.iteration_2;

import api.models.*;
import api.iteration_1.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

public class TransferMoneyTest extends BaseTest {

    // Positive case
    @Test
    public void userCanTransferMoneyToHisAccount() {
        // Создаем пользователя
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет 1
        AccountInfoResponse accountInfoResponse1 = UserSteps.createAccount(userRequest);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse1);

        // Кладем деньги на счет 1
        accountInfoResponse1 = UserSteps.addDeposit(depositRequest,userRequest,ResponseSpecs.requestReturnsOK());

        // Создаем счет 2
        AccountInfoResponse accountInfoResponse2 = UserSteps.createAccount(userRequest);

        // Создаем сумму для перевода
        float transferAmount = UserSteps.generateTransferAmount(accountInfoResponse1);

        //Переводим деньги на счет
        UserSteps.transferMoney(userRequest, accountInfoResponse1, accountInfoResponse2, transferAmount, ResponseSpecs.requestReturnsOK());

        // Получаем информацию про счета пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Обновляем account1 и account2
        AccountInfoResponse accountInfoResponse1Updated = UserSteps.updateAccount(accountInfoResponse1, listOfAccounts);
        AccountInfoResponse accountInfoResponse2Updated = UserSteps.updateAccount(accountInfoResponse2, listOfAccounts);

        // Вычисляем сколько денег должно быть на счете 1 и счете 2
        float newBalance1 = accountInfoResponse1.getBalance() - transferAmount;
        float newBalance2 = accountInfoResponse2.getBalance() + transferAmount;

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(newBalance1, accountInfoResponse1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(newBalance2, accountInfoResponse2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    // Negative cases
    @Test
    public void userCanNotTransferMoneyToHisOwnAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет 1
        AccountInfoResponse accountInfoResponse1 = UserSteps.createAccount(userRequest);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse1);

        // Кладем деньги на счет 1
        accountInfoResponse1 = UserSteps.addDeposit(depositRequest,userRequest,ResponseSpecs.requestReturnsOK());

        // Создаем счет 2
        AccountInfoResponse accountInfoResponse2 = UserSteps.createAccount(userRequest);

        // Создаем сумму для перевода
        float transferAmount = UserSteps.generateTransferAmount(accountInfoResponse1);

        //Переводим деньги на это же счет 1
        UserSteps.transferMoney(userRequest, accountInfoResponse1, accountInfoResponse1, transferAmount, ResponseSpecs.requestReturnsOK());

        // Получаем информацию про счета пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Обновляем account1
        AccountInfoResponse accountInfoResponse1Updated = UserSteps.updateAccount(accountInfoResponse1, listOfAccounts);

        // Проверяем что на счете 1 сумма не изменилась
        Assertions.assertEquals(accountInfoResponse1.getBalance(), accountInfoResponse1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferMoneyToNotExistedAccount() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет 1
        AccountInfoResponse accountInfoResponse1 = UserSteps.createAccount(userRequest);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse1);

        // Кладем деньги на счет
        accountInfoResponse1 = UserSteps.addDeposit(depositRequest,userRequest,ResponseSpecs.requestReturnsOK());

        // Создаем сумму для перевода
        float transferAmount = UserSteps.generateTransferAmount(accountInfoResponse1);

        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountInfoResponse1.getId())
                .receiverAccountId(1900)
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 1
        UserSteps.transferMoney(userRequest, accountInfoResponse1, accountInfoResponse1, transferAmount,
                ResponseSpecs.requestReturnsOK());

        // Получаем информацию про счета пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Обновляем account1
        AccountInfoResponse accountInfoResponse1Updated = UserSteps.updateAccount(accountInfoResponse1, listOfAccounts);

        // Проверяем что на счете 1 сумма не изменилась
        Assertions.assertEquals(accountInfoResponse1.getBalance(), accountInfoResponse1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferMoneyIfSumInTheAccountNotEnough() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет 1
        AccountInfoResponse accountInfoResponse1 = UserSteps.createAccount(userRequest);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse1);

        // Кладем деньги на счет
        accountInfoResponse1 = UserSteps.addDeposit(depositRequest, userRequest, ResponseSpecs.requestReturnsOK());

        // Создаем счет 2
        AccountInfoResponse accountInfoResponse2 = UserSteps.createAccount(userRequest);

        // Создаем сумму для перевода
        float transferAmount = accountInfoResponse1.getBalance() + 1000;

        // Пытаемся перевести деньги на счет 2
        UserSteps.transferMoney(userRequest,accountInfoResponse1,accountInfoResponse2,transferAmount,
                ResponseSpecs.requestReturnsBadRequestWithErrorInString());

        // Получаем информацию про счета пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Обновляем account1
        AccountInfoResponse accountInfoResponse1Updated = UserSteps.updateAccount(accountInfoResponse1, listOfAccounts);

        // Обновляем account2
        AccountInfoResponse accountInfoResponse2Updated = UserSteps.updateAccount(accountInfoResponse2, listOfAccounts);

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(accountInfoResponse1.getBalance(), accountInfoResponse1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(accountInfoResponse2.getBalance(), accountInfoResponse2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }

    @Test
    public void userCanNotTransferIncorrectSum() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Создаем счет 1
        AccountInfoResponse accountInfoResponse1 = UserSteps.createAccount(userRequest);

        // генерируем сумму депозита
        AddDepositRequest depositRequest = UserSteps.generateDepositSum(accountInfoResponse1);

        // Кладем деньги на счет
        accountInfoResponse1 = UserSteps.addDeposit(depositRequest, userRequest, ResponseSpecs.requestReturnsOK());

        // Создаем счет 2
        AccountInfoResponse accountInfoResponse2 = UserSteps.createAccount(userRequest);

        // Создаем сумму для перевода
        float transferAmount = -1000;

        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountInfoResponse1.getId())
                .receiverAccountId(accountInfoResponse2.getId())
                .amount(transferAmount)
                .build();

        // Пытаемся перевести деньги на счет 2
        ResponseSpecs.requestReturnsBadRequestWithErrorInString();

        // Получаем информацию про счета пользователя
        CustomerAccountsList listOfAccounts = UserSteps.accountsList(userRequest);

        // Обновляем account1
        AccountInfoResponse accountInfoResponse1Updated = UserSteps.updateAccount(accountInfoResponse1, listOfAccounts);

        // Обновляем account2
        AccountInfoResponse accountInfoResponse2Updated = UserSteps.updateAccount(accountInfoResponse2, listOfAccounts);

        // Проверяем сколько денег осталось на счете 1
        Assertions.assertEquals(accountInfoResponse1.getBalance(), accountInfoResponse1Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");

        // Проверяем что на счете 2 появилась новая сумма
        Assertions.assertEquals(accountInfoResponse2.getBalance(), accountInfoResponse2Updated.getBalance(), 0.001, "Баланс после перевода не совпадает");
    }
}


