package iteration_2;

import generators.RandomModelGenerator;
import iteration_1.BaseTest;
import models.CreateUserRequest;
import models.UpdateUserNameRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
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
        new CrudRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .put(updatedUserName);
    }
}
