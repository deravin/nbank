package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@AllArgsConstructor
@Getter
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class),

    DELETE(
            "/users",
            DeleteUserRequest.class,
            BaseModel.class),

    LOGIN("/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class),

    DEPOSIT(
            "/accounts/deposit",
            AddDepositRequest.class,
            AccountInfoResponse.class), // или сделать заглушку - BaseModel.class

    TRANSFER(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class),

    PROFILE(
            "/customer/profile",
            BaseModel.class, // или UpdateUserNameRequest для put
            CreateUserResponse.class),

    UPDATE_PROFILE(
            "/customer/profile",
            UpdateUserNameRequest.class,
            CreateUserResponse.class),

    GET_ACCOUNT("/customer/accounts",
            BaseModel.class,
            CustomerAccountsList.class),

    CREATE_ACCOUNT(
            "/accounts",
            BaseModel.class,
            AccountInfoResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
