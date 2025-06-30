/* Лицензия */
package ui.iteration_2;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.UpdateUserNameRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

public class ChangeNameTest extends BaseUiTest {
    // Позитивный тест 1
    @Test
    public void userCanChangeName() {
        // ШАГ 1: Админ создает юзера
        CreateUserRequest user = AdminSteps.createUser();
        // ШАГ 2: Юзер логинится в банке
        authAsUser(user);
        // ШАГ 3: Юзер меняет имя
        // генерируем имя и вводим его в UI
        UpdateUserNameRequest updatedUserName =
                RandomModelGenerator.generate(UpdateUserNameRequest.class);
        new EditProfilePage()
                .open()
                .changeName(updatedUserName.getName())
                // ШАГ 4: Проверяем алерт
                .checkedAlertMessageAndAccept(BankAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        // Проверяем что имя поменялось в API
        CreateUserResponse response = UserSteps.getUserProfile(user);
        String updateUserNameResponse = response.getName();

        // Проверка, что имя поменялось ан UI
        new UserDashboard().open().checkName(updateUserNameResponse);

        Assertions.assertAll(
                () ->
                        Assertions.assertEquals(
                                updatedUserName.getName(),
                                updateUserNameResponse,
                                "Имя должно соответствовать отправленному"));
    }
}
