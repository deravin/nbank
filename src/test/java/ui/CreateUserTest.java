package ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.specs.RequestSpecs;
import com.codeborne.selenide.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
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

    @Test
    public void adminCanCreateUserTest(){
// ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();


        // ШАГ 3: проверка, что алерт "✅ User created successfully!"
        Alert alert = switchTo().alert();

        assertEquals(alert.getText(), "✅ User created successfully!");

        alert.accept();

        // ШАГ 4: проверка, что юзер отображается на UI

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        // ШАГ 5: проверка, что юзер создан на API

        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        CreateUserResponse createdUser = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();


        // ШАГ 3: проверка, что алерт "Username must be between 3 and 15 characters"
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("Username must be between 3 and 15 characters");

        alert.accept();

        // ШАГ 4: проверка, что юзер НЕ отображается на UI

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        // ШАГ 5: проверка, что юзер НЕ создан на API

        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        long usersWithSameUsernameAsNewUser = Arrays.stream(users).filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }
}
