/* Лицензия */
package ui.iteration_2;

import api.models.AccountInfoResponse;
import api.models.AddDepositRequest;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Selenide;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

public class TransferMoneyTest extends BaseUiTest {
    // Позитивный тест 1 - перевод денег между своими счетами
    @Test
    public void userCanTransferMoneyToOwnAccountTest() {
        // Создание юзера
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        // Юзер создает 2 счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        String account2Number = account2.getAccountNumber();
        String account1Number = account1.getAccountNumber();
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account1);
        // генерируем сумму перевода
        float transferAmount = deposit.getBalance() - 100;
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку
        // Юзер кладет деньги на счет 1
        UserSteps.addDeposit(deposit, user, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // Юзер переводит деньги со счета 1 на счет 2
        new UserDashboard().open().makeTransfer();
        new TransferPage()
                .makeTransfer(account1Number, account2Number, transferAmountString)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(
                        BankAlerts.SUCCESSFULLY_TRANSFERRED.getMessage()
                                + transferAmount
                                + " to account "
                                + account2.getAccountNumber()
                                + "!");
        // Проверим что после обновления страницы на счетах изменилась сумма
        Selenide.refresh();
        float amount = (float) 100.00;
        new TransferPage().checkSelfAccounts(account1Number, amount);
        new TransferPage().checkSelfAccounts(account2Number, transferAmount);

        // Проверка на API
        // Получаем список всех аккаунтов юзера
        List<AccountInfoResponse> existingUserAccounts = UserSteps.accountsList(user).getAccounts();
        // Находим нужные аккаунты по ID
        AccountInfoResponse updatedAccount1 =
                UserSteps.getAccountIDFromList(existingUserAccounts, account1);
        AccountInfoResponse updatedAccount2 =
                UserSteps.getAccountIDFromList(existingUserAccounts, account2);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                amount, updatedAccount1.getBalance(), 0.001, "Баланс аккаунта 1 в API не совпадает");
        Assertions.assertEquals(
                transferAmount,
                updatedAccount2.getBalance(),
                0.001,
                "Баланс аккаунта 2 в API не совпадает");
    }

    // Позитивный тест 2 - перевод денег другому пользователю
    @Test
    public void userCanTransferMoneyToAnotherUserAccountTest() {
        // Админ создает пользователя 1 и 2
        CreateUserRequest user1 = AdminSteps.createUser();
        CreateUserRequest user2 = AdminSteps.createUser();
        // Юзер1 логинится в банке
        authAsUser(user1);
        // Юзер1 создает счет
        AccountInfoResponse account1 = UserSteps.createAccount(user1);
        String accountUser1Number = account1.getAccountNumber();
        // Юзер2 логинится в банке
        authAsUser(user2);
        // Юзер2 создает счет
        AccountInfoResponse account2 = UserSteps.createAccount(user2);
        String accountUser2Number = account2.getAccountNumber();
        // генерируем сумму депозита для юзера2
        AddDepositRequest deposit = UserSteps.generateDepositSum(account2);
        // генерируем сумму перевода от юзера2 к юзеру1
        float transferAmount = deposit.getBalance() - 100;
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку
        // Юзер2 кладет деньги на счет
        UserSteps.addDeposit(deposit, user2, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // Юзер2 переводит деньги на счет юзеру1
        new UserDashboard().open().makeTransfer();
        new TransferPage()
                .makeTransfer(accountUser2Number, accountUser1Number, transferAmountString)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(
                        BankAlerts.SUCCESSFULLY_TRANSFERRED.getMessage()
                                + transferAmount
                                + " to account "
                                + account1.getAccountNumber()
                                + "!");
        // Проверим что после обновления страницы сумма на счете юзера 2 изменилась
        Selenide.refresh();
        new TransferPage().checkSelfAccounts(accountUser2Number, 100.0f);

        // проверка на API
        // Получаем список всех аккаунтов юзера 1 и 2
        // Находим нужные аккаунты по ID
        List<AccountInfoResponse> existingUser1Accounts = UserSteps.accountsList(user1).getAccounts();
        List<AccountInfoResponse> existingUser2Accounts = UserSteps.accountsList(user2).getAccounts();
        AccountInfoResponse updatedAccount1 =
                UserSteps.getAccountIDFromList(existingUser1Accounts, account1);
        AccountInfoResponse updatedAccount2 =
                UserSteps.getAccountIDFromList(existingUser2Accounts, account2);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                100.0f, updatedAccount2.getBalance(), 0.001, "Баланс аккаунта 2 в API не совпадает");
        Assertions.assertEquals(
                transferAmount,
                updatedAccount1.getBalance(),
                0.001,
                "Баланс аккаунта 1 в API не совпадает");
    }

    // Негативный тест 1 - нельзя перевести некорректную сумму
    @Test
    public void userCanNotTransferIncorrectSumTest() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        // Юзер создает 2 счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        String account2Number = account2.getAccountNumber();
        String account1Number = account1.getAccountNumber();
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account1);
        float depositAmount = deposit.getBalance();
        // генерируем сумму перевода
        float transferAmount = -1 * (deposit.getBalance());
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку
        // Юзер кладет деньги на счет 1
        UserSteps.addDeposit(deposit, user, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // Юзер переводит деньги со счета 1 на счет 2
        new UserDashboard().open().makeTransfer();
        new TransferPage()
                .makeTransfer(account1Number, account2Number, transferAmountString)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(BankAlerts.ERROR_INVALID_TRANSFER.getMessage());
        // Проверим что после обновления страницы на счетах НЕ изменилась сумма
        Selenide.refresh();
        new TransferPage().checkSelfAccounts(account1Number, depositAmount);
        new TransferPage().checkSelfAccounts(account2Number, 0.0f);

        // Проверка на api что деньги не ушли
        List<AccountInfoResponse> existingUserAccounts = UserSteps.accountsList(user).getAccounts();
        // Находим нужные аккаунты по ID
        AccountInfoResponse updatedAccount1 =
                UserSteps.getAccountIDFromList(existingUserAccounts, account1);
        AccountInfoResponse updatedAccount2 =
                UserSteps.getAccountIDFromList(existingUserAccounts, account2);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                depositAmount, updatedAccount1.getBalance(), 0.001, "Баланс аккаунта 1 в API не совпадает");
        Assertions.assertEquals(
                0.0f, updatedAccount2.getBalance(), 0.001, "Баланс аккаунта 2 в API не совпадает");
    }
}
