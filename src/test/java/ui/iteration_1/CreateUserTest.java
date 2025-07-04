/* Лицензия */
package ui.iteration_1;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import common.annotations.AdminSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.elements.UserBage;
import ui.pages.AdminPanel;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CreateUserTest extends BaseUiTest {
    @AdminSession
    @Test
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        assertTrue(new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkedAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .stream().noneMatch(userBage -> userBage.getUsername().trim()
                        .equalsIgnoreCase(newUser.getUsername())));

        // проверка, что юзер создан на API
        CreateUserResponse createdUser =
                AdminSteps.getAllUsers().stream()
                        .filter(user -> user.getUsername().equals(newUser.getUsername()))
                        .findFirst()
                        .get();
        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @AdminSession
    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        assertTrue(new AdminPanel()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkedAlertMessageAndAccept(BankAlerts.USER_MUST_BE_BETWEEN_3_AND_15.getMessage())
                .getAllUsers()
                .stream().noneMatch((userBage -> userBage.getUsername().equals(newUser.getUsername()))));

        // проверка, что юзер НЕ создан на API
        long userWithSameUserNameAsNewUser =
                AdminSteps.getAllUsers().stream()
                        .filter(user -> user.getUsername().equals(newUser.getUsername()))
                        .count();
        assertThat(userWithSameUserNameAsNewUser).isZero();
    }
}
