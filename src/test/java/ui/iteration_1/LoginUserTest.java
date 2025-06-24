package ui.iteration_1;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ui.BaseUiTest;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;


import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest extends BaseUiTest {
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        new LoginPage().open().login(admin.getUsername(),admin.getPassword())
                        .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();
        new LoginPage().open().login(user.getUsername(),user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
    }
}