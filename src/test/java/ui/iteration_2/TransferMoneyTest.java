package ui.iteration_2;

import api.models.AccountInfoResponse;
import api.models.AddDepositRequest;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest {
    @BeforeAll
    public static void setUp() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = false;
        Configuration.baseUrl = "http://localhost:3000";
        Configuration.timeout = 10000; // На всякий случай

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        Configuration.browserCapabilities = chromeOptions;
    }
    // Позитивный тест 1
    @Test
    public void userCanTransferMoneyToOwnAccountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: Админ логинится в банке
        // ШАГ 2: Админ создает пользователя
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 3: Юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // ШAГ 4: Юзер создает 2 счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        String account2Number = account2.getAccountNumber();
        String account1Number = account1.getAccountNumber();
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account1);
        // генерируем сумму перевода
        float transferAmount = deposit.getBalance()-100;
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку

        // ШАГ 5: Юзер кладет деньги на счет 1
        UserSteps.addDeposit(deposit, user, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер переводит деньги со счета 1 на счет 2
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDD04 Make a Transfer")).click();

        $("select.account-selector").click(); // Открываем дропдаун

        // Находим select элемент по классу
        $(".account-selector")
                // Выбираем option, который содержит текст accName
                .selectOptionContainingText(account1Number);

        $(by("placeholder", "Enter recipient account number")).setValue(account2Number);

        $(by("placeholder", "Enter amount")).setValue(transferAmountString);

        $(by("id","confirmCheck")).click();

        $(byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: Проверка на ui что деньги успешно зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("✅ Successfully transferred $" + transferAmount + " to account "+account2.getAccountNumber()+"!");
        alert.accept(); // закрыли окно алерта

        // Проверим что после обновления страницы на счетах изменилась сумма
        Selenide.refresh();

        $(".form-control.account-selector").click(); // открываю дропдаун выбора аккаунта

        $("select.account-selector")
                .selectOption( account1.getAccountNumber() +" (Balance: $100.00)"); // Смотрим акк1

        $("select.account-selector option:checked")
                .shouldHave(exactText(account1.getAccountNumber() + " (Balance: $100.00)"));

        $("select.account-selector")
                .selectOption( account2.getAccountNumber() +" (Balance: $"+transferAmount+")"); // Смотрим акк2

        $("select.account-selector option:checked")
                .shouldHave(exactText(account2.getAccountNumber() +" (Balance: $"+transferAmount+")"));


        // ШАГ 8: Проверка на api что деньги успешно зачислились на счет
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse account1WithDeposit = existingUserAccounts[0]; // Берем первый аккаунт
        Assertions.assertEquals(100, account1WithDeposit.getBalance(),"Баланс аккаунта 1 в API не совпадает"); // Проверяем что там верная сумма
        AccountInfoResponse account2WithDeposit = existingUserAccounts[1]; // Берем первый аккаунт
        Assertions.assertEquals(transferAmount, account2WithDeposit.getBalance(),"Баланс аккаунта 2 в API не совпадает"); // Проверяем что там верная сумма
    }

    // Позитивный тест 2
    @Test
    public void userCanTransferMoneyToAnotherUserAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: Админ логинится в банке
        // ШАГ 2: Админ создает пользователя 1 и 2
        CreateUserRequest user1 = AdminSteps.createUser();
        CreateUserRequest user2 = AdminSteps.createUser();

        // ШАГ 3: Юзер1 логинится в банке
        String user1AuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user1.getUsername())
                        .password(user1.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user1AuthHeader);

        // ШAГ 4: Юзер1 создает счет
        AccountInfoResponse account1 = UserSteps.createAccount(user1);
        String accountUser1Number = account1.getAccountNumber();

        // ШАГ 5: Юзер2 логинится в банке
        String user2AuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user2.getUsername())
                        .password(user2.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user2AuthHeader);

        // ШAГ 4: Юзер2 создает счет
        AccountInfoResponse account2 = UserSteps.createAccount(user2);

        // генерируем сумму депозита для юзера2
        AddDepositRequest deposit = UserSteps.generateDepositSum(account2);

        // генерируем сумму перевода
        float transferAmount = deposit.getBalance() - 100;
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку

        // ШАГ 5: Юзер2 кладет деньги на счет
        UserSteps.addDeposit(deposit, user2, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер2 переводит деньги со счета юзера1
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".form-control.account-selector").click(); // открываю дропдаун выбора аккаунта

        $$("select.account-selector option").get(1).click(); // выбрали аккаунт1 для перевода из списка

        $(by("placeholder", "Enter recipient account number")).setValue(accountUser1Number);

        $(by("placeholder", "Enter amount")).setValue(transferAmountString);

        $(by("id", "confirmCheck")).click();

        $(byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: Проверка на ui что деньги успешно зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("✅ Successfully transferred $" + transferAmount + " to account " + account1.getAccountNumber() + "!");
        alert.accept(); // закрыли окно алерта

        // Проверим что после обновления страницы сумма на счете юзера 2 изменилась
        Selenide.refresh();

        $(".form-control.account-selector").click(); // открываю дропдаун выбора аккаунта

        $("select.account-selector")
                .selectOption(account2.getAccountNumber() + " (Balance: $100.00)"); // Смотрим акк2

        $("select.account-selector option:checked")
                .shouldHave(exactText(account2.getAccountNumber() + " (Balance: $100.00)"));

        // ШАГ 8: Проверка на api что деньги успешно списались у юзера2
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUser2Accounts = given()
                .spec(RequestSpecs.authAsUserSpec(user2.getUsername(), user2.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse account2WithDeposit = existingUser2Accounts[0];
        Assertions.assertEquals(100.0f, account2WithDeposit.getBalance(), 0.01f, "Баланс аккаунта 2 в API не совпадает");
        // ШАГ 9: Проверка на api что деньги успешно пришли юзеру1
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUser1Accounts = given()
                .spec(RequestSpecs.authAsUserSpec(user1.getUsername(), user1.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse account1WithDeposit = existingUser1Accounts[0]; // Берем первый аккаунт
        Assertions.assertEquals(transferAmount, account1WithDeposit.getBalance(), "Баланс аккаунта 1 в API не совпадает"); // Проверяем что там верная сумма
    }

    // Негативный тест 1
    @Test
    public void userCanNotTransferIncorrectSumTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: Админ логинится в банке
        // ШАГ 2: Админ создает пользователя
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 3: Юзер логинится в банке
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(user.getUsername())
                        .password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // ШAГ 4: Юзер создает 2 счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        String account2Number = account2.getAccountNumber();
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account1);
        float depositAmount = deposit.getBalance();
        // генерируем сумму перевода
        float transferAmount = -1*(deposit.getBalance());
        String transferAmountString = String.valueOf(transferAmount); // Преобразуем число в строку

        // ШАГ 5: Юзер кладет деньги на счет 1
        UserSteps.addDeposit(deposit, user, ResponseSpecs.requestReturnsOK()); // положили на счет 1

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер переводит деньги со счета 1 на счет 2
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDD04 Make a Transfer")).click();

        $(".form-control.account-selector").click(); // открываю дропдаун выбора аккаунта

        $$("select.account-selector option").get(1).click(); // выбрали аккаунт1 для перевода из списка

        $(by("placeholder", "Enter recipient account number")).setValue(account2Number);

        $(by("placeholder", "Enter amount")).setValue(transferAmountString);

        $(by("id","confirmCheck")).click();

        $(byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: Проверка на ui что деньги НЕ зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept(); // закрыли окно алерта

        // Проверим что после обновления страницы на счетах изменилась сумма
        Selenide.refresh();

        $(".form-control.account-selector").click(); // открываю дропдаун выбора аккаунта

        $("select.account-selector")
                .selectOption( account1.getAccountNumber() +" (Balance: $"+depositAmount+")"); // Смотрим акк1

        $("select.account-selector option:checked")
                .shouldHave(exactText(account1.getAccountNumber() + " (Balance: $"+depositAmount+")"));

        $("select.account-selector")
                .selectOption( account2.getAccountNumber() +" (Balance: $0.00)"); // Смотрим акк2

        $("select.account-selector option:checked")
                .shouldHave(exactText(account2.getAccountNumber() +" (Balance: $0.00)"));

        // ШАГ 8: Проверка на api что деньги не ушли
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse account1WithDeposit = existingUserAccounts[0]; // Берем первый аккаунт
        Assertions.assertEquals(depositAmount, account1WithDeposit.getBalance(),"Баланс аккаунта 1 в API не совпадает"); // Проверяем что там верная сумма
        AccountInfoResponse account2WithDeposit = existingUserAccounts[1]; // Берем первый аккаунт
        Assertions.assertEquals(0.00, account2WithDeposit.getBalance(),"Баланс аккаунта 2 в API не совпадает"); // Проверяем что там верная сумма
    }
}
