/* Лицензия */
package api.iteration_2;

import api.generators.RandomModelGenerator;
import api.iteration_1.BaseTest;
import api.models.CreateUserRequest;
import api.models.UpdateUserNameRequest;
import api.models.UpdateUserNameResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChangeUserNameTest extends BaseTest {
  // позитивный тест
  @Test
  public void userCanChangeName() {
    CreateUserRequest user = AdminSteps.createUser();

    // генерируем новое имя для пользователя
    UpdateUserNameRequest updatedUserName =
        RandomModelGenerator.generate(UpdateUserNameRequest.class);

    // логинимся и меняем имя
    UpdateUserNameResponse updateUserName = UserSteps.updateUserName(user, updatedUserName);

    // проверяем что имя изменилось
    Assertions.assertAll(
        () ->
            Assertions.assertEquals(
                updatedUserName.getName(),
                updateUserName.getCustomer().getName(),
                "Имя должно соответствовать отправленному"));
  }
}
