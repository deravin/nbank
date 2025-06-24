package ui;

import api.configs.Config;
import api.iteration_1.BaseTest;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setUp() {
        Configuration.browser = Config.getProperties("browser");
        Configuration.browserSize = Config.getProperties("browser.size");
        Configuration.headless = false;

        // Запуск через локалхост
        Configuration.baseUrl = "http://localhost:3000";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        Configuration.browserCapabilities = chromeOptions;

        // Нерабочий конфиг из-за селеноида
//        Configuration.remote = Config.getProperties("ui.remote");
//        Configuration.baseUrl = Config.getProperties("ui.base.url");
//
//        Configuration.browserCapabilities.setCapability("selenoid:options",
//                Map.of("enableVNC", true, "enableLog", true)
//        );
    }

    public void authAsUser(String username, String password){
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username,password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public void authAsUser(CreateUserRequest userRequest){
        authAsUser(userRequest.getUsername(), userRequest.getPassword());
    }
}
