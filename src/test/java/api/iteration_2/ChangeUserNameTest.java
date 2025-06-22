package api.iteration_2;

import api.generators.RandomModelGenerator;
import api.iteration_1.BaseTest;
import api.models.CreateUserRequest;
import api.models.UpdateUserNameRequest;
import api.models.UpdateUserNameResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class ChangeUserNameTest extends BaseTest {
    // позитивный тест
    @Test
    public void userCanChangeName() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // генерируем новое имя для пользователя
        UpdateUserNameRequest updatedUserName =
                RandomModelGenerator.generate(UpdateUserNameRequest.class);

        // логинимся и меняем имя
        UpdateUserNameResponse updateUserNameResponse =
        new ValidatedCrudRequester<UpdateUserNameResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .put(updatedUserName);

        // проверяем что имя изменилось
        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        updatedUserName.getName(),
                        updateUserNameResponse.getCustomer().getName(),
                        "Имя должно соответствовать отправленному"
                ));
    }
}
