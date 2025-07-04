/* Лицензия */
package ui.iteration_2;

import api.models.AccountInfoResponse;
import api.models.AddDepositRequest;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;

import java.util.List;

import common.storage.SessionStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboard;

public class UserDepositTest extends BaseUiTest {
    // Позитивный тест 1 - юзер с 1 счетом может положить депозит
    @Test
    public void userWithOneAccountCanDepositCorrectSumTest() {
        // Админ создает пользователя
        CreateUserRequest user = AdminSteps.createUser();
        // Юзер логинится в банке
  //      authAsUser(user);
        // Юзер создает счет
        AccountInfoResponse account = UserSteps.createAccount(user);
        String accountNumber = account.getAccountNumber();
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account);
        String depositAmount = String.valueOf(deposit.getBalance()); // Преобразуем число в строку

        // ШАГИ ТЕСТА:
        // Юзер кладет деньги на счет
        new UserDashboard().open().makeDeposit();
        new DepositMoneyPage()
                .addDeposit(accountNumber, depositAmount)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(
                        BankAlerts.SUCCESSFULLY_DEPOSITED.getMessage() + deposit.getBalance() + " to account ");
        // Проверка на api что деньги успешно зачислились на счет
//        List<AccountInfoResponse> existingUserAccounts = UserSteps.accountsList(user).getAccounts();
        List<AccountInfoResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();
        // Находим нужные аккаунт
        AccountInfoResponse updatedAccount =
                UserSteps.getAccountIDFromList(createdAccounts, account);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                deposit.getBalance(),
                updatedAccount.getBalance(),
                "Баланс в API не совпадает"); // Проверяем что там верная сумма
    }

    // Позитивный тест 2 - юзер с 2 счетами может положить депозит
    @Test
    public void userWithSeveralAccountsCanDepositCorrectSumTest() {
        CreateUserRequest user = AdminSteps.createUser();
 //       authAsUser(user);
        // Юзер создает счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        AccountInfoResponse account3 = UserSteps.createAccount(user);
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account2);
        String depositAmount = String.valueOf(deposit.getBalance()); // Преобразуем число в строку

        // ШАГИ ТЕСТА:
        // Юзер кладет деньги на счет
        new UserDashboard().open().makeDeposit();
        new DepositMoneyPage()
                .addDeposit(account2.getAccountNumber(), depositAmount)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(
                        BankAlerts.SUCCESSFULLY_DEPOSITED.getMessage() + deposit.getBalance() + " to account ");
        // Проверка на api что деньги успешно зачислились на счет
//        List<AccountInfoResponse> existingUserAccounts = UserSteps.accountsList(user).getAccounts();
        List<AccountInfoResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();
        // Находим нужные аккаунт
        AccountInfoResponse updatedAccount =
                UserSteps.getAccountIDFromList(createdAccounts, account2);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                deposit.getBalance(),
                updatedAccount.getBalance(),
                "Баланс в API не совпадает"); // Проверяем что там верная сумма
    }

    // Негативный тест 1
    @Test
    public void userWithOneAccountCanNotDepositIncorrectSumTest() {
        CreateUserRequest user = AdminSteps.createUser();
  //      authAsUser(user);
        // Юзер создает счет
        AccountInfoResponse account = UserSteps.createAccount(user);
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account);
        String depositAmount =
                String.valueOf(-1 * (deposit.getBalance())); // Преобразуем число в строку

        // ШАГИ ТЕСТА:
        // Юзер кладет деньги на счет
        new UserDashboard().open().makeDeposit();
        new DepositMoneyPage()
                .addDeposit(account.getAccountNumber(), depositAmount)
                // Проверка на ui что деньги успешно зачислились на счет
                .checkedAlertMessageAndAccept(BankAlerts.ENTER_A_VALID_AMOUNT.getMessage());
        // Проверка на api что деньги успешно зачислились на счет
//        List<AccountInfoResponse> existingUserAccounts = UserSteps.accountsList(user).getAccounts();
        List<AccountInfoResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();
        // Находим нужные аккаунт
        AccountInfoResponse updatedAccount =
                UserSteps.getAccountIDFromList(createdAccounts, account);
        // Проверяем балансы с допустимой погрешностью для float
        Assertions.assertEquals(
                0,
                updatedAccount.getBalance(),
                "Баланс в API не совпадает"); // Проверяем что там верная сумма
    }
}
