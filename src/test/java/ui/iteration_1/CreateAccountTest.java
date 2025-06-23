package ui.iteration_1;

import api.models.AccountInfoResponse;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
    @BeforeAll
    public static void setUp() {
//        Configuration.browser = "chrome";
//        Configuration.browserSize = "1920x1080";
//        Configuration.headless = false;
//        Configuration.baseUrl = "http://localhost:3000";
//        Configuration.timeout = 10000; // На всякий случай
//
//        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
//        Configuration.browserCapabilities = chromeOptions;

        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://10.0.0.104:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );

    }

    @Test
    public void userCanCreateAccountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: Админ логинится в банке
        // ШАГ 2: Админ создает пользователя
        // ШАГ 3: Юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();

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

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 4: Юзер создает аккаунт

        $(Selectors.byText("➕ Create New Account")).click();

        // ШАГ 5: проверка, что аккаунт создался на UI

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ New Account Created! Account Number:");

        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        // ШАГ 6: проверка, что аккаунт создался на API

        AccountInfoResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountInfoResponse[].class);

        assertThat(existingUserAccounts).hasSize(1);

        AccountInfoResponse createdAccount = existingUserAccounts[0];

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();

    }
}
