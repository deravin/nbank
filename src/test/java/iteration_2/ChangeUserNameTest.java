package iteration_2;

import generators.RandomData;
import models.CreateUserRequest;
import models.UpdateUserNameRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.UpdateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ChangeUserNameTest {
    // позитивный тест
    @Test
    public void userCanChangeName() {
        // генерируем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // генерируем новое имя для пользователя
        UpdateUserNameRequest updatedUserName = UpdateUserNameRequest.builder()
                .name(RandomData.getUsername())
                .build();

        // передаем этого пользователя на сервер
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
                ResponseSpecs.entityWasCreated())
                .post(userRequest);  // заводим нового пользователя - вызываем переопределенный post у объекта класса AdminCreateUserRequester

        // логинимся и меняем имя
        new UpdateUserRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(updatedUserName);
    }

    // негативные тесты
    public static Stream<Arguments> userInvalidData(){
        return Stream.of(
                // если требования такие же как username, то кейсы будут такие:
                Arguments.of("    ", "username", "Username cannot be blank"),
                Arguments.of("ab" , "username", "Username must be between 3 and 15 characters"),
                Arguments.of("ryryrutitithfgsd" , "username", "Username must be between 3 and 15 characters"),
                Arguments.of("$%^&*()@#" , "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void userCanNotUpdateNameWithIncorrectData(String name, String errorKey, String errorValue) {
        // генерируем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // подставляем новое имя для пользователя
        UpdateUserNameRequest updatedUserName = UpdateUserNameRequest.builder()
                .name(name)
                .build();

        // передаем этого пользователя на сервер
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), // создаем спецификацию под админом
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // меняем имя
        new UpdateUserRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest(errorKey,errorValue))
                .put(updatedUserName);
    }
}
