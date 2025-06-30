/* Лицензия */
package ui.iteration_1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.pages.AdminPanel;

@Slf4j
public class CreateUserTest extends BaseUiTest {
    @Test
    public void adminCanCreateUserTest() {
        // ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin); // залогинись под админом

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())

                // ШАГ 3: проверка, что алерт "✅ User created successfully!"
                .checkedAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage())

                // ШАГ 4: проверка, что юзер отображается на UI
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldBe(Condition.visible);

        // ШАГ 5: проверка, что юзер создан на API
        CreateUserResponse createdUser =
                AdminSteps.getAllUsers().stream()
                        .filter(user -> user.getUsername().equals(newUser.getUsername()))
                        .findFirst()
                        .get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 1: админ залогинился в банке
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())

                // ШАГ 3: проверка, что алерт "Username must be between 3 and 15 characters"
                .checkedAlertMessageAndAccept(BankAlerts.USER_MUST_BE_BETWEEN_3_AND_15.getMessage())

                // ШАГ 4: проверка, что юзер НЕ отображается на UI
                .getAllUsers()
                .findBy(Condition.exactText(newUser.getUsername() + "\nUSER"))
                .shouldNotBe(Condition.exist);

        // ШАГ 5: проверка, что юзер НЕ создан на API
        long userWithSameUserNameAsNewUser =
                AdminSteps.getAllUsers().stream()
                        .filter(user -> user.getUsername().equals(newUser.getUsername()))
                        .count();
        assertThat(userWithSameUserNameAsNewUser).isZero();
    }
}
