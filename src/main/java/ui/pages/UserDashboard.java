/* Лицензия */
package ui.pages;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
  private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
  private SelenideElement userName = $("span");
  private SelenideElement createNewAccount = $(byText("➕ Create New Account"));
  private SelenideElement makeTransfrButton = $(byText("\uD83D\uDD04 Make a Transfer"));
  private SelenideElement makeDepositButton = $(byText("💰 Deposit Money"));

  @Override
  public String url() {
    return "/dashboard";
  }

  public UserDashboard createNewAccount() {
    createNewAccount.click();
    return this;
  }

  public UserDashboard makeTransfer() {
    makeTransfrButton.click();
    return this;
  }

  public UserDashboard makeDeposit() {
    makeDepositButton.click();
    return this;
  }

  public UserDashboard checkName(String name) {
    userName.shouldHave(text(name));
    return this;
  }
}
