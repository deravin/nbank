package ui.iteration_2;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.UpdateUserNameRequest;
import api.models.UpdateUserNameResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest {
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
    public void userCanChangeName(){
        // ШАГ 1: Админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();

        // ШАГ 2: Юзер логинится в банке
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

        // ШАГ 3: Юзер меняет имя
        // генерируем имя
        UpdateUserNameRequest updatedUserName = RandomModelGenerator.generate(UpdateUserNameRequest.class);

        Selenide.open("/edit-profile");

        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(updatedUserName.getName());

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert(); // переключились в окно алерта
        String alertText = alert.getText(); // считали оттуда текст и запомнили его
        assertThat(alertText).contains("✅ Name updated successfully!");
        alert.accept(); // закрыли окно алерта

        // Проверяем что имя поменялось в API
        UpdateUserNameResponse updateUserNameResponse =
                new ValidatedCrudRequester<UpdateUserNameResponse>(RequestSpecs.authAsUserSpec(user.getUsername(), user.getPassword()),
                        Endpoint.UPDATE_PROFILE,
                        ResponseSpecs.requestReturnsOK())
                        .put(updatedUserName);

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        updatedUserName.getName(),
                        updateUserNameResponse.getCustomer().getName(),
                        "Имя должно соответствовать отправленному"
                ));
    }
}
