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

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UserDepositTest {
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
    public void userWithOneAccountCanDepositCorrectSumTest(){
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

        // ШAГ 4: Юзер создает счет
        AccountInfoResponse account = UserSteps.createAccount(user);
        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account);
        String depositAmount = String.valueOf(deposit.getBalance()); // Преобразуем число в строку

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер кладет деньги на счет
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDCB0 Deposit Money")).click();
        $(".form-control.account-selector").click(); // открываю дропдаун

        $$("select.account-selector option").get(1).click(); // выбрали аккаунт для пополнения из списка

        $("input.form-control.deposit-input[type='number']").setValue(depositAmount);

        $(byText("💵 Deposit")).click();

        // ШАГ 7: Проверка на ui что деньги успешно зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("✅ Successfully deposited $" + deposit.getBalance() + " to account "); // проверили на соответствие
        alert.accept(); // закрыли окно алерта

        // ШАГ 8: Проверка на api что деньги успешно зачислились на счет
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse accountWithDeposit = existingUserAccounts[0]; // Берем первый аккаунт
        Assertions.assertEquals(deposit.getBalance(), accountWithDeposit.getBalance(),"Баланс в API не совпадает"); // Проверяем что там верная сумма
    }

    // Позитивный тест 2
    @Test
    public void userWithSeveralAccountsCanDepositCorrectSumTest(){
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

        // ШAГ 4: Юзер создает счета
        AccountInfoResponse account1 = UserSteps.createAccount(user);
        AccountInfoResponse account2 = UserSteps.createAccount(user);
        AccountInfoResponse account3 = UserSteps.createAccount(user);

        // генерируем сумму депозита
        AddDepositRequest deposit = UserSteps.generateDepositSum(account2);
        String depositAmount = String.valueOf(deposit.getBalance()); // Преобразуем число в строку

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер кладет деньги на счет
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDCB0 Deposit Money")).click();
        $(".form-control.account-selector").click(); // открываю дропдаун

        $$("select.account-selector option").get(2).click(); // выбрали аккаунт для пополнения из списка

        $("input.form-control.deposit-input[type='number']").setValue(depositAmount);

        $(byText("💵 Deposit")).click();

        // ШАГ 7: Проверка на ui что деньги успешно зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("✅ Successfully deposited $" + deposit.getBalance() + " to account "); // проверили на соответствие
        alert.accept(); // закрыли окно алерта

        // ШАГ 8: Проверка на api что деньги успешно зачислились на счет
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse accountWithDeposit = existingUserAccounts[1]; // Берем ВТОРОЙ аккаунт
        Assertions.assertEquals(deposit.getBalance(), accountWithDeposit.getBalance(),"Баланс в API не совпадает"); // Проверяем что там верная сумма
    }

    // Негативный тест 1
    @Test
    public void userWithOneAccountCanNotDepositIncorrectSumTest(){
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

        // ШAГ 4: Юзер создает счет
        AccountInfoResponse account = UserSteps.createAccount(user);

        // ШАГИ ТЕСТА:
        // ШАГ 6: Юзер кладет деньги на счет
        Selenide.open("/dashboard");
        $(byText("\uD83D\uDCB0 Deposit Money")).click();
        $(".form-control.account-selector").click(); // открываю дропдаун

        $$("select.account-selector option").get(1).click(); // выбрали аккаунт для пополнения из списка

        $("input.form-control.deposit-input[type='number']").setValue("-1000");

        $(byText("💵 Deposit")).click();

        // ШАГ 7: Проверка на ui что деньги успешно зачислились на счет
        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("❌ Please enter a valid amount."); // проверили на соответствие
        alert.accept(); // закрыли окно алерта

        // ШАГ 8: Проверка на api что деньги успешно зачислились на счет
        // Получаем список всех аккаунтов юзера
        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        AccountInfoResponse accountWithDeposit = existingUserAccounts[0]; // Берем первый аккаунт
        Assertions.assertEquals(0, accountWithDeposit.getBalance(),"Баланс в API не совпадает"); // Проверяем что там верная сумма
    }
}
