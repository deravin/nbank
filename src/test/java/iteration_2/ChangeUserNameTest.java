package iteration_2;

import generators.RandomModelGenerator;
import iteration_1.BaseTest;
import models.CreateUserRequest;
import models.UpdateUserNameRequest;
import models.UpdateUserNameResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
