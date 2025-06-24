package ui.iteration_1;

import api.models.AccountInfoResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.alerts.BankAlerts;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    public void userCanCreateAccountTest(){
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: Админ логинится в банке
        // ШАГ 2: Админ создает пользователя
        // ШАГ 3: Юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: Юзер создает аккаунт
        new UserDashboard().open().createNewAccount();

        // ШАГ 5: проверка, что аккаунт создался на API
        AccountInfoResponse createdAccount = UserSteps.accountsList(user).getAccounts().getFirst();

        // ШАГ 6: проверка, что аккаунт создался на UI
        new UserDashboard().checkedAlertMessageAndAccept(BankAlerts.NEW_ACCOUNT_CREATED.getMessage() + createdAccount.getAccountNumber());

        assertThat(createdAccount.getBalance()).isZero();

    }
}
